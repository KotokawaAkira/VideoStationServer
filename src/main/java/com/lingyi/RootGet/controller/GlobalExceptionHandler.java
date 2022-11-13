package com.lingyi.RootGet.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final Logger log = LoggerFactory.getLogger("videoLog");
    @ExceptionHandler
    public void handler(Exception e, HttpServletResponse response){
        response.setStatus(500);
        StackTraceElement[] stackTrace = e.getStackTrace();
        log.error("==============================");
        log.error("msg:"+e.getMessage());
        for(int i=0;i<5;i++){
            log.error("class:"+stackTrace[i].getClassName()+" method:"+stackTrace[i].getMethodName()+" lines:"+stackTrace[i].getLineNumber());
        }
    }
}
