package com.mymap.domain.geoms;

import com.mymap.domain.clusters.dto.JourneyDTO;
import com.mymap.domain.clusters.dto.MarkerClusterDTO;
import com.mymap.domain.geoms.MarkerDTO;

import java.util.List;

public interface GeomService {
    List<MarkerDTO> findGeoms(List<MarkerClusterDTO> clusters, Long auth, long jno);
    void createFromToGeoms(JourneyDTO journey);
    void deleteFromToGeoms(JourneyDTO dto);
    List<TransferDTO> findTransfers(TransferReqDTO dto);
}
