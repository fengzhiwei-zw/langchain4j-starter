package com.feng.langchain4jstarter.service

import dev.langchain4j.service.MemoryId
import dev.langchain4j.service.SystemMessage
import dev.langchain4j.service.TokenStream
import dev.langchain4j.service.UserMessage

interface AssistantStream {

    @SystemMessage("""
You are a helpful, professional AI assistant. 
Answer concisely and accurately. 
Maintain context from previous messages.
""")
    fun chat(@MemoryId memoryId: Long, @UserMessage userMessage: String): TokenStream
}