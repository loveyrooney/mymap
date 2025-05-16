package com.mymap.domain.geoms;

import lombok.*;
import org.locationtech.jts.geom.Point;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MarkerDTO {
    private String clusterName;
    private String group;
    private String geom;
    private String stid;
    private String lon;
    private String lat;
}
