package com.mymap.domain;

import com.mymap.domain.geoms.MarkerDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BikeRepository extends JpaRepository<Bike,Long> {
    @Query(" select new com.mymap.domain.geoms.MarkerDTO(b.stationName, b.geom, b.stationId) from Bike b where b.stationId = :s ")
    Optional<MarkerDTO> findByStId(String s);
}
