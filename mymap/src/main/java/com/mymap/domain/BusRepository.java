package com.mymap.domain;

import com.mymap.domain.geoms.TransferDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BusRepository extends JpaRepository<Bus,Long> {
    @Query(" select b from Bus b where b.stationId = :stId ")
    Optional<Bus> findByStationId(String stId);

    @Query(value = " select * from near_bus_geoms(:lon,:lat) ",nativeQuery = true)
    Optional<List<Object[]>> getNearGeoms(Double lon, Double lat);

    @Query(value = " select * from orphan_near_depth(:orpid,:depthid) ",nativeQuery = true)
    Optional<Boolean> orphan_near_depth(String orpid, String depthid);

    @Query(value = "SELECT ABS(s2.sta_ord - s1.sta_ord) FROM station_order s1 JOIN station_order s2 ON s1.route_id = s2.route_id WHERE s1.station_id = :startSt AND s2.station_id = :endSt AND s1.route_id = :routeId LIMIT 1", nativeQuery = true)
    Optional<Integer> findSeqDiff(String startSt, String endSt, String routeId);

    @Query(value = "SELECT sta_ord FROM station_order WHERE station_id = :stId AND route_id = :routeId LIMIT 1", nativeQuery = true)
    Optional<Integer> getSeq(String stId, String routeId);

    @Query(value = "WITH route_info AS (" +
            "    SELECT MAX(sta_ord) as max_ord, " +
            "           MIN(CASE WHEN sta_ord = 1 THEN station_id END) as first_st_id " +
            "    FROM station_order WHERE route_id = :routeId" +
            ") " +
            "SELECT b1.station_name AS currentSt, b2.station_name AS nextSt " +
            "FROM route_info r " +
            "JOIN bus b1 ON b1.station_id = :stId " +
            "JOIN station_order s2 ON s2.route_id = :routeId AND s2.sta_ord = (" +
            "    CASE WHEN :staOrder < r.max_ord THEN :staOrder + 1 " +
            "         ELSE CASE WHEN :stId = r.first_st_id THEN 2 ELSE 1 END " +
            "    END" +
            ") " +
            "JOIN bus b2 ON s2.station_id = b2.station_id LIMIT 1", nativeQuery = true)
    List<Object[]> findStationNames(String stId, String routeId, Integer staOrder);
}