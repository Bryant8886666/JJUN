package com.example.one.exception;

import com.example.one.common.BaseResponse;
import com.example.one.common.ErrorCode;
import com.example.one.common.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;




/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public BaseResponse businessExceptionHandler(BusinessException e){
        log.error("businessException:" + e.getMessage(), e);
        return ResultUtil.error(e.getCode(),e.getMessage(),e.getDescription());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse runtimeExceptionHandler(BusinessException e){
        log.error("runtimeException", e);
        return ResultUtil.error(ErrorCode.SYSTEM_ERROR, e.getMessage(), "");
    }
}





//@ControllerAdvice
//@Slf4j
////@RestControllerAdvice
//@ResponseBody
//@RestControllerAdvice
//public class GlobalExceptionHandler {
//
//    @ExceptionHandler(BusinessException.class)
//    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
//        log.error("businessException: " + e.getMessage(), e);
//        return ResultUtils.error(e.getCode(), e.getMessage(), e.getDescription());
//    }
//
//
//    @ExceptionHandler(RuntimeException.class)
//    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
//        log.error("runtimeException", e);
//        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, e.getMessage(), "");
//    }
//}
