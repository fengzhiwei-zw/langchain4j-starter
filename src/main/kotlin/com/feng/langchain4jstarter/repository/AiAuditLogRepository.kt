package com.feng.langchain4jstarter.repository

import com.feng.langchain4jstarter.pojo.AiAuditLog
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
interface AiAuditLogRepository : JpaRepository<AiAuditLog, Long>