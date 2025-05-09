package com.mymap.domain.clusters;

import com.mymap.RouteGraph;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter @Setter
@NoArgsConstructor
public class BusRouteFilterUtil {
    Map<String,Set<String>> routes = new ConcurrentHashMap<>();
    Map<String, Set<String>> groups = new ConcurrentHashMap<>();
    Map<Integer,Set<String>> depths = new ConcurrentHashMap<>();
    RouteGraph graph = new RouteGraph();

    public List<List<String>> edgeSearch(Set<String> dp, Set<String> ar, String dk, String ak){
        List<List<String>> freepath = new ArrayList<>();
        for(String route : dp){
            List<String> pass = new ArrayList<>();
            if(ar.contains(route)){
                pass.add(route);
                pass.add(dk);
                pass.add(ak);
                graph.addEdge(dk,ak);
            }
            if(pass.size()>0){
                freepath.add(pass);
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
                sortedList.add(routes.get(i));
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
