package com.mymap.mymap.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.*;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MarkerClusterDTO {
    private long no;
    private long journeyNo;
    private String clusterName;
    private String[] clusterBus;
    private String[] clusterSub;
    private String[] clusterBike;
}
