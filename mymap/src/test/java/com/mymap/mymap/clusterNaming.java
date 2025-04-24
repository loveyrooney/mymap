package com.mymap.mymap;

import com.mymap.mymap.domain.*;
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
                String clusterName = createClusterNameHasCid(o);
                System.out.printf("%s, %s, %s, %d",(String)o[0],(String)o[1],clusterName,(int)o[3]);
                System.out.println();
            }
        }

    }

    @Test
    public void partString(){
        List<Object[]> clusters = subway.getClusterGrouping(2L);
        // ex) o[0] = "bus", o[1] = "02006", o[2] = "서울역버스환승센터", o[3] = 0 (cluster_id)
        int clusterId = 0;
        String clusterName = createClusterNameHasCid(clusters.get(0));
        int idx = 0;
        List<String> subKeySet = new ArrayList<>();
        List<String> busKeySet = new ArrayList<>();
        List<String> bikeKeySet = new ArrayList<>();
        for(int i=0; i<clusters.size(); i++){
            if(clusters.get(i)[3]==null){
                putClusterSet(clusterName,subKeySet,busKeySet,bikeKeySet);
                idx = i;
                break;
            } else {
                if((int)clusters.get(i)[3] != clusterId){
                    putClusterSet(clusterName,subKeySet,busKeySet,bikeKeySet);
                    clusterId = (int) clusters.get(i)[3];
                    clusterName = createClusterNameHasCid(clusters.get(i));
                }
                // 클러스터 디티오를 만들 필요가 없음. 클러스터 네임이 바뀌는 단위로 그 id 배열들을 만들어 놔야함.
                if("subway".equals(clusters.get(i)[0]))
                    subKeySet.add((String)clusters.get(i)[1]);
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
        clusterKeySet.forEach((k,v)->
                    v.forEach((vk,vv)->{
                        System.out.printf("%s, %s, %s",k,vk,vv);
                        System.out.println();
                    })
                );


    }

    public String createClusterNameHasCid(Object[] cluster){
        String clusterName = "";
        if("subway".equals(cluster[0]))
            clusterName = (String) cluster[2];
        else if ("bus".equals(cluster[0]))
            clusterName = region.findRegionNameByBus((String) cluster[1]); //지하철이 없는 클러스터의 경우 행정경계 조회
        else
            clusterName = region.findRegionNameByBike((String) cluster[1]);
        return clusterName;
    }

    public void putClusterSet(String clusterName, List<String> subKeySet, List<String> busKeySet,List<String> bikeKeySet){
        List<String> subs = List.copyOf(subKeySet);
        List<String> buses = List.copyOf(busKeySet);
        List<String> bikes = List.copyOf(bikeKeySet);
        Map<String,List<String>> map = new HashMap<>();
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
        String clusterName = null;
        if("bus".equals(cluster[0]))
            clusterName = journey.containsWhereBus(jno,(String)cluster[1]);
        else if ("subway".equals(cluster[0]))
            clusterName = journey.containsWhereSub(jno,(String)cluster[1]);
        else
            clusterName = journey.containsWhereBike(jno,(String)cluster[1]);
        if(clusterName==null)
            clusterName = (String)cluster[2];
        clusterKeySet.computeIfAbsent(clusterName, k -> new HashMap<>())
                .computeIfAbsent((String)cluster[0], k -> new ArrayList<>())
                .add((String)cluster[1]);
    }

}
