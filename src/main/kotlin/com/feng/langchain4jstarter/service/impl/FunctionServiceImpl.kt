package com.feng.langchain4jstarter.service.impl

import com.feng.langchain4jstarter.service.AiService
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.store.embedding.EmbeddingMatch
import dev.langchain4j.store.embedding.EmbeddingSearchRequest
import dev.langchain4j.store.embedding.EmbeddingStore
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey
import org.springframework.stereotype.Service
import java.util.stream.Collectors.joining

@Service
class FunctionServiceImpl(
    private val embeddingStore: EmbeddingStore<TextSegment>, private val embeddingModel: EmbeddingModel
): AiService {

    override fun queryDocument(documentId: String, queryText: String): String {
        val queryEmbedding = embeddingModel.embed(queryText).content()
        // 生产环境必须：只查询当前用户的数据
        val searchRequest = EmbeddingSearchRequest.builder()
            .queryEmbedding(queryEmbedding)
            .filter(metadataKey("userId").isEqualTo(documentId))
            .maxResults(5)
            .build()
        val collect = embeddingStore.search(searchRequest)
            .matches().stream()
            .map { match: EmbeddingMatch<TextSegment> -> match.embedded().text() }
            .collect(joining("\n\n"))
        return collect.ifEmpty { "您没有查询此文档的权限，请联系管理员！" }
    }
}