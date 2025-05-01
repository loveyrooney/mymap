package com.mymap.mymap.domain.clusters;

import com.mymap.mymap.domain.clusters.dto.FilteredBusDTO;
import com.mymap.mymap.domain.clusters.dto.MarkerClusterDTO;
import com.mymap.mymap.domain.geoms.MarkerDTO;

import java.util.List;

public interface ClustersService {
    void createMarkerCluster(List<MarkerClusterDTO> lists);
    String findByArsId(long jno, String arsId);
    void createFilteredBus(List<FilteredBusDTO> lists);
    List<Long> findAllByUserNo(Long principal);
    List<FilteredBusDTO> findFilterBusByJno(long jno);
    List<MarkerClusterDTO> findMarkerClusterByJno(long jno);
    List<MarkerDTO> findGeoms(List<MarkerClusterDTO> clusters,Long auth);
}
