package com.goose.crosstimer.crossroad.domain;

import com.goose.crosstimer.signal.domain.SignalInfo;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.*;

import static lombok.AccessLevel.PROTECTED;


@Getter
@Entity
@Table(name = "crossroad")
@ToString(exclude = "signalInfo")
@NoArgsConstructor(access = PROTECTED)
public class Crossroad {
    @Id
    private Integer itstId;
    private String name;
    private Double lat;
    private Double lng;
    @OneToOne(mappedBy = "crossroad")
    private SignalInfo signalInfo;

    @Builder
    private Crossroad(Integer itstId, String name, Double lat, Double lng) {
        this.itstId = itstId;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
    }

}
