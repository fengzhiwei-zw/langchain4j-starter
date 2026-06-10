package com.feng.langchain4jstarter.repository

import com.feng.langchain4jstarter.pojo.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, Long> {
    // 自动拥有 findById, save, delete 等方法
    fun findByUsername(username: String?): Optional<User>
}