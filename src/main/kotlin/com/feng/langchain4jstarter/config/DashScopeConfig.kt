package com.feng.langchain4jstarter.config

import com.alibaba.dashscope.aigc.imagegeneration.ImageGeneration
import com.alibaba.dashscope.aigc.imagegeneration.ImageGenerationParam
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DashScopeConfig {

    @Value($$"${ai.dash-scope.api-key}")
    private lateinit var apiKey: String

    @Value($$"${ai.dash-scope.image-model-name}")
    private lateinit var imageModelName: String

    @Value($$"${ai.dash-scope.image-url}")
    private lateinit var imageUrl: String

    @Bean
    fun imageGenerationParam(): ImageGenerationParam {
        return ImageGenerationParam.builder()
            .apiKey(apiKey)
            .model(imageModelName)
            .enableSequential(false)
            .n(1)
            .size("2K")
            .build()
    }

    @Bean
    fun imageGeneration(): ImageGeneration{
        return ImageGeneration()
    }
}