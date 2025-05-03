package com.mymap.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RegionRepository extends JpaRepository<Region,Integer> {
    @Query("select r.admNm from Region r where ST_Intersects(r.geom, (select b.geom from Bus b where arsId = :id))")
    String findRegionNameByBus(String id);

    @Query("select r.admNm from Region r where ST_Intersects(r.geom, (select b.geom from Bike b where stationId = :id))")
    String findRegionNameByBike(String id);
}
