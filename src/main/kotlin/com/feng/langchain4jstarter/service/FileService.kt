package com.feng.langchain4jstarter.service

import org.springframework.web.multipart.MultipartFile
import java.io.InputStream

interface FileService {

    fun processUserUpload(userId: Long, file: MultipartFile)

    fun processUserUpload(userId: Long, inputStream: InputStream, fileBytes: ByteArray)

    fun queryDocument(userId: Long, queryText: String): String

    fun saveUserFile(userId: Long, filePath: String)
}