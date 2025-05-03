package com.mymap.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name="from_to_geo")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class FromToGeom {
    @Id
    private long no;

    @Column(name="userno",nullable = false)
    private long userNo;

    private String name;

    @Column(columnDefinition = "geometry(Point, 4326)")
    @JdbcTypeCode(SqlTypes.GEOMETRY)
    private Point geom;
}
