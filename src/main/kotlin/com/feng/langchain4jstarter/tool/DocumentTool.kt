package com.feng.langchain4jstarter.tool

import com.feng.langchain4jstarter.service.AiService
import dev.langchain4j.agent.tool.Tool
import org.springframework.web.multipart.MultipartFile

class DocumentTool(private val aiService: AiService) {

    @Tool("通过用户ID查询文件内容")
    fun search(userId: String, queryText: String): String {
        println("【Tool Called】 $userId，$queryText")
        return aiService.queryDocument(userId, queryText)
    }

    @Tool("保存用户文件")
    fun save(userId: String, file: MultipartFile) {
        println("【Tool Called】 $userId，${file.name}")
        aiService.processUserUpload(userId, file)
    }

}