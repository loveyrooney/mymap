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
    private String stName;
    private String geom;
    private String stid;

    MarkerDTO(String stName, Point geom, String id){
        this.stName = stName;
        this.geom =geom.toText();
        this.stid = id;
    }

    MarkerDTO(String clusterName, String stName, Point geom){
        this.clusterName = clusterName;
        this.stName = stName;
        this.geom =geom.toText();
    }
}
