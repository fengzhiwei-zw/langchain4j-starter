package com.feng.langchain4jstarter.exception

import com.feng.langchain4jstarter.model.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException::class)
    fun <T> handleBusinessException(e: BusinessException): ApiResponse<T> {
        System.err.printf("business exception: %s\n", e)
        return ApiResponse.fail(
            e.code,
            e.message
        )
    }

    @ExceptionHandler(Exception::class)
    fun <T> handleException(e: Exception): ApiResponse<T> {
        System.err.printf("system exception: %s\n", e)
        return ApiResponse.fail(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "系统异常"
        )
    }
}