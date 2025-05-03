package com.mymap.mymap;

import com.mymap.domain.SubwayRepository;
import com.mymap.domain.clusters.ClustersService;
import com.mymap.domain.clusters.dto.MarkerClusterDTO;
import com.mymap.domain.geoms.MarkerDTO;
import com.mymap.domain.user.UserRepository;
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
    private SubwayRepository subwayRepository;
    @Test
    public void test(){
//        Optional<User> rooney = userRepository.findByUserId("rooney");
//        System.out.println(rooney.get().getPassword());
        List<MarkerClusterDTO> lists = clustersService.findMarkerClusterByJno(1L);
        List<MarkerDTO> geoms = clustersService.findGeoms(lists, 2L);
        for(MarkerDTO dto : geoms) {
            System.out.println(dto.getClusterName() + dto.getStName() + dto.getGeom());
        }
//        Pageable page = PageRequest.of(0, 1, Sort.by("no").ascending());
//        List<MarkerDTO> m = subwayRepository.findByStName("서울",page);
//        System.out.println(m.get(0).getGeom());
    }
}
