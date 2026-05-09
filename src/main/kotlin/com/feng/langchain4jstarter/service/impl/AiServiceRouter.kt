package com.feng.langchain4jstarter.service.impl

import com.feng.langchain4jstarter.service.AiService
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
@Primary
class AiServiceRouter(
    private val aiServiceImpl: AiServiceImpl,
    private val functionServiceImpl: FunctionServiceImpl
) : AiService {

    override fun processUserUpload(userId: String, file: MultipartFile) {
        aiServiceImpl.processUserUpload(userId, file)
    }
    override fun queryDocument(documentId: String, fileHash: String): String {
        return functionServiceImpl.queryDocument(documentId, fileHash)
    }
}