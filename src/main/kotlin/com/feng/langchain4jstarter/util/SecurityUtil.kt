package com.feng.langchain4jstarter.util

import com.feng.langchain4jstarter.pojo.User
import org.springframework.security.core.context.SecurityContextHolder


object SecurityUtil {
    val loginUser: LoginUser
        get() {
            val authentication =
                SecurityContextHolder.getContext().authentication ?: throw RuntimeException("未登录")

            return authentication.principal as LoginUser
        }

    val user: User
        get() = loginUser.getUser()

    val userId: Long
        get() = loginUser.userId

    val username: String
        get() = loginUser.username
}