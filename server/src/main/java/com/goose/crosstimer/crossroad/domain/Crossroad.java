package com.goose.crosstimer.crossroad.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;


import static lombok.AccessLevel.PROTECTED;


@Getter
@Entity
@Table(name = "crossroads")
@ToString
@NoArgsConstructor(access = PROTECTED)
public class Crossroad {
    @Id
    private Integer crossroadId;
    private String name;
    private Double lat;
    private Double lng;

    @Builder
    private Crossroad(Integer crossroadId, String name, Double lat, Double lng) {
        this.crossroadId = crossroadId;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
    }

    public void update(String name, Double lat, Double lng) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
    }
}
