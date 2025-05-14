package com.mymap.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;

@ControllerAdvice
@Slf4j
public class ExceptionController {
    @ExceptionHandler(BusinessException.class)
    public @ResponseBody HashMap<String,Object> handleBusinessException(BusinessException e){
        log.info("Payment Exception : {}",e.getMessage());
        HashMap<String,Object> hm = new HashMap<>();
        hm.put("status",500);
        hm.put("msg",e.getMessage());
        return hm;
    }
}
