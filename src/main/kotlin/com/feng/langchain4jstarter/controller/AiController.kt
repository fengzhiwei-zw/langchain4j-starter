package com.feng.langchain4jstarter.controller

import com.feng.langchain4jstarter.service.AiService
import com.feng.langchain4jstarter.service.Assistant
import com.feng.langchain4jstarter.service.AssistantStream
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


@RestController
@RequestMapping("/ai")
class AiController {
    private val executorService: ExecutorService = Executors.newFixedThreadPool(3)

    @Autowired
    private lateinit var assistant: Assistant

    @Autowired
    private lateinit var assistantStream: AssistantStream

    @Autowired
    private lateinit var aiService: AiService

    @PostMapping("/chat")
    fun chat(
        @RequestParam(defaultValue = "default-session") sessionId: String,
        @RequestBody message: String
    ): String {
        return assistant.chat(sessionId, message)
    }

    @PostMapping(value = ["/chatStream"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE]) // 必须指定 produces
    fun chatStream(
        @RequestParam(defaultValue = "default-session") sessionId: String,
        @RequestParam message: String,
    ): SseEmitter {
        val emitter = SseEmitter()
        executorService.execute {
            assistantStream.chat(sessionId, message)
                .onPartialResponse { token ->
                    try {
                        // SseEmitter 会自动处理 data: 前缀和编码
                        emitter.send(SseEmitter.event().data(token))
                    } catch (e: Exception) {
                        emitter.completeWithError(e)
                    }
                }
                .onCompleteResponse { _ -> emitter.complete() }
                .onError({ err ->
                    emitter.completeWithError(err)
                })
                .start()
        }
        return emitter
    }

    @PostMapping("/chromaEmbedding")
    fun addChromaEmbedding(
        @RequestParam(defaultValue = "default-session") sessionId: String,
        @RequestParam("file") file: MultipartFile
        ): String {
        aiService.processUserUpload(sessionId, file)
        return "知识库添加成功！！！"
    }

    @PostMapping("/document")
    fun searchDocument(
        @RequestParam(defaultValue = "default-session") sessionId: String
        ): String {
        return aiService.queryDocument(sessionId, "@RequestParam('file') file: MultipartFile")
    }
}