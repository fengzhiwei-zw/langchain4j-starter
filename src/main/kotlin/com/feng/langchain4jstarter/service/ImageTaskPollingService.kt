package com.feng.langchain4jstarter.service

import com.alibaba.dashscope.aigc.imagegeneration.ImageGeneration
import com.feng.langchain4jstarter.pojo.ImageTask
import com.feng.langchain4jstarter.pojo.TaskStatus
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

/**
 * 异步轮询 DashScope 任务状态
 *
 * @Async 让方法在独立线程池中执行，不占用 Tomcat 请求线程。
 * 需要在启动类加 @EnableAsync 才能生效。
 */
@Service
class ImageTaskPollingService {

    private val log = LoggerFactory.getLogger(javaClass)

    @Autowired private lateinit var imageGeneration: ImageGeneration
    @Autowired private lateinit var imageTaskStore: ImageTaskStore

    companion object {
        private const val MAX_WAIT_MS = 3 * 60 * 1000L  // 最长等待 3 分钟
        private const val POLL_INTERVAL_MS = 5_000L      // 每 5 秒轮询一次
    }

    /**
     * 提交任务后立即在后台轮询，直到成功、失败或超时
     */
    @Async("imageTaskExecutor")
    fun pollUntilDone(task: ImageTask, apiKey: String) {
        val startTime = System.currentTimeMillis()
        log.info("开始轮询任务 taskId={}, userId={}", task.taskId, task.userId)

        try {
            while (System.currentTimeMillis() - startTime < MAX_WAIT_MS) {
                Thread.sleep(POLL_INTERVAL_MS)

                val result = imageGeneration.wait(task.taskId, apiKey)
                val output = result?.output ?: continue

                when (output.taskStatus) {
                    "SUCCEEDED" -> {
                        val url: String? = output.choices?.firstOrNull()?.message?.content?.firstOrNull()?.get("image") as String?
                        if (url != null) {
                            imageTaskStore.update(task.taskId) {
                                status = TaskStatus.SUCCESS
                                imageUrl = url
                            }
                            log.info("任务完成 taskId={}, url={}", task.taskId, url)
                        } else {
                            markFailed(task.taskId, "任务成功但未返回图片URL")
                        }
                        return
                    }
                    "FAILED" -> {
                        markFailed(task.taskId, result.message ?: "DashScope 任务失败")
                        return
                    }
                    // PENDING / RUNNING 继续轮询
                }
            }

            // 超时
            markFailed(task.taskId, "任务超时（超过 ${MAX_WAIT_MS / 1000} 秒）")

        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
            markFailed(task.taskId, "轮询被中断")
        } catch (e: Exception) {
            log.error("轮询任务异常 taskId={}", task.taskId, e)
            markFailed(task.taskId, "轮询异常：${e.message}")
        }
    }

    private fun markFailed(taskId: String, message: String) {
        log.warn("任务失败 taskId={}, reason={}", taskId, message)
        imageTaskStore.update(taskId) {
            status = TaskStatus.FAILED
            errorMessage = message
        }
    }
}
