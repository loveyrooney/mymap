package com.mymap.domain.geoms;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FromToGeomRepository extends JpaRepository<FromToGeom,Long> {
    @Query(" select f from FromToGeom f where f.userNo = :auth and f.name = :clusterName ")
    Optional<FromToGeom> findByUserNoAndName(@Param("auth") Long auth, @Param("clusterName") String clusterName);

    //@Query(" delete from FromToGeom f where f.userNo = :userNo and f.name = :name ")
    void deleteByUserNoAndName(@Param("userNo") long userNo, @Param("name") String name);
}
