package com.mymap.domain.clusters.dto;

import jakarta.persistence.Column;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JourneyDTO {
    private long no;
    private long userNo;
    private String fromName;
    private String toName;
    // bus 는 arsId
    private String[] fromBus;
    private String[] tfBus;
    private String[] toBus;
    // sub 은 stName
    private String[] fromSub;
    private String[] tfSub;
    private String[] toSub;
    // bike 는 stationId
    private String[] fromBike;
    private String[] tfBike;
    private String[] toBike;

}
