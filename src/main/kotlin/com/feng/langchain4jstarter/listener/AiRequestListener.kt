package com.feng.langchain4jstarter.listener

import dev.langchain4j.observability.api.event.AiServiceRequestIssuedEvent
import dev.langchain4j.observability.api.listener.AiServiceRequestIssuedListener

class AiRequestListener: AiServiceRequestIssuedListener {
    override fun onEvent(event: AiServiceRequestIssuedEvent) {

    }
}