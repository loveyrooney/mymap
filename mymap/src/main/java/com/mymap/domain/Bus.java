package com.mymap.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;
import java.time.ZonedDateTime;

@Entity
@Table(name="bus")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class Bus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long no;

    @Column(name="arsid",nullable = false)
    private String arsId;

    @Column(columnDefinition = "geometry(Point, 4326)")
    @JdbcTypeCode(SqlTypes.GEOMETRY)
    private Point geom;

    @Column(name="station_name",nullable = false)
    private String stationName;

    @Column(name="ud_time",nullable = false)
    private ZonedDateTime udTime;

    @Column(name="station_id",nullable = false)
    private String stationId;
}
