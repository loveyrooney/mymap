package com.mymap.domain.clusters.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="marker_cluster")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class MarkerCluster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long no;

    @Column(name="journey_no",nullable = false)
    private long journeyNo;

    @Column(name="cluster_name")
    private String clusterName;

    @Column(name="geom_table")
    private String geomTable;

    @Column(name="cluster_bus")
    private String[] clusterBus;

    @Column(name="cluster_sub")
    private String[] clusterSub;

    @Column(name="cluster_bike")
    private String[] clusterBike;
}
