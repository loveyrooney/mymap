package com.mymap.domain.clusters;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.mymap.domain.BusRepository;
import com.mymap.domain.Bus;

@Getter @Setter
public class BusRouteFilterUtil {
    Map<String,Set<String>> routes = new ConcurrentHashMap<>();
    Map<String, Set<String>> groups = new ConcurrentHashMap<>();
    Map<Integer,Set<String>> depths = new ConcurrentHashMap<>();
    RouteGraph graph = new RouteGraph();
    Set<String> validDepth2Routes = new HashSet<>();
    Set<String> validDepth3Routes = new HashSet<>();
    private final BusRepository busRepository;

    public BusRouteFilterUtil(BusRepository busRepository) {
        this.busRepository = busRepository;
    }

    public void reset() {
        routes.clear();
        groups.clear();
        depths.clear();
        graph = new RouteGraph();
        validDepth2Routes.clear();
        validDepth3Routes.clear();
    }

    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371e3; // metres
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double deltaPhi = Math.toRadians(lat2 - lat1);
        double deltaLambda = Math.toRadians(lon2 - lon1);
        double a = Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2) +
                Math.cos(phi1) * Math.cos(phi2) *
                Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; 
    }

    public List<List<String>> edgeSearch(Set<String> dp, Set<String> ar, String dk, String ak){
        List<List<String>> freepath = new ArrayList<>();
        if (dp == null || ar == null) return freepath;
        for(String route : dp){
            if(ar.contains(route)){
                Integer startSeq = busRepository.getSeq(dk, route).orElse(null);
                Integer endSeq = busRepository.getSeq(ak, route).orElse(null);
                
                if (startSeq != null && endSeq != null && endSeq > startSeq) {
                    int edgeLength = endSeq - startSeq;
                    Bus startBus = busRepository.findByStationId(dk).orElse(null);
                    Bus endBus = busRepository.findByStationId(ak).orElse(null);
                    
                    if (startBus != null && endBus != null) {
                        double straightLength = calculateDistance(
                            startBus.getGeom().getY(), startBus.getGeom().getX(),
                            endBus.getGeom().getY(), endBus.getGeom().getX()
                        );
                        double standardLength = edgeLength * 500.0;
                        
                        boolean isValid = true;
                        
                        if (standardLength > straightLength) {
                            List<Object[]> nearNodes = busRepository.getNearGeoms(startBus.getGeom().getX(), startBus.getGeom().getY()).orElse(null);
                            boolean hasLoopPair = false;
                            
                            if (nearNodes != null && !nearNodes.isEmpty()) {
                                for (Object[] node : nearNodes) {
                                    if (node.length >= 3) {
                                        String pairStId = (String) node[1];
                                        if (pairStId.equals(dk)) continue;
                                        
                                        Integer pairSeq = busRepository.getSeq(pairStId, route).orElse(null);
                                        if (pairSeq != null && pairSeq > startSeq && pairSeq < endSeq) {
                                            if (pairSeq - startSeq >= 3) {
                                                hasLoopPair = true;
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                            
                            if (hasLoopPair) {
                                isValid = false;
                            } else if ((standardLength - straightLength) > 5000.0) {
                                isValid = false;
                            }
                        }
                        
                        if (isValid) {
                            List<String> pass = new ArrayList<>();
                            pass.add(route);
                            pass.add(dk);
                            pass.add(ak);
                            graph.addEdge(dk,ak);
                            freepath.add(pass);
                        }
                    }
                }
            }
        }
        return freepath;
    }

    public List<List<String>> createPassList(Set<String> froms, Set<String> tos) {
        List<List<String>> passList = new ArrayList<>();
        for(String fromK : froms){
            for(String toK : tos){
                List<List<String>> pass = edgeSearch(routes.get(fromK),routes.get(toK),fromK,toK);
                if(pass.size()>0) {
                    passList.addAll(pass);
                }
            }
        }
        return passList;
    }

    public List<Set<String>> addPass(List<List<String>> passes){
        List<Set<String>> sortedList = new ArrayList<>();
        passes.forEach(pass->{
            for(int i=1; i<3; i++){
                routes.computeIfPresent(pass.get(i),(key,set)-> {
                    set.add(pass.get(0));
                    return set;
                });
                sortedList.add(routes.get(pass.get(i)));
            }
        });
        return sortedList;
    }

    public Set<String> sortRoutes(Set<String> fromRoutes, Set<String> adjToNodes,String direction){
        Set<String> sortedRoutes = new LinkedHashSet<>();
        for(String adjK : adjToNodes){
            Set<String> toRoutes = new LinkedHashSet<>();
            Set<String> adjToRoutes = routes.get(adjK);
            for(String route : fromRoutes){
                if(adjToRoutes.contains(route))
                    toRoutes.add(route);
            }
            if(toRoutes.size()>0) {
                sortedRoutes.addAll(toRoutes);
                fromRoutes.removeAll(toRoutes);
            }
        }
        if(fromRoutes.size()>0){
            sortedRoutes.addAll(fromRoutes);
            if("in".equals(direction)){
                List<String> list = new ArrayList<>(sortedRoutes);
                Collections.reverse(list);
                sortedRoutes = new LinkedHashSet<>(list);
            }
        }
        return sortedRoutes;
    }
}
