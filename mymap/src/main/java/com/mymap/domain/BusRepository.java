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
}