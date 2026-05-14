package com.feng.langchain4jstarter.tool

import com.feng.langchain4jstarter.service.AiService
import dev.langchain4j.agent.tool.Tool
import dev.langchain4j.agent.tool.ToolMemoryId
import dev.langchain4j.model.openai.OpenAiImageModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.multipart.MultipartFile

class DocumentTool(private val aiService: AiService) {

    @Autowired
    private lateinit var openAiImageModel: OpenAiImageModel

    @Tool("保存用户文件")
    fun saveUserFile(userId: String, file: MultipartFile) {
        println("【Tool Called】 $userId，${file.name}")
        aiService.processUserUpload(userId, file)
    }

    @Tool("查询文件内容")
    fun searchFileContent(@ToolMemoryId userId: String, queryText: String): String {
        println("【Tool search Called】 $userId，$queryText")
        return aiService.queryDocument(userId, queryText)
    }

    @Tool("生成图像")
    fun generateImage(message: String): String {
        println("【Tool generateImage Called】 $message")
        val response = openAiImageModel.generate(message)
        return response.content().url().toString()
    }

}