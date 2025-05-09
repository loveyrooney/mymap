package com.mymap.domain.clusters;

import com.mymap.domain.clusters.dto.ClusterMsgDTO;
import com.mymap.domain.clusters.dto.FilteredBusDTO;
import com.mymap.domain.clusters.dto.JourneyDTO;
import com.mymap.domain.clusters.dto.MarkerClusterDTO;
import com.mymap.domain.geoms.MarkerDTO;

import java.util.List;
import java.util.Map;

public interface ClustersService {
    void createMarkerCluster(List<MarkerClusterDTO> lists);
    String findByArsId(long jno, String arsId);
    void createFilteredBus(List<FilteredBusDTO> lists);
    List<Long> findAllByUserNo(Long principal);
    List<FilteredBusDTO> findFilterBusByJno(long jno);
    List<MarkerClusterDTO> findMarkerClusterByJno(long jno);
    List<MarkerDTO> findGeoms(List<MarkerClusterDTO> clusters,Long auth);
    Map<String,ClusterMsgDTO> convertToClusterMsg(List<MarkerClusterDTO> clusterList);
    long createJourney(JourneyDTO dto);
}
