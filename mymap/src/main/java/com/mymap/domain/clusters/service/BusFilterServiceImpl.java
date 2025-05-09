package com.mymap.domain.clusters.service;

import com.mymap.RouteGraph;
import com.mymap.domain.clusters.BusRouteFilterUtil;
import com.mymap.domain.clusters.dto.JourneyDTO;
import com.mymap.domain.clusters.entity.Journey;
import com.mymap.domain.clusters.repository.JourneyRepository;
import com.mymap.exception.BusinessException;
import com.mymap.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@PropertySource("classpath:mymap_jwt.properties")
@RequiredArgsConstructor
public class BusFilterServiceImpl implements BusFilterService{
    private final RestTemplate restTemplate;
    private final BusRouteFilterUtil busRouteFilterUtil;
    private final JourneyRepository journeyRepository;
    private final RouteGraph graph;

    @Value("${topis-key}")
    private String topisKey;

    @Override
    @Transactional
    public long createJourney(JourneyDTO dto) {
        Journey entity = Journey.builder()
                .userNo(dto.getUserNo()).fromName(dto.getFromName()).toName(dto.getToName()).fromBus(dto.getFromBus()).tfBus(dto.getTfBus()).toBus(dto.getToBus()).fromSub(dto.getFromSub()).tfSub(dto.getTfSub()).toSub(dto.getToSub()).fromBike(dto.getFromBike()).tfBike(dto.getTfBike()).toBike(dto.getToBike())
                .build();
        Journey save = journeyRepository.save(entity);
        Map<String, Set<String>> groups = new ConcurrentHashMap<>();
        groups.putIfAbsent("departure",new HashSet<>(Arrays.asList(save.getFromBus())));
        groups.putIfAbsent("transfer",new HashSet<>(Arrays.asList(save.getTfBus())));
        groups.putIfAbsent("arrive",new HashSet<>(Arrays.asList(save.getToBus())));
        busRouteFilterUtil.setGroups(groups);
        busRouteFilterUtil.setDepths(new ConcurrentHashMap<>(Map.of(2,new HashSet<>(),3,new HashSet<>())));
        busRouteFilterUtil.setRoutes(callRoutes(save));
        List<List<String>> freePaths = freePathAndDepths();
        routesFilter(freePaths);
        // 정렬부 메서드 여기서 실행 후 filteredBus insert
        return save.getNo();
    }

    private Map<String,Set<String>> callRoutes(Journey journey){
        StringBuilder url = new StringBuilder();
        url.append("http://ws.bus.go.kr/api/rest/stationinfo/getRouteByStation");
        url.append("?serviceKey="+topisKey+"&arsId=");
        List<String> arsIds = new ArrayList<>();
        arsIds.addAll(Arrays.asList(journey.getFromBus()));
        arsIds.addAll(Arrays.asList(journey.getTfBus()));
        arsIds.addAll(Arrays.asList(journey.getToBus()));
        Map<String,Set<String>> routes = busRouteFilterUtil.getRoutes();
        for(String id : arsIds){
            ResponseEntity<String> response = restTemplate.getForEntity(url+id, String.class);
            // XPath를 사용하여 특정 태그에 접근
            try {
                XPath xpath = XPathFactory.newInstance().newXPath();
                XPathExpression expr = xpath.compile("//itemList/busRouteAbrv");
                Set<String> busRouteAbrvs = new HashSet<>();
                var nodes = (org.w3c.dom.NodeList) expr.evaluate(response.getBody(), javax.xml.xpath.XPathConstants.NODESET);
                for (int i = 0; i < nodes.getLength(); i++) {
                    String adrvs = nodes.item(i).getTextContent();
                    busRouteAbrvs.add(adrvs);
                }
                routes.putIfAbsent(id,busRouteAbrvs);
            } catch (Exception e){
                throw new BusinessException(ErrorCode.JOURNEY_CREATE_FAILED);
            }
        }
        return routes;
    }

    private List<List<String>> freePathAndDepths(){
        Map<String, Set<String>> routes = busRouteFilterUtil.getRoutes();
        Map<String, Set<String>> groups = busRouteFilterUtil.getGroups();
        Map<Integer,Set<String>> depths = busRouteFilterUtil.getDepths();
        // 프리패스를 먼저 확보한다. 이 과정에서 d1-d4 간선을 추가할 수 있다.
        List<List<String>> freePath = busRouteFilterUtil.createPassList(groups.get("departure"),groups.get("arrive"));

        // TF를 탐색하면서 뎁스를 확보한다. 이 과정에서 d1-d2, d3-d4 간선 확보
        Set<String> depth2 = new HashSet<>();
        Set<String> depth3 = new HashSet<>();
        for(String tk : groups.get("transfer")){
            boolean isD2 = false;
            for(String dk : groups.get("departure")){
                List<List<String>> depth2Filter = busRouteFilterUtil.edgeSearch(routes.get(dk), routes.get(tk), dk, tk);
                if(depth2Filter.size()>0){
                    depth2.add(tk);
                    isD2 = true;
                }
            }
            if(!isD2) {
                for (String ak : groups.get("arrive")) {
                    List<List<String>> depth3Filter = busRouteFilterUtil.edgeSearch(routes.get(tk), routes.get(ak), tk, ak);
                    if (depth3Filter.size()>0)
                        depth3.add(tk);
                }
            }
        }
        depths.put(2,depth2);
        depths.put(3,depth3);
        busRouteFilterUtil.setDepths(depths);
        return freePath;
    }

    private void routesFilter(List<List<String>> freePaths){
        Map<String, Set<String>> routes = busRouteFilterUtil.getRoutes();
        Map<String, Set<String>> groups = busRouteFilterUtil.getGroups();
        Map<Integer,Set<String>> depths = busRouteFilterUtil.getDepths();
        // 뎁스별 통합 셋 생성
        Set<String> depth2TF = new HashSet<>();
        Set<String> depth3TF = new HashSet<>();
        depths.get(2).forEach(k->depth2TF.addAll(routes.get(k)));
        depths.get(3).forEach(k->depth3TF.addAll(routes.get(k)));

//        System.out.println("=== d2, d3 ===");
//        System.out.println(depth2TF);
//        System.out.println(depth3TF);

        // depth2 ~ arrive 경로 뽑기 - 이 과정에서 d2-d4 간선을 추가할 수 있다.
        List<List<String>> d2arpass = busRouteFilterUtil.createPassList(depths.get(2),groups.get("arrive"));

        // 출발지 필터링
        groups.get("departure").forEach(k->routes.get(k).retainAll(depth2TF));

        // 도착지 필터링
        groups.get("arrive").forEach(k->routes.get(k).retainAll(depth3TF));

        // 환승지 필터링
        Set<String> dps = new HashSet<>();
        groups.get("departure").forEach(k->dps.addAll(routes.get(k)));
        Set<String> ars = new HashSet<>();
        groups.get("arrive").forEach(k->ars.addAll(routes.get(k)));
        depth2TF.retainAll(depth3TF);
        depth3TF.retainAll(ars);
        dps.addAll(depth2TF);
        dps.addAll(depth3TF);
        groups.get("transfer").forEach(k->routes.get(k).retainAll(dps));

        // d2-d3 방면 탐색 후 그래프 완성
        busRouteFilterUtil.createPassList(depths.get(2),depths.get(3));

        // 프리패스 및 d2패스 추가
        busRouteFilterUtil.addPass(freePaths);
        busRouteFilterUtil.addPass(d2arpass);
    }
}
