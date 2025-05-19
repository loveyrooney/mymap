package com.mymap.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {
    NOT_AUTHENTICATED(HttpStatus.UNAUTHORIZED,"Invalid Authentication"),
    NOT_REGISTERED(HttpStatus.UNAUTHORIZED,"Unregistered Data"),
    NOT_AUTHENTICATED_TOKEN(HttpStatus.UNAUTHORIZED,"Invalid Token"),
    NOT_REGISTERED_TOKEN(HttpStatus.UNAUTHORIZED,"Unregistered Token"),
    JOURNEY_INSERT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,"경로 등록에 실패하였습니다."),
    JOURNEY_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,"경로 수정에 실패하였습니다."),
    TOKEN_PROCESSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Generate Token Failed"),
    NOT_EXIST_AUTHCODE(HttpStatus.NOT_FOUND,"이메일 인증 코드를 확인할 수 없습니다."),
    NOT_EXIST(HttpStatus.NOT_FOUND, "Data Is Not Exist");

    private final HttpStatus httpStatus;
    private final String message;
}
