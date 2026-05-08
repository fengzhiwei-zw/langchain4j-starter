package com.feng.langchain4jstarter.config

import com.feng.langchain4jstarter.listener.AiCompletedListener
import com.feng.langchain4jstarter.listener.AiRequestListener
import com.feng.langchain4jstarter.listener.AiResponseListener
import com.feng.langchain4jstarter.listener.AiToolExecutedListener
import com.feng.langchain4jstarter.service.Assistant
import com.feng.langchain4jstarter.service.AssistantStream
import com.feng.langchain4jstarter.tool.WeatherTool
import dev.langchain4j.memory.chat.ChatMemoryProvider
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.StreamingChatModel
import dev.langchain4j.service.AiServices
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class AiConfig {
    @Bean
    fun chatMemoryProvider(): ChatMemoryProvider {
        return ChatMemoryProvider { memoryId: Any? ->
            MessageWindowChatMemory.builder()
                .id(memoryId) // 每个会话独立的 Memory
                .maxMessages(10) // 保留最近 10 条消息（可根据需要调整）
                .build()
        }
    }

    @Bean
    fun assistant(
        chatModel: ChatModel?,
        chatMemoryProvider: ChatMemoryProvider?
    ): Assistant {   // 注入 Provider

        return AiServices.builder(Assistant::class.java)
            .chatModel(chatModel)
            .chatMemoryProvider(chatMemoryProvider) // 使用 Provider（推荐）
            .tools(WeatherTool())
            .registerListeners(AiRequestListener(), AiToolExecutedListener(), AiResponseListener(), AiCompletedListener())
            .build()
    }

    @Bean
    fun assistantStream(
        chatModel: StreamingChatModel?,
        chatMemoryProvider: ChatMemoryProvider?
    ): AssistantStream {   // 注入 Provider

        return AiServices.builder(AssistantStream::class.java)
            .streamingChatModel(chatModel)
            .chatMemoryProvider(chatMemoryProvider) // 使用 Provider（推荐）
            .tools(WeatherTool())
            .registerListeners(AiRequestListener(), AiToolExecutedListener(), AiResponseListener(), AiCompletedListener())
            .build()
    }
}