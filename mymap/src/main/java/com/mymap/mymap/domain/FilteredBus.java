package com.mymap.mymap.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    private long no;

    @Column(name="journey_no",nullable = false)
    private long journeyNo;

    @Column(name="cluster_name")
    private String clusterName;

    @Column(name="ars_id")
    private String arsId;

    @Column(name="routes")
    private String[] routes;
}
