package com.feng.langchain4jstarter.service.impl

import com.feng.langchain4jstarter.service.FileService
import dev.langchain4j.data.document.Document
import dev.langchain4j.data.document.DocumentParser
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser
import dev.langchain4j.data.document.splitter.DocumentSplitters
import dev.langchain4j.data.embedding.Embedding
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.store.embedding.EmbeddingMatch
import dev.langchain4j.store.embedding.EmbeddingSearchRequest
import dev.langchain4j.store.embedding.EmbeddingStore
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey
import org.apache.commons.codec.digest.DigestUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.stream.Collectors.joining

@Service
class FileServiceImpl: FileService {

    @Autowired
    private lateinit var embeddingModel: EmbeddingModel

    @Autowired
    private lateinit var embeddingStore: EmbeddingStore<TextSegment>

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

    override fun processUserUpload(userId: String, file: MultipartFile) {
        // 2. 解析文档
        val parser: DocumentParser = ApacheTikaDocumentParser()
        val document: Document = parser.parse(file.inputStream)

        // 3. 切片 (保持语义连贯)
        val splitter = DocumentSplitters.recursive(500, 50)
        val segments: MutableList<TextSegment> = splitter.split(document)

        val ids: MutableList<String> = ArrayList()
        val embeddings: MutableList<Embedding> = ArrayList()

        //1. 生成文件唯一哈希 (用于防重)
        val fileBytes: ByteArray = file.bytes
        val fileHash: String = DigestUtils.md5Hex(fileBytes)

        for (i in segments.indices) {
            val segment: TextSegment = segments[i]
            // 4. 安全：强制注入用户元数据，确保查询隔离
            segment.metadata().put("userId", userId)
            segment.metadata().put("fileHash", fileHash)

            // 5. 防重：生成确定性 ID (文件哈希 + 片段索引)
            // 这样即便用户重复上传同一个文件，Chroma 也会因为 ID 相同执行 Upsert 而非新增
            val segmentId = "$fileHash-$i"

            ids.add(segmentId)
            embeddings.add(embeddingModel.embed(segment).content())
        }

        // 6. 批量写入，提高效率
        embeddingStore.addAll(ids, embeddings, segments)
        println("用户 $userId 的文件处理完成，ID: $fileHash")
    }
}