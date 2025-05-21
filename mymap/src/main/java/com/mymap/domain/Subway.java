package com.mymap.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name="subway")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class Subway {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long no;

    @Column(name="subway_id",nullable = false)
    private String subwayId;

    @Column(columnDefinition = "geometry(Point, 4326)")
    @JdbcTypeCode(SqlTypes.GEOMETRY)
    private Point geom;

    @Column(name="station_name",nullable = false)
    private String stationName;

    @Column(name="route_name")
    private String routeName;
}
