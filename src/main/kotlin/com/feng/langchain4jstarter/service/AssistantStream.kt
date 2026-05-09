package com.feng.langchain4jstarter.service

import dev.langchain4j.service.MemoryId
import dev.langchain4j.service.SystemMessage
import dev.langchain4j.service.TokenStream
import dev.langchain4j.service.UserMessage
import org.springframework.web.multipart.MultipartFile

interface AssistantStream {

    @SystemMessage("""
You are a helpful, professional AI assistant. 
Answer concisely and accurately. 
Maintain context from previous messages.
""")
    fun chat(@MemoryId memoryId: String, @UserMessage userMessage: String): TokenStream
}