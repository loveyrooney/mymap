package com.mymap.mymap.domain;

import com.mymap.mymap.domain.geoms.MarkerDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FromToGeomRepository extends JpaRepository<FromToGeom,Long> {
    @Query(" select new com.mymap.mymap.domain.geoms.MarkerDTO(:clusterName, f.name, f.geom) from FromToGeom f where f.userNo = :auth and f.name = :clusterName ")
    Optional<MarkerDTO> findByName(@Param("auth") Long auth, @Param("clusterName") String clusterName);
}
