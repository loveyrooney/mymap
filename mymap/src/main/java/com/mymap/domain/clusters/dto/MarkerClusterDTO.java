package com.mymap.domain.clusters.dto;

import lombok.*;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MarkerClusterDTO {
    private long no;
    private long journeyNo;
    private String clusterName;
    private String geomTable;
    private String[] clusterBus;
    private String[] clusterSub;
    private String[] clusterBike;
}
