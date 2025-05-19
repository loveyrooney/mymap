package com.mymap.domain.clusters.service;

import com.mymap.domain.*;
import com.mymap.domain.clusters.dto.ClusterMsgDTO;
import com.mymap.domain.clusters.dto.FilteredBusDTO;
import com.mymap.domain.clusters.dto.JourneyDTO;
import com.mymap.domain.clusters.dto.MarkerClusterDTO;
import com.mymap.domain.clusters.entity.*;
import com.mymap.domain.clusters.repository.FilteredBusRepository;
import com.mymap.domain.clusters.repository.JourneyRepository;
import com.mymap.domain.clusters.repository.MarkerClusterRepository;
import com.mymap.domain.geoms.GeomService;
import com.mymap.exception.BusinessException;
import com.mymap.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClustersServiceImpl implements ClustersService {
    private final EntityManager entityManager;
    private final MarkerClusterRepository markerClusterRepository;
    private final FilteredBusRepository filteredBusRepository;
    private final JourneyRepository journeyRepository;
    private final SubwayRepository subwayRepository;
    private final RegionRepository regionRepository;
    private final GeomService geomService;
    private final Map<String,Map<String,List<String>>> clusterKeySet = new HashMap<>();


    @Override
    @Transactional
    public long createJourney(JourneyDTO dto) {
        Journey entity = Journey.builder()
                .userNo(dto.getUserNo()).fromName(dto.getFromName()).toName(dto.getToName()).fromBus(dto.getFromBus()).tfBus(dto.getTfBus()).toBus(dto.getToBus()).fromSub(dto.getFromSub()).tfSub(dto.getTfSub()).toSub(dto.getToSub()).fromBike(dto.getFromBike()).tfBike(dto.getTfBike()).toBike(dto.getToBike()).direction(dto.getDirection())
                .build();
        Journey save = journeyRepository.save(entity);
        return save.getNo();
    }

    @Override
    @Transactional
    public void updateJourney(JourneyDTO dto) {
        Journey journey = journeyRepository.findByNo(dto.getNo())
                .orElseThrow(()->new BusinessException(ErrorCode.NOT_EXIST));
        try {
            Class<?> dtoClass = dto.getClass();
            Class<?> entityClass = journey.getClass();

            for (Field dtoField : dtoClass.getDeclaredFields()) {
                dtoField.setAccessible(true);
                Object newValue = dtoField.get(dto);

                if (newValue != null) {
                    try {
                        Field entityField = entityClass.getDeclaredField(dtoField.getName());
                        entityField.setAccessible(true);
                        Object oldValue = entityField.get(journey);

                        if (!newValue.equals(oldValue)) {
                            entityField.set(journey, newValue);
                        }
                    } catch (NoSuchFieldException ignored) {
                        // DTO에만 있는 필드일 수 있으므로 무시
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new BusinessException(ErrorCode.JOURNEY_UPDATE_FAILED);
        }
    }

    @Override
    @Transactional
    public void deleteJourney(long no) {
        journeyRepository.deleteByNo(no);
    }

    @Override
    @Transactional
    public void deleteMarkerCluster(long no) {
        markerClusterRepository.deleteAllByJourneyNo(no);
    }

    @Override
    @Transactional
    public void deleteFilteredBus(long no) {
        filteredBusRepository.deleteAllByJourneyNo(no);
    }

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
    @Transactional
    public List<MarkerClusterDTO> abstractCluster(long journeyNo){
        List<Object[]> clusters = subwayRepository.getClusterGrouping(journeyNo);
        // ex) o[0] = "bus", o[1] = "02006", o[2] = "서울역버스환승센터", o[3] = 0 (cluster_id)
        int clusterId = 0;
        String[] cluster = createClusterNameHasCid(clusters.get(0));
        int hasNotCidStartIdx = 0;
        Set<String> subKeySet = new HashSet<>();
        List<String> busKeySet = new ArrayList<>();
        List<String> bikeKeySet = new ArrayList<>();
        // 클러스터 id 가 있는 경우의 클러스터 네이밍
        for(int i=0; i<clusters.size(); i++){
            if(clusters.get(i)[3]==null){
                putClusterSet(cluster[0],cluster[1],subKeySet,busKeySet,bikeKeySet);
                hasNotCidStartIdx = i;
                break;
            } else {
                if((int)clusters.get(i)[3] != clusterId){
                    putClusterSet(cluster[0],cluster[1],subKeySet,busKeySet,bikeKeySet);
                    clusterId = (int) clusters.get(i)[3];
                    cluster = createClusterNameHasCid(clusters.get(i));
                }
                if("subway".equals(clusters.get(i)[0]))
                    subKeySet.add((String)clusters.get(i)[2]);
                else if("bus".equals(clusters.get(i)[0]))
                    busKeySet.add((String)clusters.get(i)[1]);
                else
                    bikeKeySet.add((String)clusters.get(i)[1]);
            }
        }

        // 여기서는 얘가 사용자가 지정한 출발지인지, 도착지인지를 찾아서 그 이름으로 클러스터네임 지정
        // 출발지도 도착지도 아닌 경우 정류장 이름으로 클러스터네임 지정
        for(int i=hasNotCidStartIdx; i<clusters.size(); i++){
            createClusterNameHasNotCid(journeyNo,clusters.get(i));
        }

        List<MarkerClusterDTO> lists = new ArrayList<>();
        Iterator<String> iterator1 = clusterKeySet.keySet().iterator();
        while(iterator1.hasNext()){
            String k = iterator1.next();
            Map<String,List<String>> v = clusterKeySet.get(k);
            MarkerClusterDTO dto = new MarkerClusterDTO();
            if(v.get("bus")!=null)
                dto.setClusterBus(v.get("bus").toArray(new String[0]));
            if(v.get("bike")!=null)
                dto.setClusterBike(v.get("bike").toArray(new String[0]));
            if(v.get("subway")!=null)
                dto.setClusterSub(v.get("subway").toArray(new String[0]));
            dto.setClusterName(k);
            dto.setGeomTable(v.get("geom_t").get(0));
            dto.setJourneyNo(journeyNo);
            lists.add(dto);
            //System.out.printf("%s, %s, %s, %s, %s, %s, %s, %s",k,"bus",v.get("bus"),"subway",v.get("subway"),"bike",v.get("bike"),v.get("geom_t"));
            //System.out.println();
        }
        return lists;
    }

    public String[] createClusterNameHasCid(Object[] cluster){
        String[] clusterName = new String[2];
        if("subway".equals(cluster[0])){
            clusterName[0] = (String) cluster[2];
            clusterName[1] = "subway";
        } else if ("bus".equals(cluster[0])){
            clusterName[0] = regionRepository.findRegionNameByBus((String) cluster[1]); //지하철이 없는 클러스터의 경우 행정경계 조회
            clusterName[1] = "buses";
        } else {
            clusterName[0] = regionRepository.findRegionNameByBike((String) cluster[1]);
            clusterName[1] = "bikes";
        }
        return clusterName;
    }

    private void putClusterSet(String clusterName, String geomTable, Set<String> subKeySet, List<String> busKeySet,List<String> bikeKeySet){
        List<String> subs = new ArrayList<>(List.copyOf(subKeySet));
        List<String> buses = new ArrayList<>(List.copyOf(busKeySet));
        List<String> bikes = new ArrayList<>(List.copyOf(bikeKeySet));
        List<String> geom = new ArrayList<>();
        geom.add(geomTable);
        Map<String,List<String>> map = new HashMap<>();
        map.put("geom_t",geom);
        //System.out.println("geom_t:"+map.get("geom_t"));
        if(subKeySet.size()>0)
            map.put("subway",subs);
        if(busKeySet.size()>0)
            map.put("bus",buses);
        if(bikeKeySet.size()>0)
            map.put("bike",bikes);
        clusterKeySet.put(clusterName,map);
        subKeySet.clear();
        busKeySet.clear();
        bikeKeySet.clear();
    }

    private void createClusterNameHasNotCid(long jno, Object[] cluster){
        String[] clusterInfo = new String[2];
        if("bus".equals(cluster[0]))
            clusterInfo[0] = journeyRepository.containsWhereBus(jno,(String)cluster[1]);
        else if ("subway".equals(cluster[0]))
            clusterInfo[0] = journeyRepository.containsWhereSub(jno,(String)cluster[2]);
        else
            clusterInfo[0] = journeyRepository.containsWhereBike(jno,(String)cluster[1]);
        if(clusterInfo[0]==null){
            clusterInfo[0] = (String)cluster[2];
            clusterInfo[1] = (String) cluster[0];
        } else {
            clusterInfo[1] = "from_to_geo";
        }
        System.out.println("clusterInfo: "+clusterInfo[0]+","+clusterInfo[1]);
//        clusterKeySet.computeIfAbsent(clusterName, k -> new HashMap<>())
//                .computeIfAbsent((String)cluster[0], k -> new ArrayList<>())
//                .add((String)cluster[1]);
        Map<String, List<String>> innerMap = clusterKeySet.computeIfAbsent(clusterInfo[0], k -> new HashMap<>());
        innerMap.putIfAbsent("geom_t",new ArrayList<>(List.of(clusterInfo[1])));
        List<String> list = innerMap.computeIfAbsent((String) cluster[0], k -> new ArrayList<>());
        if("subway".equals(cluster[0])) {
            list.add((String) cluster[2]);
        } else {
            list.add((String) cluster[1]);
        }
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
    public String findByArsId(long jno, String arsId) {
        return markerClusterRepository.findClusterNameByJno(jno, arsId)
                .orElseThrow(()->new BusinessException(ErrorCode.NOT_REGISTERED));
    }

    @Override
    public List<JourneyDTO> findJourneyAllByUserNo(Long principal) {
        return journeyRepository.findAllByUserNo(principal)
                .orElseThrow(()->new BusinessException(ErrorCode.NOT_REGISTERED));
    }

    @Override
    public Journey findJourneyByNo(Long journeyNo) {
        return journeyRepository.findByNo(journeyNo)
                .orElseThrow(()->new BusinessException(ErrorCode.NOT_REGISTERED));
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
    public Map<String,ClusterMsgDTO> convertToClusterMsg(List<MarkerClusterDTO> clusterList, long jno, String direction) {
        Map<String,ClusterMsgDTO> map = new HashMap<>();
        for(MarkerClusterDTO dto : clusterList){
            ClusterMsgDTO msg = new ClusterMsgDTO();
            msg.setClusterName(dto.getClusterName());
            if(dto.getClusterBus()!=null){
                Map<String,String[]> busMap = new HashMap<>();
                for(String arsid : dto.getClusterBus()){
                    FilteredBus filteredBus = filteredBusRepository.findByJnoAndArsIdAndClusterName(jno,arsid,dto.getClusterName())
                            .orElseThrow(()-> new BusinessException(ErrorCode.NOT_EXIST));
                    busMap.putIfAbsent(arsid, filteredBus.getRoutes());
                }
                msg.setBus(busMap);
            }
            if(dto.getClusterSub()!=null){
                msg.setSub(dto.getClusterSub());
                msg.setDirection(direction);
            }
            if(dto.getClusterBike()!=null)
                msg.setBike(dto.getClusterBike());
            map.putIfAbsent(dto.getClusterName(),msg);
        }
        return map;
    }


}
