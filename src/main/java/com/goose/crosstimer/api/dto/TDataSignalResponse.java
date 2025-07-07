package com.goose.crosstimer.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TDataSignalResponse(
        @JsonProperty("itstId") Integer itstId,
        @JsonProperty("trsmUtcTime") Long sentAt,

        @JsonProperty("ntPdsgRmdrCs") Integer nSec,
        @JsonProperty("ntPdsgStatNm") String nStatus,

        @JsonProperty("etPdsgRmdrCs") Integer eSec,
        @JsonProperty("etPdsgStatNm") String eStatus,

        @JsonProperty("stPdsgRmdrCs") Integer sSec,
        @JsonProperty("stPdsgStatNm") String sStatus,

        @JsonProperty("wtPdsgRmdrCs") Integer wSec,
        @JsonProperty("wtPdsgStatNm") String wStatus,

        @JsonProperty("nePdsgRmdrCs") Integer neSec,
        @JsonProperty("nePdsgStatNm") String neStatus,

        @JsonProperty("nwPdsgRmdrCs") Integer nwSec,
        @JsonProperty("nwPdsgStatNm") String nwStatus,

        @JsonProperty("sePdsgRmdrCs") Integer seSec,
        @JsonProperty("sePdsgStatNm") String seStatus,

        @JsonProperty("swPdsgRmdrCs") Integer swSec,
        @JsonProperty("swPdsgStatNm") String swStatus
) {
}
