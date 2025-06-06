package com.mymap.domain.clusters.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="filtered_bus")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class FilteredBus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long no;

    @Column(name="journey_no",nullable = false)
    private long journeyNo;

    @Column(name="cluster_name")
    private String clusterName;

    @Column(name="ars_id")
    private String arsId;

    @Column(name="routes")
    private String[] routes;
}
