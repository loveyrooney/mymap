package com.mymap.domain.clusters.service;

import com.mymap.domain.clusters.dto.ClusterMsgDTO;
import com.mymap.domain.clusters.dto.FilteredBusDTO;
import com.mymap.domain.clusters.dto.JourneyDTO;
import com.mymap.domain.clusters.dto.MarkerClusterDTO;
import com.mymap.domain.clusters.entity.Journey;
import com.mymap.domain.geoms.MarkerDTO;

import java.util.List;
import java.util.Map;

public interface ClustersService {
    long createJourney(JourneyDTO dto);
    void createMarkerCluster(List<MarkerClusterDTO> lists);
    List<MarkerClusterDTO> abstractCluster(long journeyNo);
    void createFilteredBus(List<FilteredBusDTO> lists);
    String findByArsId(long jno, String arsId);
    List<JourneyDTO> findJourneyAllByUserNo(Long principal);
    Journey findJourneyByNo(Long journeyNo);
    List<FilteredBusDTO> findFilterBusByJno(long jno);
    List<MarkerClusterDTO> findMarkerClusterByJno(long jno);
    Map<String,ClusterMsgDTO> convertToClusterMsg(List<MarkerClusterDTO> clusterList, long jno, String direction);
    void updateJourney(JourneyDTO dto);
    void deleteMarkerCluster(long no);
    void deleteFilteredBus(long no);
    void deleteJourney(long no);
}
