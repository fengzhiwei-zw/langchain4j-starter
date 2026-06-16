package com.feng.langchain4jstarter.controller

import com.feng.langchain4jstarter.dto.UserSaveDTO
import com.feng.langchain4jstarter.model.ApiResponse
import com.feng.langchain4jstarter.pojo.User
import com.feng.langchain4jstarter.service.impl.UserServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class LoginController {

    @Autowired private lateinit var userService: UserServiceImpl

    @PostMapping("/register")
    fun register(@RequestBody userSaveDTO: UserSaveDTO): ApiResponse<User> {
        return ApiResponse.success(userService.save(userSaveDTO))
    }
}