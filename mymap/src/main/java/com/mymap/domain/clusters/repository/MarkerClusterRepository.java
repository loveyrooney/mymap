package com.mymap.domain.clusters.repository;

import com.mymap.domain.clusters.entity.MarkerCluster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MarkerClusterRepository extends JpaRepository<MarkerCluster,Long> {

    @Query(" select m from MarkerCluster m where m.journeyNo = :jno ")
    Optional<List<MarkerCluster>> findByJno(@Param("jno") long jno);

    @Query(value = " select m.cluster_name " +
            " from marker_cluster m" +
            " where m.journey_no = :jno" +
            " and (:id = any(m.cluster_bike) " +
            " or :id = any(m.cluster_bus)" +
            " or :id = any(m.cluster_sub))",nativeQuery = true)
    Optional<String> findClusterNameByJno(@Param("jno") long jno, @Param("id") String arsId);

    @Query(" delete from MarkerCluster m where m.journeyNo = :no ")
    void deleteAllByJno(long no);
}
