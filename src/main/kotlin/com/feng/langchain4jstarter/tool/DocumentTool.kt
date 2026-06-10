package com.feng.langchain4jstarter.tool

import com.feng.langchain4jstarter.service.FileService
import dev.langchain4j.agent.tool.Tool
import dev.langchain4j.agent.tool.ToolMemoryId
import dev.langchain4j.model.openai.OpenAiImageModel
import org.springframework.stereotype.Component
import java.io.File
import java.time.LocalTime

@Component
class DocumentTool(
    private val fileService: FileService,
    private val openAiImageModel: OpenAiImageModel
) {

    @Tool("保存用户文件")
    fun saveUserFile(@ToolMemoryId userId: Long, filePath: String) {
        println("【Tool saveUserFile Called】")
        fileService.saveUserFile(userId, filePath)
    }

    @Tool("查询文件内容")
    fun searchFileContent(@ToolMemoryId userId: Long, queryText: String): String {
        println("【Tool search Called】")
        return fileService.queryDocument(userId, queryText)
    }

    @Tool("生成图像")
    fun generateImage(@ToolMemoryId userId: Long, message: String): String {
        println("【Tool generateImage Called】")
        val response = openAiImageModel.generate(message)
        return response.content().url().toString()
    }

    @Tool("代码生成")
    fun generateCode(@ToolMemoryId userId: Long, code: String, extension:String, savePath: String): String {
        println("【Tool generateCode Called】")
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