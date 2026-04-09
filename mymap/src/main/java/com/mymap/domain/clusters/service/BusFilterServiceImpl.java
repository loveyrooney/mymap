package com.mymap.domain.clusters.service;

import com.mymap.domain.BusRepository;
import com.mymap.domain.clusters.RouteGraph;
import com.mymap.domain.clusters.BusRouteFilterUtil;
import com.mymap.domain.clusters.dto.FilteredBusDTO;
import com.mymap.domain.clusters.dto.JourneyDTO;
import com.mymap.domain.clusters.cache.GlobalStationCache;
import com.mymap.exception.BusinessException;
import com.mymap.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@Service
@RequiredArgsConstructor
public class BusFilterServiceImpl implements BusFilterService{
    private final ClustersService clustersService;
    private final BusRepository busRepository;
    private final GlobalStationCache globalStationCache;

    @Value("${topis.key}")
    private String topisKey;


    @Override
    @Transactional
    public List<FilteredBusDTO> runBusFilter(JourneyDTO journey, int routeCase){
        BusRouteFilterUtil busRouteFilterUtil = new BusRouteFilterUtil();
        //1. api call
        busRouteFilterUtil.setRoutes(callRoutes(journey));
        //2. depth setting
        busRouteFilterUtil.setDepths(new ConcurrentHashMap<>(Map.of(2,new HashSet<>(),3,new HashSet<>())));
        searchDepths(journey,routeCase, busRouteFilterUtil);
        // group setting
        Map<String, Set<String>> groups = settingGroup(journey,routeCase, busRouteFilterUtil);
        busRouteFilterUtil.setGroups(groups);
        //4. freePath setting
        List<List<String>> freePaths = busRouteFilterUtil.createPassList(groups.get("departure"),groups.get("arrive"));
        //4. routes filtering
        routesFilter(freePaths,routeCase, busRouteFilterUtil);
        //5. filtered routes sorting
        if(routeCase!=1)
            sortFilteredRoutes(busRouteFilterUtil);
        //6. filteredBus dto list setting
        List<FilteredBusDTO> lists = new ArrayList<>();
        lists.addAll(createFilteredBusDTOs(groups.get("departure"),journey.getNo(), busRouteFilterUtil));
        if(routeCase!=1)
            lists.addAll(createFilteredBusDTOs(groups.get("transfer"),journey.getNo(), busRouteFilterUtil));
        lists.addAll(createFilteredBusDTOs(groups.get("arrive"),journey.getNo(), busRouteFilterUtil));
        return lists;
    }

    private HashMap<String,String> getArsIdsFromGlobalCache(String[] stIds){
        HashMap<String,String> stIdToArsIds = new HashMap<>();
        for(String stId : stIds){
            String arsId = globalStationCache.getArsId(stId);
            stIdToArsIds.put(stId,arsId);
        }
        return stIdToArsIds;
    }

    private Set<String> getRouteIdsFromAPI(String stId, String arsId, boolean isSeoul) throws Exception{
        StringBuilder url = new StringBuilder();
        if(isSeoul){
            url.append("http://ws.bus.go.kr/api/rest/stationinfo/getRouteByStation");
            url.append("?serviceKey="+topisKey+"&arsId="+arsId);
        } else {
            url.append("https://apis.data.go.kr/6410000/busarrivalservice/v2/getBusArrivalListv2");
            url.append("?serviceKey="+topisKey+"&stationId="+stId+"&format=json");
        }
        URL realurl = new URL(url.toString());
        HttpURLConnection conn = (HttpURLConnection) realurl.openConnection();
        conn.setConnectTimeout(3000); 
        conn.setReadTimeout(5000);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
        String response = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)).readLine();
        //System.out.println("response: "+response);
        JSONObject jsonObject = isSeoul ? XML.toJSONObject(response) : new JSONObject(response);
        String jsonPrettyPrintString = jsonObject.toString(4);
        System.out.println("JSON Response:\n" + jsonPrettyPrintString);
        JSONObject msgBody = jsonObject.getJSONObject(isSeoul ? "ServiceResult" : "response").getJSONObject("msgBody");
        JSONArray busArrivalList = msgBody.optJSONArray("busArrivalList");
        if(busArrivalList==null){
            busArrivalList = new JSONArray();
            busArrivalList.put(msgBody.getJSONObject("busArrivalList"));
        }
        //System.out.println("busArrivalList: " + busArrivalList);
        Set<String> busRouteIds = new HashSet<>();
        for (int i = 0; i < busArrivalList.length(); i++) {
            String routeId = Integer.toString(busArrivalList.getJSONObject(i).getInt(isSeoul ? "busRouteId" : "routeId")); 
            busRouteIds.add(routeId);
        }
        return busRouteIds;
    }

    private Map<String,Set<String>> callRoutes(JourneyDTO journey){
        // cache 에서 stid : arsid 가져오기 
        HashMap<String,String> stToArs = new HashMap<>();
        if(journey.getFromBus()!=null)
            stToArs.putAll(getArsIdsFromGlobalCache(journey.getFromBus())); 
        if(journey.getTfBus()!=null)
            stToArs.putAll(getArsIdsFromGlobalCache(journey.getTfBus()));    
        if(journey.getToBus()!=null)
            stToArs.putAll(getArsIdsFromGlobalCache(journey.getToBus()));    
        Map<String,Set<String>> routes = new HashMap<>();
        for(String stid : stToArs.keySet()){
            boolean isSeoul = stid.charAt(0)=='1';
            try { // stid가 1로 시작하면 서울, 아니면 경기에 first try 
                Set<String> busRouteIds = getRouteIdsFromAPI(stid, stToArs.get(stid), isSeoul);
                log.info("stid, busRouteIds: {}, {}",stid,busRouteIds);
                routes.putIfAbsent(stid,busRouteIds);
            } catch (Exception e){
                try{ // call 대상을 바꿔서 retry 
                    Set<String> busRouteIds = getRouteIdsFromAPI(stid, stToArs.get(stid), !isSeoul);
                    log.info("retry stid, busRouteIds: {}, {}",stid,busRouteIds);
                    routes.putIfAbsent(stid,busRouteIds);
                } catch (Exception e2){
                    log.error("BusFilter Retry Call API Error: ", e2);
                    throw new BusinessException(ErrorCode.JOURNEY_INSERT_FAILED);
                }
            }
        }
        return routes;
    }

    private void searchDepths(JourneyDTO journey, int routeCase, BusRouteFilterUtil busRouteFilterUtil){
        Map<String, Set<String>> routes = busRouteFilterUtil.getRoutes();
        Map<Integer,Set<String>> depths = busRouteFilterUtil.getDepths();

        // TF를 탐색하면서 뎁스를 확보한다. 이 과정에서 d1-d2, d3-d4 간선 확보
        Set<String> depth2 = new HashSet<>();
        Set<String> depth3 = new HashSet<>();
        
        String[] tfBuses = journey.getTfBus();
        if (tfBuses != null) {
            for(String tk : tfBuses){
                boolean isD2 = false;
                boolean isD3 = false;
                if(routeCase==2 || routeCase==4){
                    String[] fromBuses = journey.getFromBus();
                    if (fromBuses != null) {
                        for(String dk : fromBuses){
                            List<List<String>> depth2Filter = busRouteFilterUtil.edgeSearch(routes.get(dk), routes.get(tk), dk, tk);
                            if(depth2Filter.size()>0){
                                depth2.add(tk);
                                isD2 = true;
                            }
                        }
                    }
                    if(!isD2) {
                        if(routeCase==4){
                            String[] toBuses = journey.getToBus();
                            if (toBuses != null) {
                                for (String ak : toBuses) {
                                    List<List<String>> depth3Filter = busRouteFilterUtil.edgeSearch(routes.get(tk), routes.get(ak), tk, ak);
                                    if (depth3Filter.size()>0)
                                        depth3.add(tk);
                                }
                            }
                        } else
                            depth3.add(tk);
                    }
                } else {
                    String[] toBuses = journey.getToBus();
                    if (toBuses != null) {
                        for(String ak : toBuses){
                            List<List<String>> depth3Filter = busRouteFilterUtil.edgeSearch(routes.get(ak), routes.get(tk), ak, tk);
                            if(depth3Filter.size()>0){
                                depth3.add(tk);
                                isD3 = true;
                            }
                        }
                    }
                    if(!isD3)
                        depth2.add(tk);
                }

            }
        }
        depths.put(2,depth2);
        depths.put(3,depth3);
        busRouteFilterUtil.setDepths(depths);
        // System.out.println("d2:"+depth2);
        // System.out.println("d3:"+depth3);
    }

    private Map<String, Set<String>> settingGroup(JourneyDTO journey, int routeCase, BusRouteFilterUtil busRouteFilterUtil){
        Map<String, Set<String>> groups = new ConcurrentHashMap<>();
        // routecase 에 따른 조건 분기
        if(routeCase==1){
            groups.putIfAbsent("departure", journey.getFromBus() != null ? new HashSet<>(Arrays.asList(journey.getFromBus())) : new HashSet<>());
            groups.putIfAbsent("arrive", journey.getToBus() != null ? new HashSet<>(Arrays.asList(journey.getToBus())) : new HashSet<>());
        } else if(routeCase==2){
            groups.putIfAbsent("departure", journey.getFromBus() != null ? new HashSet<>(Arrays.asList(journey.getFromBus())) : new HashSet<>());
            groups.putIfAbsent("transfer", new HashSet<>(busRouteFilterUtil.getDepths().get(2)));
            groups.putIfAbsent("arrive", new HashSet<>(busRouteFilterUtil.getDepths().get(3)));
        } else if(routeCase==3){
            groups.putIfAbsent("departure", new HashSet<>(busRouteFilterUtil.getDepths().get(2)));
            groups.putIfAbsent("transfer", new HashSet<>(busRouteFilterUtil.getDepths().get(3)));
            groups.putIfAbsent("arrive", journey.getToBus() != null ? new HashSet<>(Arrays.asList(journey.getToBus())) : new HashSet<>());
        } else {
            groups.putIfAbsent("departure", journey.getFromBus() != null ? new HashSet<>(Arrays.asList(journey.getFromBus())) : new HashSet<>());
            groups.putIfAbsent("transfer", journey.getTfBus() != null ? new HashSet<>(Arrays.asList(journey.getTfBus())) : new HashSet<>());
            groups.putIfAbsent("arrive", journey.getToBus() != null ? new HashSet<>(Arrays.asList(journey.getToBus())) : new HashSet<>());
        }
        return groups;
    }

    private void setTransferRoutes(Map<String, Set<String>> routes, Map<String, Set<String>> groups) {
        Set<String> dpars = new HashSet<>();
        groups.get("departure").forEach(k->dpars.addAll(routes.get(k)));
        groups.get("arrive").forEach(k->dpars.addAll(routes.get(k)));
        groups.get("transfer").forEach(k->routes.get(k).retainAll(dpars));
    }

    private void routesFilter(List<List<String>> freePaths, int routeCase, BusRouteFilterUtil busRouteFilterUtil){
        Map<String, Set<String>> routes = busRouteFilterUtil.getRoutes();
        Map<String, Set<String>> groups = busRouteFilterUtil.getGroups();
        Map<Integer,Set<String>> depths = busRouteFilterUtil.getDepths();

        // 뎁스별 통합 셋 생성
        Set<String> depth2TF = new HashSet<>();
        Set<String> depth3TF = new HashSet<>();
        if(routeCase!=1){
            depths.get(2).forEach(k->depth2TF.addAll(routes.get(k)));
            depths.get(3).forEach(k->depth3TF.addAll(routes.get(k)));

           System.out.println("=== d2, d3 ===");
           System.out.println(depth2TF);
           System.out.println(depth3TF);
        }

        // depth2 ~ arrive 경로 뽑기 - 이 과정에서 d2-d4 간선을 추가할 수 있다.
        List<List<String>> d2arpass = new ArrayList<>();
        if(routeCase==4)
            d2arpass = busRouteFilterUtil.createPassList(depths.get(2),groups.get("arrive"));

        // 출발지 필터링
        if(routeCase==1)
            groups.get("departure").forEach(k-> {
                if(routes.get(k) != null) routes.get(k).clear();
            });
        else if(routeCase==3)
            groups.get("departure").forEach(k->routes.get(k).retainAll(depth3TF));
        else //(routeCase==2 || routeCase==4)
            groups.get("departure").forEach(k->routes.get(k).retainAll(depth2TF));


        // 도착지 필터링
        if(routeCase==1)
            groups.get("arrive").forEach(k-> {
                if(routes.get(k) != null) routes.get(k).clear();
            });
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

            // d2-d3 방면 탐색 후 그래프 완성
            boolean isConnect = false;
            for(String d3Id : depths.get(3)){
                for(String d2Id : depths.get(2)){
                    isConnect = busRepository.depth2_near_depth3(d2Id,d3Id).orElse(false);
                    if(isConnect){
                        break;
                    }
                }
                if(isConnect){
                    break;
                }
            }

            if(isConnect){
                depth2TF.retainAll(depth3TF);
                depth3TF.retainAll(ars);
                dps.addAll(depth2TF);
                dps.addAll(depth3TF);
                groups.get("transfer").forEach(k->routes.get(k).retainAll(dps));
            } else {
                setTransferRoutes(routes, groups);
            }

            System.out.println("graph out line 280:"+busRouteFilterUtil.getGraph().getOutEdges());
            System.out.println("graph in line 281:"+busRouteFilterUtil.getGraph().getInEdges());
            List<List<String>> d2Tod3 = busRouteFilterUtil.createPassList(depths.get(2),depths.get(3));
            System.out.println("d2Tod3 : "+d2Tod3);

        } else if (routeCase==2 || routeCase==3){
            setTransferRoutes(routes, groups);
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

    private void sortFilteredRoutes(BusRouteFilterUtil busRouteFilterUtil){
        // 노선 방면에 따른 정렬 (정렬이 필요한 경우 탐색 및 정렬)
        //graph.getOutEdges().forEach((k,v)-> System.out.println("out_"+k+":"+v));
        Map<String, Set<String>> routes = busRouteFilterUtil.getRoutes();
        Map<String, Set<String>> groups = busRouteFilterUtil.getGroups();
        RouteGraph graph = busRouteFilterUtil.getGraph();
         System.out.println("routes line 294: "+routes);
         System.out.println("groups line 295: "+groups);
         System.out.println("graph out:"+graph.getOutEdges());
         System.out.println("graph in:"+graph.getInEdges());
        for(String tk : groups.get("transfer")){
            int fanOut = graph.countFanOut(tk);
            //System.out.println("fanout_"+tk+":"+graph.findFanOutAdjNodes(tk));
            if(fanOut>1){
                Set<String> sortedSet = busRouteFilterUtil.sortRoutes(routes.get(tk),graph.findFanOutAdjNodes(tk),"out");
                //System.out.println("outSort_"+tk+" : "+sortedSet);
                routes.put(tk,sortedSet);
            } else {
                int fanIn = graph.countFanIn(tk);
                //System.out.println("fanin_"+tk+":"+graph.findFanInAdjNodes(tk));
                if(fanIn>1){
                    Set<String> sortedSet = busRouteFilterUtil.sortRoutes(routes.get(tk),graph.findFanInAdjNodes(tk),"in");
                    //System.out.println("inSort_"+tk+" : "+sortedSet);
                    routes.put(tk,sortedSet);
                }
            }
        }
    }

    private List<FilteredBusDTO> createFilteredBusDTOs(Set<String> group, long journeyNo, BusRouteFilterUtil busRouteFilterUtil) {
        if (group == null) return new ArrayList<>();
        Map<String, Set<String>> routes = busRouteFilterUtil.getRoutes();
        List<FilteredBusDTO> lists = new ArrayList<>();
        Iterator<String> iterator = group.iterator();
        while(iterator.hasNext()){
            String k = iterator.next();
            FilteredBusDTO dto = new FilteredBusDTO();
            String clusterName = clustersService.findByStId(journeyNo,k);
            dto.setJourneyNo(journeyNo);
            dto.setClusterName(clusterName);
            dto.setStationId(k);
            dto.setRoutes(routes.get(k).toArray(new String[0]));
            lists.add(dto);
        }
        return lists;
    }

}
