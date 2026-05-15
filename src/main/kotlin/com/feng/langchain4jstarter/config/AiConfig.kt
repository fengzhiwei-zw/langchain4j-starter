package com.feng.langchain4jstarter.config

import com.feng.langchain4jstarter.listener.AiCompletedListener
import com.feng.langchain4jstarter.listener.AiRequestListener
import com.feng.langchain4jstarter.listener.AiResponseListener
import com.feng.langchain4jstarter.listener.AiToolExecutedListener
import com.feng.langchain4jstarter.service.Assistant
import com.feng.langchain4jstarter.service.AssistantStream
import com.feng.langchain4jstarter.tool.DocumentTool
import com.feng.langchain4jstarter.tool.WeatherTool
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser
import dev.langchain4j.data.document.splitter.DocumentSplitters
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.memory.chat.ChatMemoryProvider
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.StreamingChatModel
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.rag.content.retriever.ContentRetriever
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever
import dev.langchain4j.service.AiServices
import dev.langchain4j.store.embedding.EmbeddingStore
import dev.langchain4j.store.embedding.chroma.ChromaApiVersion
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class AiConfig {

    // 方案一：本地小模型；还可以使用OpenAiEmbedding（yaml中自动配置）
    // @Bean
    // fun embeddingModel(): EmbeddingModel {
    //     return AllMiniLmL6V2EmbeddingModel()
    // }

    @Bean
    fun embeddingStore(): EmbeddingStore<TextSegment> {
        // 方案一：内存存储（快速测试）
        // return InMemoryEmbeddingStore()

        // 方案二：Chroma 持久化存储（推荐生产使用）
        return ChromaEmbeddingStore.builder()
            .apiVersion(ChromaApiVersion.V2)
            .baseUrl("http://localhost:8000")   // Chroma 服务地址
            .collectionName("ai-chroma-service")
            .build();
    }

    @Bean
    fun contentRetriever(
        embeddingStore: EmbeddingStore<TextSegment>,
        embeddingModel: EmbeddingModel
    ): ContentRetriever {
        // 自动将 PDF 数据注入存储（实际生产建议启动时异步加载或预热）

        //loadPdfIntoStore(embeddingStore, embeddingModel)

        return EmbeddingStoreContentRetriever.builder()
            .embeddingStore(embeddingStore)
            .embeddingModel(embeddingModel)
            .minScore(0.7)
            .maxResults(5) // 找前三个最相关的片段
            .build()
    }

    @Bean
    fun chatMemoryProvider(): ChatMemoryProvider {
        return ChatMemoryProvider { memoryId: Any? ->
            MessageWindowChatMemory.builder()
                .id(memoryId) // 每个会话独立的 Memory
                .maxMessages(10) // 保留最近 10 条消息（可根据需要调整）
                .build()
        }
    }

    @Bean
    fun assistant(
        chatModel: ChatModel,
        chatMemoryProvider: ChatMemoryProvider,
        contentRetriever : ContentRetriever,
        documentTool: DocumentTool
    ): Assistant {   // 注入 Provider

        return AiServices.builder(Assistant::class.java)
            .chatModel(chatModel)
            .chatMemoryProvider(chatMemoryProvider) // 使用 Provider（推荐）
            .contentRetriever(contentRetriever)
            .tools(WeatherTool(), documentTool)
            .registerListeners(AiRequestListener(), AiToolExecutedListener(), AiResponseListener(), AiCompletedListener())
            .build()
    }

    @Bean
    fun assistantStream(
        chatModel: StreamingChatModel,
        chatMemoryProvider: ChatMemoryProvider,
        contentRetriever : ContentRetriever,
        documentTool: DocumentTool
    ): AssistantStream {   // 注入 Provider

        return AiServices.builder(AssistantStream::class.java)
            .streamingChatModel(chatModel)
            .chatMemoryProvider(chatMemoryProvider) // 使用 Provider（推荐）
            .contentRetriever(contentRetriever)
            .tools(WeatherTool(), documentTool)
            .registerListeners(AiRequestListener(), AiToolExecutedListener(), AiResponseListener(), AiCompletedListener())
            .build()
    }

    @Deprecated("工程启动时加载文件")
    private fun loadPdfIntoStore(store: EmbeddingStore<TextSegment>, model: EmbeddingModel) {
        // 1. 加载 PDF 文件
        val document = FileSystemDocumentLoader.loadDocument(
            "C:/Users/Fengzhiwei/Downloads/核心产品：新一代多领域物理统一建模与仿真平台.docx",
            ApacheTikaDocumentParser()
        )

        // 2. 将文档切碎（每片 300 字，重叠 30 字保证语义连续）
        val splitter = DocumentSplitters.recursive(500, 100)
        val segments = splitter.split(document)
        // 5. 将切片向量化并存储
        store.addAll(model.embedAll(segments).content(), segments)
    }
}