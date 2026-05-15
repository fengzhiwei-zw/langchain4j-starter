package com.feng.langchain4jstarter.controller

import com.feng.langchain4jstarter.service.AiService
import com.feng.langchain4jstarter.service.FileService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter


@RestController
@RequestMapping("/ai")
class AiController {

    @Autowired
    private lateinit var aiService: AiService

    @Autowired
    private lateinit var fileService: FileService

    @PostMapping("/chat")
    fun chat(
        @RequestParam(defaultValue = "default-session") sessionId: String,
        @RequestBody message: String
    ): String {
        return aiService.chat(sessionId, message)
    }

    @PostMapping(value = ["/chatStream"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE]) // 必须指定 produces
    fun chatStream(
        @RequestParam(defaultValue = "default-session") sessionId: String,
        @RequestParam message: String,
    ): SseEmitter {
        return aiService.chatStream(sessionId, message)
    }

    @PostMapping("/chromaEmbedding")
    fun addChromaEmbedding(
        @RequestParam(defaultValue = "default-session") sessionId: String,
        @RequestParam("file") file: MultipartFile
        ): String {
        fileService.processUserUpload(sessionId, file)
        return "知识库添加成功！！！"
    }

    @PostMapping("/image")
    fun image(@RequestParam message: String): String {
        return aiService.image(message)
    }
}