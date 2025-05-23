package com.mymap.domain.clusters.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FilteredBusDTO {
    private long no;
    private long journeyNo;
    private String clusterName;
    private String arsId;
    private String[] routes;
}
