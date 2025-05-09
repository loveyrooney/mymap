package com.mymap.controller;

import com.mymap.domain.clusters.dto.ClusterMsgDTO;
import com.mymap.domain.clusters.dto.JourneyDTO;
import com.mymap.domain.clusters.dto.MarkerClusterDTO;
import com.mymap.domain.clusters.ClustersService;
import com.mymap.domain.geoms.MarkerDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BusinessController {
    private final ClustersService clustersService;

    @PostMapping("/register")
    public long register(@RequestBody JourneyDTO dto){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        dto.setUserNo((long)auth.getPrincipal());
        long journeyNo = clustersService.createJourney(dto);
        return 0;
    }

    @PostMapping("/journeys")
    public List<Long> journeys(){
        // main 페이지에서 fetch 요청을 받는 곳. 해당 유저의 journey list 를 보내야함
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("main: "+auth.getPrincipal());
        return clustersService.findAllByUserNo((Long)auth.getPrincipal());
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
        return clustersService.findGeoms(clusters,(Long)auth.getPrincipal());
    }

    @PostMapping("/map_msg")
    public Map<String, ClusterMsgDTO> map_msg(@RequestBody Map<String,Long> body){
        System.out.println("jno: "+body.get("jno"));
        long jno = body.get("jno");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("map: "+auth.getPrincipal());
        List<MarkerClusterDTO> clusterList = clustersService.findMarkerClusterByJno(jno);
        // map 페이지에서 fetch 요청을 받는 곳. 실시간 조회를 위한 클라이언트의 msg list 를 보내야함
        return clustersService.convertToClusterMsg(clusterList);
    }

}

