package com.mymap.mymap.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
    String containsWhereSub(long jno, String id);

    @Query(value = " select case " +
            " when :id = any(j.from_bus) then j.from_name " +
            " when :id = any(j.to_bus) then j.to_name " +
            " else null " +
            " end as place" +
            " from Journey j" +
            " where j.no = :jno" +
            " and (:id = any(j.from_bus) " +
            " or :id = any(j.to_bus))", nativeQuery = true)
    String containsWhereBus(long jno, String id);

    @Query(value = " select case " +
            " when :id = any(j.from_bike) then j.from_name " +
            " when :id = any(j.to_bike) then j.to_name " +
            " else null " +
            " end as place" +
            " from Journey j" +
            " where j.no = :jno" +
            " and (:id = any(j.from_bike) " +
            " or :id = any(j.to_bike))", nativeQuery = true)
    String containsWhereBike(long jno, String id);
}
