package com.mymap.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BikeRepository extends JpaRepository<Bike,Long> {
    @Query(" select b from Bike b where b.stationId = :s ")
    Optional<Bike> findByStId(String s);
}
