package com.feng.langchain4jstarter.exception

class BusinessException(val code: Int, message: String) : RuntimeException(message)