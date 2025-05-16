package com.mymap.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BusRepository extends JpaRepository<Bus,Long> {
    @Query(" select b from Bus b where b.arsId = :arsId ")
    Optional<Bus> findByArsId(String arsId);
}