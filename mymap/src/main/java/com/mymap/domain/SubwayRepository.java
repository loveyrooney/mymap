package com.mymap.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubwayRepository extends JpaRepository<Subway,Long> {
    @Query(value = "SELECT * FROM cluster_grouping(:jno)", nativeQuery = true)
    Optional<List<Object[]>> getClusterGrouping(@Param("jno") Long jno);

    @Query(" select s from Subway s where s.stationName = :clusterName ")
    Page<Subway> findByStName(@Param("clusterName") String clusterName, Pageable pageable);

    @Query(value = " select * from near_sub_geoms(:lon,:lat) ",nativeQuery = true)
    Optional<List<Object[]>> getNearGeoms(Double lon, Double lat);
}

