package com.feng.langchain4jstarter.service.impl

import com.alibaba.dashscope.aigc.imagegeneration.ImageGeneration
import com.alibaba.dashscope.aigc.imagegeneration.ImageGenerationMessage
import com.alibaba.dashscope.aigc.imagegeneration.ImageGenerationParam
import com.alibaba.dashscope.aigc.imagegeneration.ImageGenerationResult
import com.alibaba.dashscope.exception.ApiException
import com.alibaba.dashscope.exception.NoApiKeyException
import com.alibaba.dashscope.exception.UploadFileException
import com.alibaba.dashscope.utils.Constants.apiKey
import com.alibaba.dashscope.utils.JsonUtils
import com.feng.langchain4jstarter.Main.waitTask
import com.feng.langchain4jstarter.exception.BusinessException
import com.feng.langchain4jstarter.pojo.ImageTask
import com.feng.langchain4jstarter.service.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.*


@Service
class AiServiceImpl : AiService {

    private val log = LoggerFactory.getLogger(javaClass)

    @Autowired private lateinit var assistant: Assistant
    @Autowired private lateinit var assistantStream: AssistantStream
    @Autowired private lateinit var imageGeneration: ImageGeneration
    @Autowired private lateinit var imageGenerationParam: ImageGenerationParam
    @Autowired private lateinit var imageTaskStore: ImageTaskStore
    @Autowired private lateinit var imageTaskPollingService: ImageTaskPollingService

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

    override fun imageAsync(message: String): String {
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

    /**
     * 提交图像生成任务
     *
     * 1. 调用 DashScope asyncCall 获取 taskId（通常 < 1 秒）
     * 2. 将任务状态存入 ImageTaskStore
     * 3. 丢给 @Async 线程池后台轮询，立即返回 taskId
     */
    override fun submitImageTask(userId: Long, message: String): String {
        val generationMessage = ImageGenerationMessage.builder()
            .role("user")
            .content(mutableListOf<MutableMap<String, Any>>(Collections.singletonMap("text", message)))
            .build()

        imageGenerationParam.messages = listOf(generationMessage)

        val taskResult = try {
            log.info("提交图像任务, userId={}, prompt={}", userId, message)
            imageGeneration.asyncCall(imageGenerationParam)
        } catch (e: Exception) {
            throw BusinessException(500, "提交图像生成任务失败：${e.message}")
        }

        val taskId = taskResult.output.taskId
        val task = ImageTask(taskId = taskId, userId = userId)

        // 持久化任务状态
        imageTaskStore.save(task)

        // 异步轮询，不阻塞当前线程
        imageTaskPollingService.pollUntilDone(task, apiKey)

        log.info("图像任务已提交, taskId={}", taskId)
        return taskId
    }

    /**
     * 查询任务结果
     */
    override fun getImageTask(taskId: String): ImageTask {
        return imageTaskStore.get(taskId)
            ?: throw BusinessException(404, "任务不存在，taskId=$taskId")
    }
}