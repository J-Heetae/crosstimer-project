package com.goose.crosstimer.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TDataSignalResponse(
        @JsonProperty("itstId") Integer crossroadId,
        @JsonProperty("trsmUtcTime") Long sendAt,

        @JsonProperty("ntPdsgRmdrCs") Integer nDeciSec,
        @JsonProperty("ntPdsgStatNm") String nStatus,

        @JsonProperty("etPdsgRmdrCs") Integer eDeciSec,
        @JsonProperty("etPdsgStatNm") String eStatus,

        @JsonProperty("stPdsgRmdrCs") Integer sDeciSec,
        @JsonProperty("stPdsgStatNm") String sStatus,

        @JsonProperty("wtPdsgRmdrCs") Integer wDeciSec,
        @JsonProperty("wtPdsgStatNm") String wStatus,

        @JsonProperty("nePdsgRmdrCs") Integer neDeciSec,
        @JsonProperty("nePdsgStatNm") String neStatus,

        @JsonProperty("nwPdsgRmdrCs") Integer nwDeciSec,
        @JsonProperty("nwPdsgStatNm") String nwStatus,

        @JsonProperty("sePdsgRmdrCs") Integer seDeciSec,
        @JsonProperty("sePdsgStatNm") String seStatus,

        @JsonProperty("swPdsgRmdrCs") Integer swDeciSec,
        @JsonProperty("swPdsgStatNm") String swStatus
) {
}
