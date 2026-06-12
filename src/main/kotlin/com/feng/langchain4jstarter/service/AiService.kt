package com.feng.langchain4jstarter.service

import com.feng.langchain4jstarter.pojo.ImageTask
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

interface AiService {

    fun chat(userId: Long, message: String): String

    fun chatStream(userId: Long, message: String): SseEmitter

    fun imageAsync(message: String): String

    /** 提交图像生成任务，立即返回 taskId，不阻塞 */
    fun submitImageTask(userId: Long, message: String): String

    /** 根据 taskId 查询任务进度 */
    fun getImageTask(taskId: String): ImageTask

}