package com.mymap.mymap.domain.params;

import com.mymap.mymap.exception.BusinessException;
import com.mymap.mymap.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParamsServiceImpl implements ParamsService{
    private final EntityManager entityManager;
    private final MarkerClusterRepository markerClusterRepository;
    private final FilteredBusRepository filteredBusRepository;

    @Override
    @Transactional
    public void createMarkerCluster(List<MarkerClusterDTO> lists) {
        List<MarkerCluster> entities = lists.stream()
                .map(dto -> new MarkerCluster(dto.getNo(), dto.getJourneyNo(), dto.getClusterName(), dto.getClusterBus(), dto.getClusterSub(), dto.getClusterBike()))
                .collect(Collectors.toList());
        markerClusterRepository.saveAll(entities);
        entityManager.flush();
        entityManager.clear();
    }

    @Override
    public String findByArsId(long jno, String arsId) {
        return markerClusterRepository.findByJno(jno, arsId)
                .orElseThrow(()->new BusinessException(ErrorCode.NOT_REGISTERED));
    }

    @Override
    @Transactional
    public void createFilteredBus(List<FilteredBusDTO> lists) {
        List<FilteredBus> entities = lists.stream()
                .map(dto-> new FilteredBus(dto.getNo(),dto.getJourneyNo(),dto.getClusterName(),dto.getArsId(),dto.getRoutes()))
                .collect(Collectors.toList());
        filteredBusRepository.saveAll(entities);
        entityManager.flush();
        entityManager.clear();
    }


}
