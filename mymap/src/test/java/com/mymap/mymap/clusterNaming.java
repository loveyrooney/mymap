package com.mymap.mymap;

import com.mymap.mymap.domain.*;
import com.mymap.mymap.domain.clusters.repository.JourneyRepository;
import com.mymap.mymap.domain.clusters.dto.MarkerClusterDTO;
import com.mymap.mymap.domain.clusters.ClustersService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

@SpringBootTest
public class clusterNaming {
    @Autowired
    private BikeRepository bike;
    @Autowired
    private BusRepository bus;
    @Autowired
    private SubwayRepository subway;
    @Autowired
    private RegionRepository region;
    @Autowired
    private JourneyRepository journey;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ClustersService clustersService;

    Map<String,Map<String,List<String>>> clusterKeySet = new HashMap<>();

    @Test
    public void test(){
//        String regionName = region.findRegionNameByBike("99456");
//        System.out.println(regionName);

        // 지하철역이 없는 클러스터 이름 짓기 테스트
        // geom 데이터를 가져와서 곧장 그걸로 폴리곤 비교하려고 했는데 좌표계 변환이 안먹힘. db에서 찾도록 하는 게 낫겠음.
        String sql = "WITH all_points AS (\n" +
                "  SELECT 'bus' AS source, arsid as id, ST_Transform(geom, 3857) AS geom \n" +
                "  FROM bus\n" +
                ")  \n" +
                "SELECT\n" +
                "  source,id,\n" +
                "  ST_AsText(geom) AS geom_wkt,\n" +
                "  ST_ClusterDBSCAN(geom, eps := 350, minpoints := 2) OVER () AS cluster_id\n" +
                "FROM all_points\n" +
                "order by cluster_id;";
        Query nativeQuery = entityManager.createNativeQuery(sql);
        List<Object[]> resultList = nativeQuery.getResultList();
        for(Object[] o:resultList){
            if(o[3]!=null){
                String clusterName = createClusterNameHasCid(o)[0];
                System.out.printf("%s, %s, %s, %d",(String)o[0],(String)o[1],clusterName,(int)o[3]);
                System.out.println();
            }
        }

    }

    @Test
    public void partString(){
        List<Object[]> clusters = subway.getClusterGrouping(1L);
        // ex) o[0] = "bus", o[1] = "02006", o[2] = "서울역버스환승센터", o[3] = 0 (cluster_id)
        int clusterId = 0;
        String[] cluster = createClusterNameHasCid(clusters.get(0));
        int idx = 0;
        Set<String> subKeySet = new HashSet<>();
        List<String> busKeySet = new ArrayList<>();
        List<String> bikeKeySet = new ArrayList<>();
        for(int i=0; i<clusters.size(); i++){
            if(clusters.get(i)[3]==null){
                putClusterSet(cluster[0],cluster[1],subKeySet,busKeySet,bikeKeySet);
                idx = i;
                break;
            } else {
                if((int)clusters.get(i)[3] != clusterId){
                    putClusterSet(cluster[0],cluster[1],subKeySet,busKeySet,bikeKeySet);
                    clusterId = (int) clusters.get(i)[3];
                    cluster = createClusterNameHasCid(clusters.get(i));
                }
                // 클러스터 디티오를 만들 필요가 없음. 클러스터 네임이 바뀌는 단위로 그 id 배열들을 만들어 놔야함.
                if("subway".equals(clusters.get(i)[0]))
                    subKeySet.add((String)clusters.get(i)[2]);
                else if("bus".equals(clusters.get(i)[0]))
                    busKeySet.add((String)clusters.get(i)[1]);
                else
                    bikeKeySet.add((String)clusters.get(i)[1]);
            }
        }

        // 여기서는 얘가 사용자가 지정한 출발지인지, 도착지인지를 찾아서 그 이름으로 클러스터네임 지정
        // 출발지도 도착지도 아닌 경우 얘 정류장 이름으로 클러스터네임 지정
        for(int i=idx; i<clusters.size(); i++){
            createClusterNameHasNotCid(1L,clusters.get(i));
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
            dto.setJourneyNo(1);
            lists.add(dto);
            System.out.printf("%s, %s, %s, %s, %s, %s, %s, %s",k,"bus",v.get("bus"),"subway",v.get("subway"),"bike",v.get("bike"),v.get("geom_t"));
            System.out.println();
        }
//        clusterKeySet.forEach((k,v)->{
//
//        });
        clustersService.createMarkerCluster(lists);


    }

    public String[] createClusterNameHasCid(Object[] cluster){
        String[] clusterName = new String[2];
        if("subway".equals(cluster[0])){
            clusterName[0] = (String) cluster[2];
            clusterName[1] = "subway";
        } else if ("bus".equals(cluster[0])){
            clusterName[0] = region.findRegionNameByBus((String) cluster[1]); //지하철이 없는 클러스터의 경우 행정경계 조회
            clusterName[1] = "buses";
        } else {
            clusterName[0] = region.findRegionNameByBike((String) cluster[1]);
            clusterName[1] = "bikes";
        }
        return clusterName;
    }

    public void putClusterSet(String clusterName, String geomTable, Set<String> subKeySet, List<String> busKeySet,List<String> bikeKeySet){
        List<String> subs = List.copyOf(subKeySet);
        List<String> buses = List.copyOf(busKeySet);
        List<String> bikes = List.copyOf(bikeKeySet);
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

    public void createClusterNameHasNotCid(long jno, Object[] cluster){
        String[] clusterInfo = new String[2];
        if("bus".equals(cluster[0]))
            clusterInfo[0] = journey.containsWhereBus(jno,(String)cluster[1]);
        else if ("subway".equals(cluster[0]))
            clusterInfo[0] = journey.containsWhereSub(jno,(String)cluster[2]);
        else
            clusterInfo[0] = journey.containsWhereBike(jno,(String)cluster[1]);
        if(clusterInfo[0]==null){
            clusterInfo[0] = (String)cluster[2];
            clusterInfo[1] = (String) cluster[0];
        } else {
            clusterInfo[1] = "from_to_geo";
        }
//        clusterKeySet.computeIfAbsent(clusterName, k -> new HashMap<>())
//                .computeIfAbsent((String)cluster[0], k -> new ArrayList<>())
//                .add((String)cluster[1]);
        Map<String, List<String>> innerMap = clusterKeySet.computeIfAbsent(clusterInfo[0], k -> new HashMap<>());
        innerMap.putIfAbsent("geom_t",List.of(clusterInfo[1]));
        List<String> list = innerMap.computeIfAbsent((String) cluster[0], k -> new ArrayList<>());
        if("subway".equals(cluster[0])) {
            list.add((String) cluster[2]);
        } else {
            list.add((String) cluster[1]);
        }
    }

}
