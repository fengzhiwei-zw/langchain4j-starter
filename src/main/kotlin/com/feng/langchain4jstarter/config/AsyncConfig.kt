package com.feng.langchain4jstarter.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

/**
 * @EnableAsync  — 开启 @Async 注解
 * @EnableScheduling — 开启 @Scheduled（用于定期清理过期任务）
 */
@Configuration
@EnableAsync
@EnableScheduling
class AsyncConfig {

    /**
     * 图像任务专用线程池
     *
     * 文生图最多 3 分钟/次，设 10 个核心线程足够处理并发：
     *   10 线程 × 同时最多 3 min = 最多 10 张并发生成
     * maxPoolSize = 20 应对突发流量，超出进队列（容量 50），再超出拒绝
     */
    @Bean("imageTaskExecutor")
    fun imageTaskExecutor(): Executor {
        return ThreadPoolTaskExecutor().apply {
            corePoolSize = 10
            maxPoolSize = 20
            queueCapacity = 50
            setThreadNamePrefix("image-task-")
            setRejectedExecutionHandler { _, _ ->
                throw RuntimeException("图像生成队列已满，请稍后再试")
            }
            initialize()
        }
    }
}
