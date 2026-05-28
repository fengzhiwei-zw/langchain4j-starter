package com.feng.langchain4jstarter.tool

import com.feng.langchain4jstarter.dto.UserSaveDTO
import com.feng.langchain4jstarter.pojo.User
import com.feng.langchain4jstarter.service.impl.UserServiceImpl
import dev.langchain4j.agent.tool.Tool
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component


@Component
class UserTool {
    @Autowired
    private lateinit var request: HttpServletRequest

    @Autowired
    private lateinit var userService: UserServiceImpl

    @Tool("通过用户名查询用户")
    fun findByUsername(username: String): User {
        // 模拟从 Header 获取 Token 并校验权限
        // String token = request.getHeader("Authorization");
        // if (token == null || !token.startsWith("Bearer ")) {
        //     return "错误：未授权的操作，拒绝访问订单数据。";
        // }

        // 校验该订单是否属于当前用户（逻辑省略）

        println("【审计】用户正在通过用户名查询用户：$username")
        return userService.findByUsername(username)
    }

    @Tool("查询所有用户")
    fun findAll(): MutableList<User> {
        // 校验该订单是否属于当前用户（逻辑省略）
        println("【审计】用户正在通过 AI 查询所有用户")
        return userService.findAll()
    }

    @Tool("保存用户")
    fun save(username: String, nickname: String, email: String, phone: String): User {
        // 校验该订单是否属于当前用户（逻辑省略）
        println("【审计】用户正在通过 AI 保存用户")
        return userService.save(UserSaveDTO(username, nickname, email, phone))
    }

    @Tool("通过用户名修改用户")
    fun updateByUsername(username: String, nickname: String, email: String, phone: String): User {
        // 校验该订单是否属于当前用户（逻辑省略）
        println("【审计】用户正在通过 AI 修改用户")
        return userService.updateByUsername(UserSaveDTO(username, nickname, email, phone))
    }

    @Tool("通过用户名修改用户")
    fun updatePasswordByUsername(username: String, password: String) {
        // 校验该订单是否属于当前用户（逻辑省略）
        println("【审计】用户正在通过 AI 修改用户密码")
        userService.updatePasswordByUsername(username, password)
    }

    @Tool("通过用户名删除用户")
    fun deleteByUsername(username: String): User {
        // 校验该订单是否属于当前用户（逻辑省略）
        println("【审计】用户正在通过 AI 删除用户")
        return userService.deleteByUsername(username)
    }
}