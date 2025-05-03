package com.mymap.domain.clusters.repository;

import com.mymap.domain.clusters.entity.FilteredBus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FilteredBusRepository extends JpaRepository<FilteredBus,Long> {
    @Query(" select f from FilteredBus f where f.journeyNo = :jno ")
    Optional<List<FilteredBus>> findByJno(@Param("jno") long jno);

    @Query(" select f from FilteredBus f where f.arsId = :arsid and f.clusterName = :clusterName ")
    Optional<FilteredBus> findByArsIdAndClusterName(@Param("arsid") String arsid, @Param("clusterName") String clusterName);
}
