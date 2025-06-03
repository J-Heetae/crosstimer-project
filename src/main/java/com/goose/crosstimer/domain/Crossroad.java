package com.goose.crosstimer.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;


@Getter
@Entity
@Table(name = "crossroad")
@NoArgsConstructor(access = PROTECTED)
public class Crossroad {
    @Id
    private Integer itstId;
    private String itstKrNm;
    private Double mapCtptIntLat;
    private Double mapCtptIntLot;
    @OneToOne(mappedBy = "crossroad")
    private SignalInfo signalInfo;

    private Crossroad(Integer itsdId, String itstKrNm, Double mapCtptIntLat, Double mapCtptIntLot) {
        this.itstId = itsdId;
        this.itstKrNm = itstKrNm;
        this.mapCtptIntLat = mapCtptIntLat;
        this.mapCtptIntLot = mapCtptIntLot;
    }

    public static Crossroad create(Integer itsdId, String itstKrNm, Double mapCtptIntLat, Double mapCtptIntLot) {
        return new Crossroad(itsdId, itstKrNm, mapCtptIntLat, mapCtptIntLot);
    }

}
