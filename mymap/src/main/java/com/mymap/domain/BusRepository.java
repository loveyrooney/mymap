package com.mymap.domain;

import com.mymap.domain.geoms.TransferDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BusRepository extends JpaRepository<Bus,Long> {
    @Query(" select b from Bus b where b.arsId = :arsId ")
    Optional<Bus> findByArsId(String arsId);

    @Query(value = " select * from near_bus_geoms(:lon,:lat) ",nativeQuery = true)
    Optional<List<Object[]>> getNearGeoms(Double lon, Double lat);
}