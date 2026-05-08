package com.feng.langchain4jstarter.listener

import dev.langchain4j.observability.api.event.ToolExecutedEvent
import dev.langchain4j.observability.api.listener.ToolExecutedEventListener

class AiToolExecutedListener: ToolExecutedEventListener {
    override fun onEvent(p0: ToolExecutedEvent) {
        val invocationContext = p0.invocationContext()
        val request = p0.request()

        /**
         * 消耗主键ID、用户ID、方法名称、提问、提问时间、回答、函数ID、函数名称
         */
        invocationContext.userMessage().attribute(invocationContext.invocationId().toString(), String::class.java)
        invocationContext.chatMemoryId()
        invocationContext.methodName()
        invocationContext.userMessage().singleText()
        invocationContext.timestamp()
        p0.resultText()
        request.id()
        request.name()
    }
}