package com.mymap.domain.clusters.service;

import com.mymap.domain.clusters.dto.FilteredBusDTO;
import com.mymap.domain.clusters.dto.JourneyDTO;

import java.util.List;

public interface BusFilterService {
    List<FilteredBusDTO> runBusFilter(JourneyDTO journey);
}
