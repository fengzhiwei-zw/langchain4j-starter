package com.feng.langchain4jstarter.pojo

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "ai_audit_log")
@EntityListeners(AuditingEntityListener::class) // 激活 @CreatedDate 自动填充功能
class AiAuditLog(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null, // 自增ID，未持久化前为 null，用 val 即可，JPA 会通过反射注入

    var userId: Long = 0, // 明确哪个用户在问，必填项，映射为 user_id

    @Column(length = 4096)
    var prompt: String? = null, // 问了什么，必填项

    @Column(length = 4096)
    var response: String? = null, // AI 回了什么。如果允许流式传输中断或失败，可保留可空

    var totalTokens: Int? = null, // 消耗了多少（哪怕是 null，也要记录）

    var latencyMs: Long? = null, // 响应耗时

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null // 自动填入创建时间，由 Spring Data 审计功能接管
)