package com.feng.langchain4jstarter.listener

import com.feng.langchain4jstarter.pojo.AiAuditLog
import com.feng.langchain4jstarter.repository.AiAuditLogRepository
import dev.langchain4j.observability.api.event.AiServiceResponseReceivedEvent
import dev.langchain4j.observability.api.listener.AiServiceResponseReceivedListener
import org.springframework.stereotype.Component
import java.lang.System.currentTimeMillis
import java.util.concurrent.CompletableFuture

@Component
class AiResponseListener(
    private val aiAuditLogRepository: AiAuditLogRepository
): AiServiceResponseReceivedListener {

    override fun onEvent(p0: AiServiceResponseReceivedEvent) {
        val response = p0.response()
        val invocationContext = p0.invocationContext()

        /**
         * 用户ID、方法名称、提问、提问时间、回答、finishReason、大模型名称
         */
        println("【监控】Token 消耗: ${response.tokenUsage().totalTokenCount()}")

        // invocationContext.methodName()
        // response.finishReason()
        // response.modelName()

        // 计算耗时
        val latency = currentTimeMillis() - invocationContext.timestamp().toEpochMilli()

        val log = AiAuditLog().apply {
            userId = invocationContext.chatMemoryId() as Long
            prompt = invocationContext.userMessage().singleText()
            // 如果存在toolExecutionRequests：调用Tool；不存在：AI结果
            this.response = if (response.aiMessage().hasToolExecutionRequests()){
                invocationContext.userMessage().attributes()[invocationContext.invocationId().toString()] = "主键ID"
                response.aiMessage().toolExecutionRequests().toString()
            } else {
                response.aiMessage().text()
            }
            totalTokens = (if (response.tokenUsage() != null) response.tokenUsage().totalTokenCount() else 0)
            latencyMs = (latency)
        }

        CompletableFuture.runAsync(Runnable {
            aiAuditLogRepository.save(log) // 异步保存到数据库
        })
    }
}