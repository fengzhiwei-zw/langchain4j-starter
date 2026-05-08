package com.feng.langchain4jstarter.listener

import dev.langchain4j.observability.api.event.AiServiceCompletedEvent
import dev.langchain4j.observability.api.listener.AiServiceCompletedListener

class AiCompletedListener : AiServiceCompletedListener {
    override fun onEvent(event: AiServiceCompletedEvent) {

    }
}