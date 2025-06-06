package com.mymap.domain.clusters.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="journey")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter @Setter
public class Journey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long no;

    @Column(name="user_no",nullable = false)
    private long userNo;

    @Column(name="from_name")
    private String fromName;

    @Column(name="to_name")
    private String toName;

    @Column(name="from_bus")
    private String[] fromBus;

    @Column(name="tf_bus")
    private String[] tfBus;

    @Column(name="to_bus")
    private String[] toBus;

    @Column(name="from_sub")
    private String[] fromSub;

    @Column(name="tf_sub")
    private String[] tfSub;

    @Column(name="to_sub")
    private String[] toSub;

    @Column(name="from_bike")
    private String[] fromBike;

    @Column(name="tf_bike")
    private String[] tfBike;

    @Column(name="to_bike")
    private String[] toBike;

    private String direction;

}
