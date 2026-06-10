package com.feng.langchain4jstarter.util

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

@Component
class RateLimitUtil {
    // 使用 ConcurrentHashMap 存储所有用户的桶
    private val buckets: MutableMap<Long, Bucket> = ConcurrentHashMap<Long, Bucket>()

    fun tryConsume(userId: Long): Boolean {
        val bucket = buckets.computeIfAbsent(userId) { key: Long ->
            // 设置规则：每分钟最多 5 次调用 (生产环境建议按 Token 或 Request 次数计)
            // Bandwidth limit = Bandwidth.classic(2, Refill.greedy(2, Duration.ofMinutes(1)));
            val build = Bandwidth.builder()
                .capacity(5)
                .refillIntervally(5, Duration.ofMinutes(1))
                .build()
            Bucket.builder().addLimit(build).build()
        }
        return bucket.tryConsume(1) // 尝试消耗 1 个令牌
    }
}