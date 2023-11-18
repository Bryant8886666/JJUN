package com.two.exception;

import com.two.common.BaseResponse;
import com.two.common.ErrorCode;
import com.two.common.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
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
