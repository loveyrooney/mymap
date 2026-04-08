package com.mymap.domain.clusters.cache;

import com.mymap.domain.Bus;
import com.mymap.domain.BusRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Log4j2
@Component
@RequiredArgsConstructor
public class GlobalStationCache {

    private final BusRepository busRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String STATION_MAP_KEY = "station:mapping";

    @PostConstruct
    public void init() {
        log.info("🚀 Warming up Redis Station Cache...");
        List<Bus> allBuses = busRepository.findAll();
        
        // 역방향 매핑도 필요할 수 있으므로 station_id -> ars_id 저장
        // Redis Hash 구조를 사용하여 효율적으로 관리합니다.
        allBuses.forEach(bus -> {
            if (bus.getStationId() != null && bus.getArsId() != null) {
                redisTemplate.opsForHash().put(STATION_MAP_KEY, bus.getStationId(), bus.getArsId());
            }
        });
        
        log.info("✅ {} filtered station mappings loaded into Redis.", allBuses.size());
    }

    /**
     * station_id를 입력받아 arsid를 반환합니다.
     */
    public String getArsId(String stationId) {
        Object arsId = redisTemplate.opsForHash().get(STATION_MAP_KEY, stationId);
        return arsId != null ? arsId.toString() : null;
    }
}
