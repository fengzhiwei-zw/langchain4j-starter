package com.feng.langchain4jstarter.service.impl

import com.alibaba.dashscope.aigc.imagegeneration.ImageGeneration
import com.alibaba.dashscope.aigc.imagegeneration.ImageGenerationMessage
import com.alibaba.dashscope.aigc.imagegeneration.ImageGenerationParam
import com.alibaba.dashscope.aigc.imagegeneration.ImageGenerationResult
import com.alibaba.dashscope.exception.ApiException
import com.alibaba.dashscope.exception.NoApiKeyException
import com.alibaba.dashscope.exception.UploadFileException
import com.alibaba.dashscope.utils.JsonUtils
import com.feng.langchain4jstarter.Main.waitTask
import com.feng.langchain4jstarter.service.AiService
import com.feng.langchain4jstarter.service.Assistant
import com.feng.langchain4jstarter.service.AssistantStream
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.*


@Service
class AiServiceImpl : AiService {

    @Autowired
    private lateinit var assistant: Assistant

    @Autowired
    private lateinit var assistantStream: AssistantStream

    @Autowired
    private lateinit var imageGeneration: ImageGeneration

    @Autowired
    private lateinit var imageGenerationParam: ImageGenerationParam

    override fun chat(userId: Long, message: String): String {
        return assistant.chat(userId, message)
    }

    override fun chatStream(
        userId: Long,
        message: String
    ): SseEmitter {
        val emitter = SseEmitter()
        assistantStream.chat(userId, message)
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
        return emitter
    }

    override fun image(message: String): String {
        val generationMessage: ImageGenerationMessage = ImageGenerationMessage.builder()
            .role("user")
            .content(
                mutableListOf<MutableMap<String, Any>>(
                    Collections.singletonMap(
                        "text",
                        message
                    )
                )
            ).build()
        val taskResult: ImageGenerationResult?
        try {
            imageGenerationParam.messages = Collections.singletonList(generationMessage)
            println("----async call, creating task----")
            taskResult = imageGeneration.asyncCall(imageGenerationParam)
        } catch (e: ApiException) {
            throw RuntimeException(e.message)
        } catch (e: NoApiKeyException) {
            throw RuntimeException(e.message)
        } catch (e: UploadFileException) {
            throw RuntimeException(e.message)
        }
        println("Task created: " + JsonUtils.toJson(taskResult))
        // 等待任务完成
        val taskId = taskResult.output.taskId
        val result = waitTask(taskId)
        return JsonUtils.toJson(result)
    }
}