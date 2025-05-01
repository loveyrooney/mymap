package com.mymap.mymap.controller;

import com.mymap.mymap.domain.clusters.dto.MarkerClusterDTO;
import com.mymap.mymap.domain.clusters.ClustersService;
import com.mymap.mymap.domain.geoms.MarkerDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BusinessController {
    private final ClustersService clustersService;

    @PostMapping("/journeys")
    public List<Long> journeys(){
        // main 페이지에서 fetch 요청을 받는 곳. 해당 유저의 journey list 를 보내야함
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("main: "+auth.getPrincipal());
        return clustersService.findAllByUserNo((Long)auth.getPrincipal());
    }

    @PostMapping("/map")
    public List<MarkerClusterDTO> map(@RequestBody Long jno){
        // map 페이지에서 fetch 요청을 받는 곳. 마커들의 geometry 정보를 보내야함
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<MarkerClusterDTO> clusters = clustersService.findMarkerClusterByJno(jno);
        List<MarkerDTO> markers = clustersService.findGeoms(clusters,(Long)auth.getPrincipal());
        // map 페이지에서 fetch 요청을 받는 곳. 실시간 조회를 위한 클라이언트의 msg list 를 보내야함
        return null;
    }

}
