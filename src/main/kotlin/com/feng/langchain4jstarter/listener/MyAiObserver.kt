package com.feng.langchain4jstarter.listener

import com.feng.langchain4jstarter.pojo.AiAuditLog
import com.feng.langchain4jstarter.repository.AiAuditLogRepository
import com.feng.langchain4jstarter.util.SecurityUtil
import dev.langchain4j.data.message.TextContent
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.listener.ChatModelErrorContext
import dev.langchain4j.model.chat.listener.ChatModelListener
import dev.langchain4j.model.chat.listener.ChatModelRequestContext
import dev.langchain4j.model.chat.listener.ChatModelResponseContext
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture

// @Component
class MyAiObserver: ChatModelListener {

    @Autowired
    private lateinit var aiAuditLogRepository: AiAuditLogRepository

    override fun onRequest(requestContext: ChatModelRequestContext) {
        requestContext.attributes()["startTime"] = System.currentTimeMillis()
        // System.out.println("【监控】AI 准备回答，问题: " + requestContext.request().messages().getLast().text());
        requestContext.attributes()["userId"] = SecurityUtil.userId
    }

    override fun onResponse(responseContext: ChatModelResponseContext) {
        // 计算耗时
        val startTime = responseContext.attributes()["startTime"] as Long
        val latency = System.currentTimeMillis() - startTime

        // System.out.println("【监控】AI 回答完毕，耗时: " + duration + "ms");
        // System.out.println("【监控】Token 消耗: " + responseContext.response().tokenUsage().totalTokenCount());
        val log = AiAuditLog()
        log.setUserId(responseContext.attributes()["userId"] as Long)
        val request = responseContext.chatRequest()
        if (request != null) {
            val userMessageList = request.messages().filterIsInstance<UserMessage>()
            val textContents = userMessageList.last().contents().filterIsInstance<TextContent>()
            log.setPrompt(textContents.last().text())
        }
        val response = responseContext.chatResponse()
        log.setResponse(response.aiMessage().text())
        log.setTotalTokens(if (response.tokenUsage() != null) response.tokenUsage().totalTokenCount() else 0)
        log.setLatencyMs(latency)
        log.setCreatedAt(LocalDateTime.now())

        CompletableFuture.runAsync(Runnable {
            aiAuditLogRepository.save(log) // 异步保存到数据库
        })
    }

    override fun onError(errorContext: ChatModelErrorContext) {
        System.err.println("【监控】发生错误: " + errorContext.error().message)
    }
}