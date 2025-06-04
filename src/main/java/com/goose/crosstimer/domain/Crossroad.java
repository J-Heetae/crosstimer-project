package com.goose.crosstimer.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

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

    private Crossroad(Integer itstId, String name, Double lat, Double lng) {
        this.itstId = itstId;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
    }

    public static Crossroad create(Integer itstId, String name, Double lat, Double lng) {
        return new Crossroad(itstId, name, lat, lng);
    }

}
