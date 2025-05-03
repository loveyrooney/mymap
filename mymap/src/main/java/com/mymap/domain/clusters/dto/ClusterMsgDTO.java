package com.mymap.domain.clusters.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClusterMsgDTO {
    private String clusterName;
    private Map<String, String[]> bus;
    private String[] sub;
    private String[] bike;

}
