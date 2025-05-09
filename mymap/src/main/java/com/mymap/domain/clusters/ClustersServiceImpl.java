package com.mymap.domain.clusters;

import com.mymap.domain.BikeRepository;
import com.mymap.domain.BusRepository;
import com.mymap.domain.FromToGeomRepository;
import com.mymap.domain.SubwayRepository;
import com.mymap.domain.clusters.dto.ClusterMsgDTO;
import com.mymap.domain.clusters.dto.FilteredBusDTO;
import com.mymap.domain.clusters.dto.JourneyDTO;
import com.mymap.domain.clusters.dto.MarkerClusterDTO;
import com.mymap.domain.clusters.entity.*;
import com.mymap.domain.clusters.repository.FilteredBusRepository;
import com.mymap.domain.clusters.repository.JourneyRepository;
import com.mymap.domain.clusters.repository.MarkerClusterRepository;
import com.mymap.domain.geoms.MarkerDTO;
import com.mymap.exception.BusinessException;
import com.mymap.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
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
                .map(entity-> FilteredBusDTO.builder()
                        .arsId(entity.getArsId())
                        .clusterName(entity.getClusterName())
                        .routes(entity.getRoutes())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<MarkerClusterDTO> findMarkerClusterByJno(long jno) {
        List<MarkerCluster> lists = markerClusterRepository.findByJno(jno)
                .orElseThrow(()->new BusinessException(ErrorCode.NOT_REGISTERED));
        return lists.stream()
                .map(entity-> MarkerClusterDTO.builder()
                        .clusterName(entity.getClusterName())
                        .clusterSub(entity.getClusterSub())
                        .clusterBus(entity.getClusterBus())
                        .clusterBike(entity.getClusterBike())
                        .geomTable(entity.getGeomTable())
                        .build())
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
                Object[] subs = subwayRepository.findByStName(dto.getClusterName(),page)
                        .getContent().stream().findFirst()
                        .orElseThrow(()->new BusinessException(ErrorCode.NOT_EXIST));
                m = MarkerDTO.builder()
                        .clusterName((String)subs[0])
                        .stName((String)subs[1])
                        .geom((String)subs[2].toString())
                        .build();
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

    @Override
    public Map<String,ClusterMsgDTO> convertToClusterMsg(List<MarkerClusterDTO> clusterList) {
        Map<String,ClusterMsgDTO> map = new HashMap<>();
        for(MarkerClusterDTO dto : clusterList){
            ClusterMsgDTO msg = new ClusterMsgDTO();
            msg.setClusterName(dto.getClusterName());
            if(dto.getClusterBus()!=null){
                Map<String,String[]> busMap = new HashMap<>();
                for(String arsid : dto.getClusterBus()){
                    FilteredBus filteredBus = filteredBusRepository.findByArsIdAndClusterName(arsid,dto.getClusterName())
                            .orElseThrow(()-> new BusinessException(ErrorCode.NOT_EXIST));
                    busMap.putIfAbsent(arsid, filteredBus.getRoutes());
                }
                msg.setBus(busMap);
            }
            if(dto.getClusterSub()!=null)
                msg.setSub(dto.getClusterSub());
            if(dto.getClusterBike()!=null)
                msg.setBike(dto.getClusterBike());
            map.putIfAbsent(dto.getClusterName(),msg);
        }
        return map;
    }

    @Override
    @Transactional
    public long createJourney(JourneyDTO dto) {
        Journey entity = Journey.builder()
                .userNo(dto.getUserNo()).fromName(dto.getFromName()).toName(dto.getToName()).fromBus(dto.getFromBus()).tfBus(dto.getTfBus()).toBus(dto.getToBus()).fromSub(dto.getFromSub()).tfSub(dto.getTfSub()).toSub(dto.getToSub()).fromBike(dto.getFromBike()).tfBike(dto.getTfBike()).toBike(dto.getToBike())
                .build();
        Journey save = journeyRepository.save(entity);

        return save.getNo();
    }

    private void callRoutes(){

    }


}
