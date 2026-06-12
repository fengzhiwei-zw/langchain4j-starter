package com.feng.langchain4jstarter.service

import com.feng.langchain4jstarter.pojo.ImageTask
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

/**
 * 任务状态存储
 *
 * 当前用 ConcurrentHashMap（单机内存），适合学习和单节点部署。
 * 生产多实例场景替换为 Redis：
 *   redisTemplate.opsForValue().set("image:task:$taskId", task, 1, TimeUnit.HOURS)
 */
@Component
class ImageTaskStore {

    private val store = ConcurrentHashMap<String, ImageTask>()

    fun save(task: ImageTask) {
        store[task.taskId] = task
    }

    fun get(taskId: String): ImageTask? = store[taskId]

    fun update(taskId: String, block: ImageTask.() -> Unit) {
        store[taskId]?.apply(block)
    }

    /** 清理超过 2 小时的任务，防止内存无限增长（可配合 @Scheduled 定时调用） */
    fun evictExpired() {
        val threshold = System.currentTimeMillis() - 2 * 60 * 60 * 1000
        store.entries.removeIf { it.value.createdAt < threshold }
    }
}
