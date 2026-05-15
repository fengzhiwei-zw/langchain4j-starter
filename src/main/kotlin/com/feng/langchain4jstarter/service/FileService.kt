package com.feng.langchain4jstarter.service

import org.springframework.web.multipart.MultipartFile

interface FileService {

    fun processUserUpload(userId: String, file: MultipartFile)

    fun queryDocument(documentId: String, queryText: String): String
}