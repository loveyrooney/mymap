package com.mymap.mymap.domain.params;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="journey")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class Journey {
    @Id
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

}
