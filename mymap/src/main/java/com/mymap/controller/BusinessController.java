package com.mymap.controller;

import com.mymap.domain.clusters.dto.*;
import com.mymap.domain.clusters.service.ClustersService;
import com.mymap.domain.clusters.service.BusFilterService;
import com.mymap.domain.geoms.GeomService;
import com.mymap.domain.geoms.MarkerDTO;
import com.mymap.domain.geoms.TransferDTO;
import com.mymap.domain.geoms.TransferReqDTO;
import com.mymap.exception.BusinessException;
import com.mymap.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log4j2
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

    @PostMapping("/transfer")
    public List<TransferDTO> transfer(@RequestBody TransferReqDTO dto){
        return geomService.findTransfers(dto);
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

    @DeleteMapping("/journey/{jno}")
    public long deleteJourney(@PathVariable long jno){
        try {
            JourneyDTO dto = clustersService.findJourneyDTO(jno);
            geomService.deleteFromToGeoms(dto);  //userNo, fromName, toName
            clustersService.deleteJourneyByNo(dto.getNo());
            clustersService.deleteMarkerCluster(dto.getNo());
            clustersService.deleteFilteredBus(dto.getNo());
        } catch (Exception e){
            throw new BusinessException(ErrorCode.JOURNEY_DELETE_FAILED);
        }
        return jno;
    }

    @GetMapping("/journeys")
    public List<JourneyDTO> journeys(){
        // main 페이지에서 fetch 요청을 받는 곳. 해당 유저의 journey list 를 보내야함
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return clustersService.findJourneyAllByUserNo((Long)auth.getPrincipal());
    }

    @GetMapping("/map_geom/{jno}")
    public List<MarkerDTO> map_geom(@PathVariable long jno){
        // map 페이지에서 fetch 요청을 받는 곳. 마커들의 geometry 정보를 보내야함
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<MarkerClusterDTO> clusters = clustersService.findMarkerClusterByJno(jno);
        // 지금은 클러스터 좌표만 보내주고 있는데 각각의 정류장 좌표도 보내줘야 함.
        return geomService.findGeoms(clusters,(Long)auth.getPrincipal(),jno);
    }

    @PostMapping("/map_msg")
    public Map<String, ClusterMsgDTO> map_msg(@RequestBody Map<String,String> body){
        long jno = Long.parseLong(body.get("jno"));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<MarkerClusterDTO> clusterList = clustersService.findMarkerClusterByJno(jno);
        // map 페이지에서 fetch 요청을 받는 곳. 실시간 조회를 위한 클라이언트의 msg list 를 보내야함
        return clustersService.convertToClusterMsg(clusterList,jno,(String)body.get("direction"));
    }

}

