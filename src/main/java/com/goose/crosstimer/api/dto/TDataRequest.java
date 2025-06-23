package com.goose.crosstimer.api.dto;

import lombok.Getter;

@Getter
public class TDataRequest {
    private final String type = "json";
    private final int pageNo;
    private final int numOfRows;
    private final Integer itstId;

    private TDataRequest(int pageNo, int numOfRows, Integer itstId) {
        if (pageNo <= 0) {
            throw new IllegalArgumentException("pageNo은 0보다 커야 합니다.");
        }

        if (numOfRows <= 0 || numOfRows > 1000) {
            throw new IllegalArgumentException("numOfRows은 1 이상 1000 이하만 가능합니다.");
        }

        this.pageNo = pageNo;
        this.numOfRows = numOfRows;
        this.itstId = itstId;
    }

    public static TDataRequest from() {
        return new TDataRequest(1, 1000, null);
    }

    // 일반 페이지네이션 요청
    public static TDataRequest fromPagination(int pageNo, int numOfRows) {
        return new TDataRequest(pageNo, numOfRows, null);
    }

    // 특정 교차로 ID 기반 요청
    public static TDataRequest fromItstId(Integer itstId) {
        return new TDataRequest(1, 1, itstId);
    }
}
