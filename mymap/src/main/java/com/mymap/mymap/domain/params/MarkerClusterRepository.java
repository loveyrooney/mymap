package com.mymap.mymap.domain.params;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MarkerClusterRepository extends JpaRepository<MarkerCluster,Long> {
    @Query(value = " select m.cluster_name " +
            " from marker_cluster m" +
            " where m.journey_no = :jno" +
            " and (:id = any(m.cluster_bike) " +
            " or :id = any(m.cluster_bus)" +
            " or :id = any(m.cluster_sub))",nativeQuery = true)
    Optional<String> findByJno(@Param("jno") long jno, @Param("id") String arsId);
}
