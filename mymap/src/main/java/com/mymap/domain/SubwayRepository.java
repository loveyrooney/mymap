package com.mymap.domain;

import com.mymap.domain.geoms.MarkerDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubwayRepository extends JpaRepository<Subway,Long> {
    @Query(value = "SELECT * FROM cluster_grouping(:uno)", nativeQuery = true)
    List<Object[]> getClusterGrouping(@Param("uno") Long uno);

    @Query(" select :clusterName, s.stationName, s.geom from Subway s where s.stationName = :clusterName ")
    Page<Object[]> findByStName(@Param("clusterName") String clusterName, Pageable pageable);
}

