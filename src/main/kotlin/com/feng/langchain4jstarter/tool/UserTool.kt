package com.feng.langchain4jstarter.tool

import com.feng.langchain4jstarter.dto.UserSaveDTO
import com.feng.langchain4jstarter.exception.BusinessException
import com.feng.langchain4jstarter.pojo.User
import com.feng.langchain4jstarter.service.impl.UserServiceImpl
import dev.langchain4j.agent.tool.Tool
import dev.langchain4j.agent.tool.ToolMemoryId
import org.springframework.stereotype.Component


@Component
class UserTool(private val userService: UserServiceImpl) {

    @Tool("通过用户名查询用户")
    fun findByUsername(@ToolMemoryId userId: Long, username: String): User {
        // 模拟从 Header 获取 Token 并校验权限
        // String token = request.getHeader("Authorization");
        // if (token == null || !token.startsWith("Bearer ")) {
        //     return "错误：未授权的操作，拒绝访问订单数据。";
        // }
        println("【审计】用户正在通过用户名查询用户：$username")
        return userService.findByUsername(username)
    }

    @Tool("查询所有用户")
    fun findAll(@ToolMemoryId userId: Long): MutableList<User> {
        println("【审计】用户正在通过 AI 查询所有用户")
        if (userService.findById(userId).username != "李二") {
            throw BusinessException(403, "无权执行此操作")
        }
        return userService.findAll()
    }

    @Tool("保存用户")
    fun save(username: String, nickname: String, email: String, phone: String): User {
        println("【审计】用户正在通过 AI 保存用户")
        return userService.save(UserSaveDTO(username, nickname, email, phone))
    }

    @Tool("通过用户名修改用户信息")
    fun updateByUsername(@ToolMemoryId userId: Long, username: String, nickname: String, email: String, phone: String): User {
        println("【审计】用户正在通过 AI 修改用户")
        if (userService.findById(userId).username != "李二") {
            throw BusinessException(403, "无权执行此操作")
        }
        return userService.updateByUsername(UserSaveDTO(username, nickname, email, phone))
    }

    @Tool("修改用户密码")
    fun updatePasswordByUsername(@ToolMemoryId userId: Long, username: String, password: String, newPassword: String) {
        println("【审计】用户正在通过 AI 修改用户密码")
        if (userService.findById(userId).username != "李二") {
            throw BusinessException(403, "无权执行此操作")
        }
        userService.updatePasswordByUsername(username, password, newPassword)
    }

    @Tool("通过用户名删除用户")
    fun deleteByUsername(@ToolMemoryId userId: Long, username: String): User {
        println("【审计】用户正在通过 AI 删除用户")
        if (userService.findById(userId).username != "李二") {
            throw BusinessException(403, "无权执行此操作")
        }
        return userService.deleteByUsername(username)
    }
}