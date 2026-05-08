package com.mymap.domain.clusters.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
// @Primary
@Service
public class DefaultMapParserService implements MapParserService {

    @Override
    public Map<String, Object> parseAndLogRoute(String sharedUrl) {
        log.info("DefaultMapParserService: Parsing is not implemented in this version.");
        Map<String, Object> result = new HashMap<>();
        result.put("error", "Service not implemented. Please provide a valid implementation.");
        return result;
    }
}
