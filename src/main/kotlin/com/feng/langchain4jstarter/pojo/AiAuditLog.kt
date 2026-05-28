package com.feng.langchain4jstarter.pojo

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import java.time.LocalDateTime


@Entity
@Table(name = "ai_audit_log")
class AiAuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        private set

    @Column(name = "user_id")
    var userId: Long? = null // 哪个用户在问
        private set
    var prompt: String? = null // 问了什么
        private set
    var response: String? = null // AI 回了什么
        private set

    @Column(name = "total_tokens")
    var totalTokens: Int? = null // 消耗了多少（哪怕是 null，也要记录）
        private set

    @Column(name = "latency_ms")
    var latencyMs: Long? = null // 响应耗时
        private set

    @Column(name = "create_time")
    @CreatedDate // 自动填入创建时间
    var createdAt: LocalDateTime? = null
        private set

    fun setId(id: Long?): AiAuditLog {
        this.id = id
        return this
    }

    fun setUserId(userId: Long?): AiAuditLog {
        this.userId = userId
        return this
    }

    fun setPrompt(prompt: String?): AiAuditLog {
        this.prompt = prompt
        return this
    }

    fun setResponse(response: String?): AiAuditLog {
        this.response = response
        return this
    }

    fun setTotalTokens(totalTokens: Int?): AiAuditLog {
        this.totalTokens = totalTokens
        return this
    }

    fun setLatencyMs(latencyMs: Long?): AiAuditLog {
        this.latencyMs = latencyMs
        return this
    }

    fun setCreatedAt(createdAt: LocalDateTime?): AiAuditLog {
        this.createdAt = createdAt
        return this
    }
}