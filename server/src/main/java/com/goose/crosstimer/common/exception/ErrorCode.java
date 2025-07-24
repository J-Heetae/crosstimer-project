package com.goose.crosstimer.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    
    //Global
    DB_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "G_001", "데이터베이스 작업 중 예외 발생"),

    //TDataApi
    EXTERNAL_SIGNAL_API_ERROR(HttpStatus.BAD_GATEWAY, "T_001", "서울교통빅데이터플랫폼 신호 잔여시간 조회 실패"),
    EXTERNAL_CROSSROAD_API_ERROR(HttpStatus.BAD_GATEWAY, "T_002", "서울교통빅데이터플랫폼 교차로 Map 정보 조회 실패"),
    BATCH_LOG_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "T_003", "로그 배치 작업 중 예외 발생"),

    //Crossroad
    CROSSROAD_NOT_FOUND(HttpStatus.NOT_FOUND, "C_001", "교차로를 찾을 수 없습니다."),
    CROSSROAD_RANGE_EMPTY(HttpStatus.NOT_FOUND, "CROSSROAD_002", "범위 내 교차로가 존재하지 않습니다.");

    //Signal

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
