package com.mymap.controller;

import com.mymap.domain.clusters.dto.ClusterMsgDTO;
import com.mymap.domain.clusters.dto.FilteredBusDTO;
import com.mymap.domain.clusters.dto.JourneyDTO;
import com.mymap.domain.clusters.dto.MarkerClusterDTO;
import com.mymap.domain.clusters.service.ClustersService;
import com.mymap.domain.clusters.service.BusFilterService;
import com.mymap.domain.geoms.GeomService;
import com.mymap.domain.geoms.MarkerDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BusinessController {
    private final ClustersService clustersService;
    private final BusFilterService busFilterService;
    private final GeomService geomService;

    @GetMapping("/crawling")
    public List<String> crawling(){
        return Crawling.crawlSelenium();
    }

    @PostMapping("/journey")
    public long registerJourney(@RequestBody JourneyDTO dto){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        dto.setUserNo((long)auth.getPrincipal());
        long journeyNo = clustersService.createJourney(dto);
        geomService.createFromToGeoms(dto);
        List<MarkerClusterDTO> markerClusterDTOS = clustersService.abstractCluster(journeyNo);
        clustersService.createMarkerCluster(markerClusterDTOS);
        dto.setNo(journeyNo);
        if(dto.getFromBus()!=null || dto.getTfBus()!=null || dto.getToBus()!=null){
            List<FilteredBusDTO> filteredBusDTOS = new ArrayList<>();
            if(dto.getFromBus()!=null && dto.getTfBus()!=null && dto.getToBus()!=null)
                filteredBusDTOS = busFilterService.runBusFilter(dto,4);
            else if(dto.getFromBus()!=null && dto.getToBus()!=null)
                filteredBusDTOS = busFilterService.runBusFilter(dto,1);
            else if(dto.getFromBus()!=null && dto.getTfBus()!=null)
                filteredBusDTOS = busFilterService.runBusFilter(dto,2);
            else if(dto.getTfBus()!=null && dto.getToBus()!=null)
                filteredBusDTOS = busFilterService.runBusFilter(dto,3);
            else
                return journeyNo;
            clustersService.createFilteredBus(filteredBusDTOS);
        }
        return journeyNo;
    }

    @PatchMapping("/journey")
    public long updateJourney(@RequestBody JourneyDTO dto){
        clustersService.updateJourney(dto);
        clustersService.deleteMarkerCluster(dto.getNo());
        clustersService.deleteFilteredBus(dto.getNo());
        List<MarkerClusterDTO> markerClusterDTOS = clustersService.abstractCluster(dto.getNo());
        clustersService.createMarkerCluster(markerClusterDTOS);
        if(dto.getFromBus()!=null || dto.getTfBus()!=null || dto.getToBus()!=null){
            List<FilteredBusDTO> filteredBusDTOS = new ArrayList<>();
            if(dto.getFromBus()!=null && dto.getTfBus()!=null && dto.getToBus()!=null)
                filteredBusDTOS = busFilterService.runBusFilter(dto,4);
            else if(dto.getFromBus()!=null && dto.getToBus()!=null)
                filteredBusDTOS = busFilterService.runBusFilter(dto,1);
            else if(dto.getFromBus()!=null && dto.getTfBus()!=null)
                filteredBusDTOS = busFilterService.runBusFilter(dto,2);
            else if(dto.getTfBus()!=null && dto.getToBus()!=null)
                filteredBusDTOS = busFilterService.runBusFilter(dto,3);
            else
                return dto.getNo();
            clustersService.createFilteredBus(filteredBusDTOS);
        }
        return dto.getNo();
    }

    @DeleteMapping("/journey")
    public void deleteJourney(@RequestBody JourneyDTO dto){
        clustersService.deleteJourney(dto.getNo());
        geomService.deleteFromToGeoms(dto);
        clustersService.deleteMarkerCluster(dto.getNo());
        clustersService.deleteFilteredBus(dto.getNo());
    }

    @GetMapping("/journeys")
    public List<Long> journeys(){
        // main 페이지에서 fetch 요청을 받는 곳. 해당 유저의 journey list 를 보내야함
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("main: "+auth.getPrincipal());
        return clustersService.findJourneyAllByUserNo((Long)auth.getPrincipal());
    }

    @PostMapping("/map_geom")
    public List<MarkerDTO> map_geom(@RequestBody Map<String,Long> body){
        System.out.println("jno: "+body.get("jno"));
        long jno = body.get("jno");
        // map 페이지에서 fetch 요청을 받는 곳. 마커들의 geometry 정보를 보내야함
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("map: "+auth.getPrincipal());
        List<MarkerClusterDTO> clusters = clustersService.findMarkerClusterByJno(jno);
        // 지금은 클러스터 좌표만 보내주고 있는데 각각의 정류장 좌표도 보내줘야 함.
        return geomService.findGeoms(clusters,(Long)auth.getPrincipal());
    }

    @PostMapping("/map_msg")
    public Map<String, ClusterMsgDTO> map_msg(@RequestBody Map<String,String> body){
        System.out.println("jno: "+body.get("jno"));
        long jno = Long.parseLong(body.get("jno"));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("map: "+auth.getPrincipal());
        List<MarkerClusterDTO> clusterList = clustersService.findMarkerClusterByJno(jno);
        // map 페이지에서 fetch 요청을 받는 곳. 실시간 조회를 위한 클라이언트의 msg list 를 보내야함
        return clustersService.convertToClusterMsg(clusterList,jno,(String)body.get("direction"));
    }

}

