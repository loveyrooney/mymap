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
import org.locationtech.jts.geom.MultiPolygon;

@Entity
@Table(name="region")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class Region {
    @Id
    private int gid;

    @Column(name="base_date")
    private String baseDate;

    @Column(name="adm_cd")
    private String admCd;

    @Column(name="adm_nm")
    private String admNm;

    @Column(columnDefinition = "geometry(Point, 4326)")
    @JdbcTypeCode(SqlTypes.GEOMETRY)
    private MultiPolygon geom;


}
