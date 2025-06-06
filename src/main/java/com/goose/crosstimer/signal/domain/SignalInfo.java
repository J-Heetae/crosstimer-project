package com.goose.crosstimer.signal.domain;

import com.goose.crosstimer.crossroad.domain.Crossroad;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(name = "signal_info")
@NoArgsConstructor(access = PROTECTED)
@ToString(exclude = "crossroad")
public class SignalInfo {
    @Id
    private Integer itstId;
    private Long trsmUtcTime;
    @OneToOne
    @JoinColumn(name = "itstId")
    private Crossroad crossroad;

    private Integer ntPdsgRmdrCs;
    private String ntPdsgStatNm;

    private Integer etPdsgRmdrCs;
    private String etPdsgStatNm;

    private Integer stPdsgRmdrCs;
    private String stPdsgStatNm;

    private Integer wtPdsgRmdrCs;
    private String wtPdsgStatNm;

    private Integer nePdsgRmdrCs;
    private String nePdsgStatNm;

    private Integer sePdsgRmdrCs;
    private String sePdsgStatNm;

    private Integer swPdsgRmdrCs;
    private String swPdsgStatNm;

    private Integer nwPdsgRmdrCs;
    private String nwPdsgStatNm;

    @Builder
    private SignalInfo(
            Integer itstId,
            Long trsmUtcTime,
            Integer ntPdsgRmdrCs, String ntPdsgStatNm,
            Integer etPdsgRmdrCs, String etPdsgStatNm,
            Integer stPdsgRmdrCs, String stPdsgStatNm,
            Integer wtPdsgRmdrCs, String wtPdsgStatNm,
            Integer nePdsgRmdrCs, String nePdsgStatNm,
            Integer sePdsgRmdrCs, String sePdsgStatNm,
            Integer swPdsgRmdrCs, String swPdsgStatNm,
            Integer nwPdsgRmdrCs, String nwPdsgStatNm
    ) {
        this.itstId = itstId;
        this.trsmUtcTime = trsmUtcTime;

        this.ntPdsgRmdrCs = ntPdsgRmdrCs;
        this.ntPdsgStatNm = ntPdsgStatNm;

        this.etPdsgRmdrCs = etPdsgRmdrCs;
        this.etPdsgStatNm = etPdsgStatNm;

        this.stPdsgRmdrCs = stPdsgRmdrCs;
        this.stPdsgStatNm = stPdsgStatNm;

        this.wtPdsgRmdrCs = wtPdsgRmdrCs;
        this.wtPdsgStatNm = wtPdsgStatNm;

        this.nePdsgRmdrCs = nePdsgRmdrCs;
        this.nePdsgStatNm = nePdsgStatNm;

        this.sePdsgRmdrCs = sePdsgRmdrCs;
        this.sePdsgStatNm = sePdsgStatNm;

        this.swPdsgRmdrCs = swPdsgRmdrCs;
        this.swPdsgStatNm = swPdsgStatNm;

        this.nwPdsgRmdrCs = nwPdsgRmdrCs;
        this.nwPdsgStatNm = nwPdsgStatNm;
    }
}
