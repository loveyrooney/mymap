package com.mymap.domain.clusters.service;

import com.mymap.domain.clusters.RouteGraph;
import com.mymap.domain.clusters.BusRouteFilterUtil;
import com.mymap.domain.clusters.dto.FilteredBusDTO;
import com.mymap.domain.clusters.dto.JourneyDTO;
import com.mymap.exception.BusinessException;
import com.mymap.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@PropertySource("classpath:mymap_jwt.properties")
@RequiredArgsConstructor
public class BusFilterServiceImpl implements BusFilterService{
    private final BusRouteFilterUtil busRouteFilterUtil;
    private final ClustersService clustersService;

    @Value("${topis-key}")
    private String topisKey;


    @Override
    @Transactional
    public List<FilteredBusDTO> runBusFilter(JourneyDTO journey, int routeCase){
        //1. api call
        busRouteFilterUtil.setRoutes(callRoutes(journey));
        //2. depth setting
        busRouteFilterUtil.setDepths(new ConcurrentHashMap<>(Map.of(2,new HashSet<>(),3,new HashSet<>())));
        searchDepths(journey,routeCase);
        // group setting
        Map<String, Set<String>> groups = settingGroup(journey,routeCase);
        busRouteFilterUtil.setGroups(groups);
        //4. freePath setting
        List<List<String>> freePaths = busRouteFilterUtil.createPassList(groups.get("departure"),groups.get("arrive"));
        //4. routes filtering
        routesFilter(freePaths,routeCase);
        //5. filtered routes sorting
        if(routeCase!=1)
            sortFilteredRoutes();
        //6. filteredBus dto list setting
        List<FilteredBusDTO> lists = new ArrayList<>();
        lists.addAll(createFilteredBusDTOs(groups.get("departure"),journey.getNo()));
        if(routeCase!=1)
            lists.addAll(createFilteredBusDTOs(groups.get("transfer"),journey.getNo()));
        lists.addAll(createFilteredBusDTOs(groups.get("arrive"),journey.getNo()));
        return lists;
    }

    private Map<String,Set<String>> callRoutes(JourneyDTO journey){
        StringBuilder url = new StringBuilder();
        url.append("http://ws.bus.go.kr/api/rest/stationinfo/getRouteByStation");
        url.append("?serviceKey="+topisKey+"&arsId=");
        List<String> arsIds = new ArrayList<>();
        if(journey.getFromBus()!=null)
            arsIds.addAll(Arrays.asList(journey.getFromBus()));
        if(journey.getTfBus()!=null)
            arsIds.addAll(Arrays.asList(journey.getTfBus()));
        if(journey.getToBus()!=null)
            arsIds.addAll(Arrays.asList(journey.getToBus()));
        Map<String,Set<String>> routes = new HashMap<>();
        for(String id : arsIds){
            try {
                // api call
                URL realurl = new URL(url+id);
                HttpURLConnection conn = (HttpURLConnection) realurl.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-type", "application/json");
                String response = new BufferedReader(new InputStreamReader(conn.getInputStream())).readLine();
                //System.out.println(response);
                // response to document
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                InputSource inputSource = new InputSource(new StringReader(response));
                Document document = builder.parse(inputSource);
                // 특정 xml 태그 abstract
                XPath xpath = XPathFactory.newInstance().newXPath();
                XPathExpression expr = xpath.compile("//itemList/busRouteAbrv");
                NodeList nodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
                Set<String> busRouteAbrvs = new HashSet<>();
                for (int i = 0; i < nodes.getLength(); i++) {
                    String adrvs = nodes.item(i).getTextContent();
                    busRouteAbrvs.add(adrvs);
                    //System.out.println(id+","+adrvs);
                }
                routes.putIfAbsent(id,busRouteAbrvs);
            } catch (Exception e){
                //System.out.println(e);
                throw new BusinessException(ErrorCode.JOURNEY_INSERT_FAILED);
            }
        }
        return routes;
    }

    private void searchDepths(JourneyDTO journey, int routeCase){
        Map<String, Set<String>> routes = busRouteFilterUtil.getRoutes();
        Map<Integer,Set<String>> depths = busRouteFilterUtil.getDepths();

        // TF를 탐색하면서 뎁스를 확보한다. 이 과정에서 d1-d2, d3-d4 간선 확보
        Set<String> depth2 = new HashSet<>();
        Set<String> depth3 = new HashSet<>();
        for(String tk : journey.getTfBus()){
            boolean isD2 = false;
            boolean isD3 = false;
            if(routeCase==2 || routeCase==4){
                for(String dk : journey.getFromBus()){
                    List<List<String>> depth2Filter = busRouteFilterUtil.edgeSearch(routes.get(dk), routes.get(tk), dk, tk);
                    if(depth2Filter.size()>0){
                        depth2.add(tk);
                        isD2 = true;
                    }
                }
                if(!isD2) {
                    if(routeCase==4){
                        for (String ak : journey.getToBus()) {
                            List<List<String>> depth3Filter = busRouteFilterUtil.edgeSearch(routes.get(tk), routes.get(ak), tk, ak);
                            if (depth3Filter.size()>0)
                                depth3.add(tk);
                        }
                    } else
                        depth3.add(tk);
                }
            } else {
                for(String ak : journey.getToBus()){
                    List<List<String>> depth3Filter = busRouteFilterUtil.edgeSearch(routes.get(ak), routes.get(tk), ak, tk);
                    if(depth3Filter.size()>0){
                        depth3.add(tk);
                        isD3 = true;
                    }
                }
                if(!isD3)
                    depth2.add(tk);
            }

        }
        depths.put(2,depth2);
        depths.put(3,depth3);
        busRouteFilterUtil.setDepths(depths);
        System.out.println("d2:"+depth2);
        System.out.println("d3:"+depth3);
    }

    private Map<String, Set<String>> settingGroup(JourneyDTO journey, int routeCase){
        Map<String, Set<String>> groups = new ConcurrentHashMap<>();
        // routecase 에 따른 조건 분기
        if(routeCase==1){
            groups.putIfAbsent("departure",new HashSet<>(Arrays.asList(journey.getFromBus())));
            groups.putIfAbsent("arrive",new HashSet<>(Arrays.asList(journey.getToBus())));
        } else if(routeCase==2){
            groups.putIfAbsent("departure",new HashSet<>(Arrays.asList(journey.getFromBus())));
            groups.putIfAbsent("transfer",new HashSet<>(busRouteFilterUtil.getDepths().get(2)));
            groups.putIfAbsent("arrive",new HashSet<>(busRouteFilterUtil.getDepths().get(3)));
        } else if(routeCase==3){
            groups.putIfAbsent("departure",new HashSet<>(busRouteFilterUtil.getDepths().get(2)));
            groups.putIfAbsent("transfer",new HashSet<>(busRouteFilterUtil.getDepths().get(3)));
            groups.putIfAbsent("arrive",new HashSet<>(Arrays.asList(journey.getToBus())));
        } else {
            groups.putIfAbsent("departure",new HashSet<>(Arrays.asList(journey.getFromBus())));
            groups.putIfAbsent("transfer",new HashSet<>(Arrays.asList(journey.getTfBus())));
            groups.putIfAbsent("arrive",new HashSet<>(Arrays.asList(journey.getToBus())));
        }
        return groups;
    }

    private void routesFilter(List<List<String>> freePaths, int routeCase){
        Map<String, Set<String>> routes = busRouteFilterUtil.getRoutes();
        Map<String, Set<String>> groups = busRouteFilterUtil.getGroups();
        Map<Integer,Set<String>> depths = busRouteFilterUtil.getDepths();

        // 뎁스별 통합 셋 생성
        Set<String> depth2TF = new HashSet<>();
        Set<String> depth3TF = new HashSet<>();
        if(routeCase!=1){
            depths.get(2).forEach(k->depth2TF.addAll(routes.get(k)));
            depths.get(3).forEach(k->depth3TF.addAll(routes.get(k)));

//            System.out.println("=== d2, d3 ===");
//            System.out.println(depth2TF);
//            System.out.println(depth3TF);
        }

        // depth2 ~ arrive 경로 뽑기 - 이 과정에서 d2-d4 간선을 추가할 수 있다.
        List<List<String>> d2arpass = new ArrayList<>();
        if(routeCase==4)
            d2arpass = busRouteFilterUtil.createPassList(depths.get(2),groups.get("arrive"));

        // 출발지 필터링
        if(routeCase==1)
            groups.put("departure",new HashSet<>());
        else if(routeCase==3)
            groups.get("departure").forEach(k->routes.get(k).retainAll(depth3TF));
        else //(routeCase==2 || routeCase==4)
            groups.get("departure").forEach(k->routes.get(k).retainAll(depth2TF));


        // 도착지 필터링
        if(routeCase==1)
            groups.put("arrive",new HashSet<>());
        else if(routeCase==2)
            groups.get("arrive").forEach(k->routes.get(k).retainAll(depth2TF));
        else //if(routeCase==3 || routeCase==4)
            groups.get("arrive").forEach(k->routes.get(k).retainAll(depth3TF));

        // 환승지 필터링
        if(routeCase==4){
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
        } else if (routeCase==2 || routeCase==3){
            Set<String> dpars = new HashSet<>();
            groups.get("departure").forEach(k->dpars.addAll(routes.get(k)));
            groups.get("arrive").forEach(k->dpars.addAll(routes.get(k)));
            groups.get("transfer").forEach(k->routes.get(k).retainAll(dpars));
            if(routeCase==2)
                busRouteFilterUtil.createPassList(depths.get(2),groups.get("arrive"));
            else
                busRouteFilterUtil.createPassList(depths.get(3),groups.get("arrive"));
        }

        // 프리패스 및 d2패스 추가
        busRouteFilterUtil.addPass(freePaths);
        if(routeCase==4)
            busRouteFilterUtil.addPass(d2arpass);
    }

    private void sortFilteredRoutes(){
        // 노선 방면에 따른 정렬 (정렬이 필요한 경우 탐색 및 정렬)
        //graph.getOutEdges().forEach((k,v)-> System.out.println("out_"+k+":"+v));
        Map<String, Set<String>> routes = busRouteFilterUtil.getRoutes();
        Map<String, Set<String>> groups = busRouteFilterUtil.getGroups();
        RouteGraph graph = busRouteFilterUtil.getGraph();
        for(String tk : groups.get("transfer")){
            int fanOut = graph.countFanOut(tk);
            System.out.println("fanout_"+tk+":"+graph.findFanOutAdjNodes(tk));
            if(fanOut>1){
                Set<String> sortedSet = busRouteFilterUtil.sortRoutes(routes.get(tk),graph.findFanOutAdjNodes(tk),"out");
                System.out.println("outSort_"+tk+" : "+sortedSet);
                routes.put(tk,sortedSet);
            } else {
                int fanIn = graph.countFanIn(tk);
                System.out.println("fanin_"+tk+":"+graph.findFanInAdjNodes(tk));
                if(fanIn>1){
                    Set<String> sortedSet = busRouteFilterUtil.sortRoutes(routes.get(tk),graph.findFanInAdjNodes(tk),"in");
                    System.out.println("inSort_"+tk+" : "+sortedSet);
                    routes.put(tk,sortedSet);
                }
            }
        }
    }

    private List<FilteredBusDTO> createFilteredBusDTOs(Set<String> group, long journeyNo) {
        Map<String, Set<String>> routes = busRouteFilterUtil.getRoutes();
        List<FilteredBusDTO> lists = new ArrayList<>();
        Iterator<String> iterator = group.iterator();
        while(iterator.hasNext()){
            String k = iterator.next();
            FilteredBusDTO dto = new FilteredBusDTO();
            String clusterName = clustersService.findByArsId(journeyNo,k);
            dto.setJourneyNo(journeyNo);
            dto.setClusterName(clusterName);
            dto.setArsId(k);
            dto.setRoutes(routes.get(k).toArray(new String[0]));
            lists.add(dto);
            //System.out.println(routes.get(k));
        }
        return lists;
    }

}
