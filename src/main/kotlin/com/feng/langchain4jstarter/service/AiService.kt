package com.feng.langchain4jstarter.service

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

interface AiService {

    fun chat(userId: String, message: String): String {
        throw NotImplementedError()
    }

    fun chatStream(userId: String, message: String): SseEmitter {
        throw NotImplementedError()
    }

    fun image(message: String): String {
        throw NotImplementedError()
    }

}