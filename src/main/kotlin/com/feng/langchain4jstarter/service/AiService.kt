package com.feng.langchain4jstarter.service

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

interface AiService {

    fun chat(userId: Long, message: String): String

    fun chatStream(userId: Long, message: String): SseEmitter

    fun image(message: String): String

}