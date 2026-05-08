package com.feng.langchain4jstarter.service

import dev.langchain4j.data.segment.TextSegment
import org.springframework.web.multipart.MultipartFile

interface AiService {

    fun processUserUpload(userId: String, file: MultipartFile)

    fun search(userId: String, queryText: String): List<TextSegment>
}