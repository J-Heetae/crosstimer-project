package com.goose.crosstimer.crossroad.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.goose.crosstimer.signal.domain.SignalCycle;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PROTECTED;


@Getter
@Entity
@Table(name = "crossroad")
@ToString
@NoArgsConstructor(access = PROTECTED)
public class Crossroad {
    @Id
    private Integer itstId;
    private String name;
    private Double lat;
    private Double lng;

    @OneToMany(mappedBy = "crossroad")
    @JsonManagedReference
    List<SignalCycle> cycleList = new ArrayList<>();

    @Builder
    private Crossroad(Integer itstId, String name, Double lat, Double lng) {
        this.itstId = itstId;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
    }

}
