package com.mymap.mymap.domain.clusters;

import com.mymap.mymap.domain.BikeRepository;
import com.mymap.mymap.domain.BusRepository;
import com.mymap.mymap.domain.FromToGeomRepository;
import com.mymap.mymap.domain.SubwayRepository;
import com.mymap.mymap.domain.clusters.dto.FilteredBusDTO;
import com.mymap.mymap.domain.clusters.dto.MarkerClusterDTO;
import com.mymap.mymap.domain.clusters.entity.*;
import com.mymap.mymap.domain.clusters.repository.FilteredBusRepository;
import com.mymap.mymap.domain.clusters.repository.JourneyRepository;
import com.mymap.mymap.domain.clusters.repository.MarkerClusterRepository;
import com.mymap.mymap.domain.geoms.MarkerDTO;
import com.mymap.mymap.exception.BusinessException;
import com.mymap.mymap.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClustersServiceImpl implements ClustersService {
    private final EntityManager entityManager;
    private final MarkerClusterRepository markerClusterRepository;
    private final FilteredBusRepository filteredBusRepository;
    private final JourneyRepository journeyRepository;
    private final BusRepository busRepository;
    private final SubwayRepository subwayRepository;
    private final BikeRepository bikeRepository;
    private final FromToGeomRepository fromToGeomRepository;

    @Override
    @Transactional
    public void createMarkerCluster(List<MarkerClusterDTO> lists) {
        List<MarkerCluster> entities = lists.stream()
                .map(dto -> new MarkerCluster(dto.getNo(), dto.getJourneyNo(), dto.getClusterName(), dto.getGeomTable(),dto.getClusterBus(), dto.getClusterSub(), dto.getClusterBike()))
                .collect(Collectors.toList());
        markerClusterRepository.saveAll(entities);
        entityManager.flush();
        entityManager.clear();
    }

    @Override
    public String findByArsId(long jno, String arsId) {
        return markerClusterRepository.findClusterNameByJno(jno, arsId)
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

    @Override
    public List<Long> findAllByUserNo(Long principal) {
        List<Long> lists = journeyRepository.findByUserNo(principal)
                .orElseThrow(()->new BusinessException(ErrorCode.NOT_REGISTERED));
        return lists;
    }

    @Override
    public List<FilteredBusDTO> findFilterBusByJno(long jno) {
        List<FilteredBus> lists = filteredBusRepository.findByJno(jno)
                .orElseThrow(()->new BusinessException(ErrorCode.NOT_REGISTERED));
        return lists.stream()
                .map(entity-> new FilteredBusDTO(entity.getNo(),entity.getJourneyNo(),entity.getClusterName(), entity.getArsId(), entity.getRoutes()))
                .collect(Collectors.toList());
    }

    @Override
    public List<MarkerClusterDTO> findMarkerClusterByJno(long jno) {
        List<MarkerCluster> lists = markerClusterRepository.findByJno(jno)
                .orElseThrow(()->new BusinessException(ErrorCode.NOT_REGISTERED));
        return lists.stream()
                .map(entity-> new MarkerClusterDTO(entity.getNo(), entity.getJourneyNo(), entity.getClusterName(), entity.getGeomTable(), entity.getClusterBus(), entity.getClusterSub(), entity.getClusterBike()))
                .collect(Collectors.toList());
    }

    @Override
    public List<MarkerDTO> findGeoms(List<MarkerClusterDTO> clusters,Long auth) {
        List<MarkerDTO> markers = new ArrayList<>();
        for(MarkerClusterDTO dto : clusters){
            MarkerDTO m;
            if("from_to_geo".equals(dto.getGeomTable())){
                m = fromToGeomRepository.findByName(auth,dto.getClusterName())
                        .orElseThrow(()->new BusinessException(ErrorCode.NOT_EXIST));
            } else if("subway".equals(dto.getGeomTable())){
                Pageable page = PageRequest.of(0, 1, Sort.by("no").ascending());
                m = subwayRepository.findByStName(dto.getClusterName(),page)
                        .stream().findFirst()
                        .orElseThrow(()->new BusinessException(ErrorCode.NOT_EXIST));
            } else if("bus".equals(dto.getGeomTable()) || "buses".equals(dto.getGeomTable())){
                m = busRepository.findByArsId(dto.getClusterBus()[0])
                        .orElseThrow(()->new BusinessException(ErrorCode.NOT_EXIST));
                m.setClusterName(dto.getClusterName());
            } else if("bike".equals(dto.getGeomTable()) || "bikes".equals(dto.getGeomTable())){
                m = bikeRepository.findByStId(dto.getClusterBike()[0])
                        .orElseThrow(()->new BusinessException(ErrorCode.NOT_EXIST));
                m.setClusterName(dto.getClusterName());
            } else
                throw new BusinessException(ErrorCode.NOT_EXIST);
            markers.add(m);
        }
        return markers;
    }


}
