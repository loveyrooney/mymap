package com.mymap.exception;

import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@Log4j2
@ControllerAdvice
public class ExceptionController {
    @ExceptionHandler(BusinessException.class)
    public @ResponseBody ResponseEntity<?> handleBusinessException(BusinessException e){
        log.info("BusinessException : {}",e.getErrorCode().getMessage());
        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(Map.of("msg",e.getErrorCode().getMessage()));
    }
}
