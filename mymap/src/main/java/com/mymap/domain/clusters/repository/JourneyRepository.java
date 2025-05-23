package com.mymap.domain.clusters.repository;

import com.mymap.domain.clusters.dto.JourneyDTO;
import com.mymap.domain.clusters.entity.Journey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JourneyRepository extends JpaRepository<Journey,Long> {
    @Query(value = " select case " +
            " when :id = any(j.from_sub) then j.from_name " +
            " when :id = any(j.to_sub) then j.to_name " +
            " else null " +
            " end as place" +
            " from Journey j" +
            " where j.no = :jno" +
            " and (:id = any(j.from_sub) " +
            " or :id = any(j.to_sub))", nativeQuery = true)
    String containsWhereSub(@Param("jno") long jno, @Param("id") String id);

    @Query(value = " select case " +
            " when :id = any(j.from_bus) then j.from_name " +
            " when :id = any(j.to_bus) then j.to_name " +
            " else null " +
            " end as place" +
            " from Journey j" +
            " where j.no = :jno" +
            " and (:id = any(j.from_bus) " +
            " or :id = any(j.to_bus))", nativeQuery = true)
    String containsWhereBus(@Param("jno") long jno, @Param("id") String id);

    @Query(value = " select case " +
            " when :id = any(j.from_bike) then j.from_name " +
            " when :id = any(j.to_bike) then j.to_name " +
            " else null " +
            " end as place" +
            " from Journey j" +
            " where j.no = :jno" +
            " and (:id = any(j.from_bike) " +
            " or :id = any(j.to_bike))", nativeQuery = true)
    String containsWhereBike(@Param("jno") long jno, @Param("id") String id);

    @Query( " select new com.mymap.domain.clusters.dto.JourneyDTO(j.no, j.fromName, j.toName) from Journey j where j.userNo = :userNo ")
    Optional<List<JourneyDTO>> findAllByUserNo(@Param("userNo") Long principal);

    Optional<Journey> findByNo(Long no);

    //@Query(" delete from Journey j where j.no = :no ")
    void deleteByNo(long no);

    @Query(" select count(j) from Journey j where j.userNo = :userNo and j.fromName = :fromName ")
    int findByUserNoAndFromName(@Param("userNo") long userNo, @Param("fromName") String fromName);

    @Query(" select count(j) from Journey j where j.userNo = :userNo and j.toName = :toName ")
    int findByUserNoAndToName(@Param("userNo") long userNo, @Param("toName") String toName);
}
