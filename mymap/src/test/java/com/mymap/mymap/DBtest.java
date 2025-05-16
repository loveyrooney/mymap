package com.mymap.mymap;

import com.mymap.domain.SubwayRepository;
import com.mymap.domain.clusters.dto.JourneyDTO;
import com.mymap.domain.clusters.entity.Journey;
import com.mymap.domain.clusters.service.BusFilterService;
import com.mymap.domain.clusters.service.ClustersService;
import com.mymap.domain.clusters.dto.MarkerClusterDTO;
import com.mymap.domain.geoms.GeomService;
import com.mymap.domain.geoms.MarkerDTO;
import com.mymap.domain.user.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class DBtest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ClustersService clustersService;
    @Autowired
    private BusFilterService busFilterService;
    @Autowired
    private SubwayRepository subwayRepository;
    @Autowired
    private GeomService geomService;

    @Test
    public void test(){
//        Optional<User> rooney = userRepository.findByUserId("rooney");
//        System.out.println(rooney.get().getPassword());
        List<MarkerClusterDTO> lists = clustersService.findMarkerClusterByJno(1L);
        List<MarkerDTO> geoms = geomService.findGeoms(lists, 1L);
//        for(MarkerDTO dto : geoms) {
//            System.out.println(dto.getClusterName() + dto.getStName() );
//        }
//        Pageable page = PageRequest.of(0, 1, Sort.by("no").ascending());
//        List<MarkerDTO> m = subwayRepository.findByStName("서울",page);
//        System.out.println(m.get(0).getGeom());
    }

    @Test
    public void journeyInsert (){
        JourneyDTO dto = JourneyDTO.builder()
                        .fromName("집").toName("천재")
                .fromBus(new String[]{"13550","13168","13169"}).tfBus(new String[]{"01126"})
                .tfSub(new String[]{"홍제","서울","종각","시청"}).toSub(new String[]{"가산디지털단지"})
                        .userNo(2L).build();
        clustersService.createJourney(dto);
    }

    @Test
    @Transactional
    public void deleteTest() {
        Journey journey = clustersService.findJourneyByNo(3L);
        JourneyDTO dto = JourneyDTO.builder()
                        .no(journey.getNo()).userNo(journey.getUserNo())
                        .fromName(journey.getFromName()).toName(journey.getToName())
                        .build();
        System.out.println(dto.getNo());
        clustersService.deleteJourney(dto.getNo());
        geomService.deleteFromToGeoms(dto);
        clustersService.deleteMarkerCluster(dto.getNo());
        clustersService.deleteFilteredBus(dto.getNo());
    }

    @Test
    @Transactional
    public void updateTest(){
        Journey journey = clustersService.findJourneyByNo(3L);
        JourneyDTO dto = JourneyDTO.builder()
                .no(journey.getNo()).userNo(journey.getUserNo())
                .fromName(journey.getFromName()).toName("chunjae")
                .fromBike(journey.getFromBike()).tfBike(journey.getTfBike()).toBike(new String[]{"ST-100","ST-200"})
                .fromBus(journey.getFromBus()).tfBus(journey.getTfBus()).toBus(journey.getToBus())
                .fromSub(journey.getFromSub()).tfSub(journey.getTfSub()).toSub(journey.getToSub())
                .build();
        clustersService.updateJourney(dto);
    }
}
