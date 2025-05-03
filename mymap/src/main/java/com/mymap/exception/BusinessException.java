package com.mymap.exception;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BusinessException extends RuntimeException{
    private final ErrorCode errorCode;
}
