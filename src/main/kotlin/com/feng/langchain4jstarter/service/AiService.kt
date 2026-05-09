package com.feng.langchain4jstarter.service

import org.springframework.web.multipart.MultipartFile

interface AiService {

    fun processUserUpload(userId: String, file: MultipartFile) {
        println("AiService")
    }

    fun queryDocument(documentId: String, fileHash: String): String {
        return "AiService"
    }
}