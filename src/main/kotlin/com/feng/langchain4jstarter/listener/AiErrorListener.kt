package com.feng.langchain4jstarter.listener

import dev.langchain4j.observability.api.event.AiServiceErrorEvent
import dev.langchain4j.observability.api.listener.AiServiceErrorListener

class AiErrorListener: AiServiceErrorListener {
    override fun onEvent(p0: AiServiceErrorEvent) {
        System.err.println("【监控】发生错误: ${p0.error().message}")
    }
}