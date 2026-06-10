package com.feng.langchain4jstarter.controller

import com.feng.langchain4jstarter.exception.BusinessException
import com.feng.langchain4jstarter.model.ApiResponse
import com.feng.langchain4jstarter.service.AiService
import com.feng.langchain4jstarter.service.FileService
import com.feng.langchain4jstarter.util.RateLimitUtil
import com.feng.langchain4jstarter.util.SecurityUtil
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

    @Autowired
    private lateinit var rateLimitUtil: RateLimitUtil

    @PostMapping("/chat")
    fun chat(@RequestBody message: String): ApiResponse<String> {
        return ApiResponse.success(aiService.chat(SecurityUtil.userId, message))
    }

    @PostMapping(value = ["/chatStream"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE]) // 必须指定 produces
    fun chatStream(@RequestParam message: String): SseEmitter {
        val userId = SecurityUtil.userId
        if (!rateLimitUtil.tryConsume(userId)) {
            throw BusinessException(429, "请求过于频繁，请稍后再试")
        }
        return aiService.chatStream(userId, message)
    }

    @PostMapping("/chromaEmbedding")
    fun addChromaEmbedding(@RequestParam("file") file: MultipartFile): ApiResponse<String> {
        fileService.processUserUpload(SecurityUtil.userId, file)
        return ApiResponse.success("知识库添加成功！！！")
    }

    @PostMapping("/image")
    fun image(@RequestParam message: String): ApiResponse<String> {
        return ApiResponse.success(aiService.image(message))
    }
}