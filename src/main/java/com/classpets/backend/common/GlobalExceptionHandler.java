package com.classpets.backend.common;

import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ApiResponse<Void> handleBiz(BizException ex) {
        return ApiResponse.fail(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler({ MethodArgumentNotValidException.class, BindException.class,
            HttpMessageNotReadableException.class })
    public ApiResponse<Void> handleBadRequest(Exception ex) {
        if (ex instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException manv = (MethodArgumentNotValidException) ex;
            String msg = manv.getBindingResult().getFieldError() != null
                    ? manv.getBindingResult().getFieldError().getDefaultMessage()
                    : "参数错误";
            return ApiResponse.fail(40001, msg);
        }
        if (ex instanceof BindException) {
            BindException be = (BindException) ex;
            String msg = be.getBindingResult().getFieldError() != null
                    ? be.getBindingResult().getFieldError().getDefaultMessage()
                    : "参数错误";
            return ApiResponse.fail(40001, msg);
        }
        return ApiResponse.fail(40001, "参数错误");
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleSystem(Exception ex) {
        return ApiResponse.fail(50001, "系统异常");
    }
}
