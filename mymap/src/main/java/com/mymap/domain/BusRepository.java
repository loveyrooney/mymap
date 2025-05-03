package com.mymap.domain;

import com.mymap.domain.geoms.MarkerDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BusRepository extends JpaRepository<Bus,Long> {
    @Query(" select new com.mymap.domain.geoms.MarkerDTO(b.stationName, b.geom, b.arsId) from Bus b where b.arsId = :arsId ")
    Optional<MarkerDTO> findByArsId(String arsId);
}