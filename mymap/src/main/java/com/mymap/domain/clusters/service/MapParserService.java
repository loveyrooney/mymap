package com.mymap.domain.clusters.service;

import java.util.Map;

public interface MapParserService {
    /**
     * 공유 URL을 분석하여 지오메트리 및 정류장 노드 리스트를 반환합니다.
     */
    Map<String, Object> parseAndLogRoute(String sharedUrl);
}
