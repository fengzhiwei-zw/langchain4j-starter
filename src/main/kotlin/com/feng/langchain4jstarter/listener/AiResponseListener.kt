package com.feng.langchain4jstarter.listener

import dev.langchain4j.observability.api.event.AiServiceResponseReceivedEvent
import dev.langchain4j.observability.api.listener.AiServiceResponseReceivedListener

class AiResponseListener: AiServiceResponseReceivedListener {

    override fun onEvent(p0: AiServiceResponseReceivedEvent) {
        val request = p0.request()
        val response = p0.response()
        val invocationContext = p0.invocationContext()

        /**
         * 用户ID、方法名称、提问、提问时间、回答、finishReason、大模型名称
         */
        println("【监控】Token 消耗: ${response.tokenUsage().totalTokenCount()}")
        invocationContext.chatMemoryId()
        invocationContext.methodName()
        invocationContext.userMessage().singleText()
        invocationContext.timestamp()
        // 如果存在toolExecutionRequests：调用Tool；不存在：AI结果
        if (response.aiMessage().hasToolExecutionRequests()){
            invocationContext.userMessage().attributes()[invocationContext.invocationId().toString()] = "主键ID"
        } else {
            response.aiMessage().text()
        }
        response.finishReason()
        response.modelName()
    }
}