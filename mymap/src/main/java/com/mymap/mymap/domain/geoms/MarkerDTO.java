package com.mymap.mymap.domain.geoms;

import lombok.*;
import org.locationtech.jts.geom.Point;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MarkerDTO {
    private String clusterName;
    private String stName;
    private Point geom;

    MarkerDTO(String stName, Point geom){
        this.stName = stName;
        this.geom =geom;
    }
}
