package com.mymap;

import java.util.*;

public class RouteGraph {

    private Map<String, Set<String>> outEdges = new HashMap<>();
    private Map<String, Set<String>> inEdges = new HashMap<>();

    public void addEdge(String from, String to) {
        outEdges.computeIfAbsent(from, k -> new HashSet<>()).add(to);
        inEdges.computeIfAbsent(to, k -> new HashSet<>()).add(from);
    }

    public int countFanOut(String node) {
        Set<String> outList = outEdges.getOrDefault(node, Collections.emptySet());
        return outList.size();  // 연결된 노드 개수
    }

    public int countFanIn(String node) {
        Set<String> inList = inEdges.getOrDefault(node, Collections.emptySet());
        return inList.size();
    }

    public Set<String> findFanOutAdjNodes(String node) {
        return outEdges.get(node);
    }

    public Set<String> findFanInAdjNodes(String node) {
        return inEdges.get(node);
    }

    public Map<String, Set<String>> getOutEdges() {
        return outEdges;
    }

    public Map<String, Set<String>> getInEdges() {
        return inEdges;
    }
}
