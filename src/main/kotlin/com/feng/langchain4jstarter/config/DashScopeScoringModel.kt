package com.feng.langchain4jstarter.config

import com.fasterxml.jackson.annotation.JsonProperty
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.output.Response
import dev.langchain4j.model.scoring.ScoringModel
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class DashScopeScoringModel(
    @Value($$"${ai.dash-scope.api-key}") private val apiKey: String
) : ScoringModel {

    private val restTemplate = RestTemplate()
    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val URL = "https://dashscope.aliyuncs.com/compatible-api/v1/reranks"
        private const val MODEL = "qwen3-rerank"  // gte-rerank 已下线，统一用这个
    }

    override fun scoreAll(segments: List<TextSegment>, query: String): Response<List<Double>> {
        val documents = segments.map { it.text() }

        val requestBody = mapOf(
            "model" to MODEL,
            "query" to query,
            "documents" to documents,
            "return_documents" to false,
            "instruct" to "Given a web search query, retrieve relevant passages that answer the query.",
            "top_n" to documents.size  // 返回全部，顺序交给 LangChain4j 处理
        )

        val headers = HttpHeaders().apply {
            setBearerAuth(apiKey)
            contentType = MediaType.APPLICATION_JSON
        }

        val response = try {
            restTemplate.postForObject(
                URL, HttpEntity(requestBody, headers), RerankOutput::class.java
            )
        } catch (e: Exception) {
            log.error("调用 DashScope rerank 失败", e)
            throw RuntimeException("重排序服务调用失败: ${e.message}")
        }

        // DashScope 返回的是按相关性排序后的 index + score，需要还原成原始顺序的 score 列表
        val scoreMap = response?.results
            ?.associate { it.index to it.relevanceScore }
            ?: emptyMap()

        val scores = segments.indices.map { idx -> scoreMap[idx] ?: 0.0 }

        return Response.from(scores)
    }
}

data class RerankOutput(val results: List<RerankResult>?)
data class RerankResult(val index: Int, @JsonProperty("relevance_score") val relevanceScore: Double)