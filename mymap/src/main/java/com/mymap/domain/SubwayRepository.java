package com.mymap.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubwayRepository extends JpaRepository<Subway,Long> {
    @Query(value = "SELECT * FROM cluster_grouping(:jno)", nativeQuery = true)
    Optional<List<Object[]>> getClusterGrouping(@Param("jno") Long jno);

    Optional<Subway> findFirstByStationNameOrderByNoAsc(String stationName);

    @Query(value = " select * from near_sub_geoms(:lon,:lat) ",nativeQuery = true)
    Optional<List<Object[]>> getNearGeoms(Double lon, Double lat);
}

