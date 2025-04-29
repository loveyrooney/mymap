package com.mymap.mymap.domain.params;

import java.util.List;
import java.util.Optional;

public interface ParamsService {
    void createMarkerCluster(List<MarkerClusterDTO> lists);
    String findByArsId(long jno, String arsId);
    void createFilteredBus(List<FilteredBusDTO> lists);
}
