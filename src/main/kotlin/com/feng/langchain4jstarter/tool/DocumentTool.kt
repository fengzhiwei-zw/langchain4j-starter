package com.feng.langchain4jstarter.tool

import com.feng.langchain4jstarter.service.FileService
import dev.langchain4j.agent.tool.Tool
import dev.langchain4j.agent.tool.ToolMemoryId
import dev.langchain4j.model.openai.OpenAiImageModel
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.time.LocalTime

@Component
class DocumentTool(
    private val fileService: FileService,
    private val openAiImageModel: OpenAiImageModel
) {

    @Tool("保存用户文件")
    fun saveUserFile(userId: String, file: MultipartFile) {
        println("【Tool Called】 $userId，${file.name}")
        fileService.processUserUpload(userId, file)
    }

    @Tool("查询文件内容")
    fun searchFileContent(@ToolMemoryId userId: String, queryText: String): String {
        println("【Tool search Called】 $userId，$queryText")
        return fileService.queryDocument(userId, queryText)
    }

    @Tool("生成图像")
    fun generateImage(message: String): String {
        println("【Tool generateImage Called】 $message")
        val response = openAiImageModel.generate(message)
        return response.content().url().toString()
    }

    @Tool("代码生成")
    fun generateCode(@ToolMemoryId userId: String, code: String, extension:String, savePath: String): String {
        val fileName = "${LocalTime.now().nano}.$extension"
        val path = "$savePath/$fileName"
        File(path).bufferedWriter(bufferSize = 64 * 1024).use { writer ->
            // 将字面量的 "\n" 替换为当前系统的标准换行符（Windows下是\r\n，Mac/Linux下是\n）
            val formattedCode = code.replace("\\n", System.lineSeparator())
            writer.write(formattedCode)
        }
        return path
    }

}