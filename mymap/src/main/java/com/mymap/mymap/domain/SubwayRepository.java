package com.mymap.mymap.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubwayRepository extends JpaRepository<Subway,Long> {
    @Query(value = "SELECT * FROM cluster_grouping(:uno)", nativeQuery = true)
    List<Object[]> getClusterGrouping(@Param("uno") Long uno);
}

