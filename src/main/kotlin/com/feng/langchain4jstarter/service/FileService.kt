package com.feng.langchain4jstarter.service

import org.springframework.web.multipart.MultipartFile
import java.io.InputStream

interface FileService {

    fun processUserUpload(userId: String, file: MultipartFile)

    fun processUserUpload(userId: String, inputStream: InputStream, fileBytes: ByteArray)

    fun queryDocument(documentId: String, queryText: String): String

    fun saveUserFile(userId: String, filePath: String)
}