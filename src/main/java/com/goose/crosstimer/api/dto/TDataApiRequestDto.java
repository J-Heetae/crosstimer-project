package com.goose.crosstimer.api.dto;

import lombok.Getter;

@Getter
public class TDataApiRequestDto {
    private final String type = "json";
    private final int pageNo;
    private final int numOfRows;
    private final Integer itstId;

    private TDataApiRequestDto(int pageNo, int numOfRows, Integer itstId) {
        if (pageNo <= 0) {
            throw new IllegalArgumentException("pageNo must be greater than 0");
        }

        if (numOfRows <= 0 || numOfRows > 1000) {
            throw new IllegalArgumentException("numOfRows must be between 1 and 1000");
        }

        this.pageNo = pageNo;
        this.numOfRows = numOfRows;
        this.itstId = itstId;
    }

    public static TDataApiRequestDto from() {
        return new TDataApiRequestDto(1, 1000, null);
    }

    // 일반 페이지네이션 요청
    public static TDataApiRequestDto fromPagination(int pageNo, int numOfRows) {
        return new TDataApiRequestDto(pageNo, numOfRows, null);
    }

    // 특정 교차로 ID 기반 요청
    public static TDataApiRequestDto fromItstId(Integer itstId) {
        return new TDataApiRequestDto(1, 1, itstId);
    }
}
