package com.feng.langchain4jstarter.pojo

/**
 * 图像生成任务（存储在内存 ConcurrentHashMap 中）
 * 生产环境建议替换为 Redis，支持多实例部署和持久化
 */
data class ImageTask(
    val taskId: String,                    // DashScope 返回的任务ID
    val userId: Long,
    var status: TaskStatus = TaskStatus.PENDING,
    var imageUrl: String? = null,          // 任务成功后填入
    var errorMessage: String? = null,      // 任务失败后填入
    val createdAt: Long = System.currentTimeMillis()
)

enum class TaskStatus {
    PENDING,   // 轮询中
    SUCCESS,   // 生成完成
    FAILED     // 生成失败
}
