package com.mymap.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum ErrorCode {
    NOT_AUTHENTICATED(HttpStatus.UNAUTHORIZED,"비밀번호가 일치하지 않습니다."),
    NOT_REGISTERED(HttpStatus.UNAUTHORIZED,"등록된 정보가 없습니다."),
    UNABLE_TO_SEND_MAIL(HttpStatus.INTERNAL_SERVER_ERROR, "이메일 요청에 실패하였습니다."),
    NOT_EXIST_AUTHCODE(HttpStatus.NOT_FOUND,"이메일 인증 코드를 확인할 수 없습니다."),
    NOT_EXIST(HttpStatus.NOT_FOUND, "해당 정보를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
