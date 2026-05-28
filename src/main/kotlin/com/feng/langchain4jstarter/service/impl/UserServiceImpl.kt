package com.feng.langchain4jstarter.service.impl

import com.feng.langchain4jstarter.constant.DeletedStatusEnum
import com.feng.langchain4jstarter.constant.EnableStatusEnum
import com.feng.langchain4jstarter.dto.UserSaveDTO
import com.feng.langchain4jstarter.pojo.User
import com.feng.langchain4jstarter.repository.UserRepository
import com.feng.langchain4jstarter.service.UserService
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*


@Service
class UserServiceImpl: UserService {
    // 注入 YAML 里的配置
    @Value($$"${ai.default.password}")
    private lateinit var defaultPassword: String
    
    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var userRepository: UserRepository

    fun findAll(): MutableList<User> {
        return userRepository.findAll()
    }

    fun findById(id: Long): Optional<User> {
        return userRepository.findById(id)
    }

    fun findByUsername(name: String): User {
        return userRepository.findByUsername(name)
    }

    @Transactional
    fun save(userSaveDTO: UserSaveDTO): User {
        val user: User = User()
        user.setUsername(userSaveDTO.username)
            .setEmail(userSaveDTO.email)
            .setNickname(userSaveDTO.nickname)
            .setPhone(userSaveDTO.phone)
            .setStatus(EnableStatusEnum.ENABLE.status)
            .setIsDeleted(DeletedStatusEnum.UN_DELETED.status)
        // 关键点：调用 encode 方法
        val encodedPassword = passwordEncoder.encode(defaultPassword)
        user.setPassword(encodedPassword)
        return userRepository.save(user)
    }

    @Transactional
    fun updateByUsername(userSaveDTO: UserSaveDTO): User {
        val byUsername: User = userRepository.findByUsername(userSaveDTO.username)
        if (StringUtils.isNotBlank(userSaveDTO.phone)) {
            byUsername.setPhone(userSaveDTO.phone)
        }
        if (StringUtils.isNotBlank(userSaveDTO.email)) {
            byUsername.setEmail(userSaveDTO.email)
        }
        if (StringUtils.isNotBlank(userSaveDTO.nickname)) {
            byUsername.setNickname(userSaveDTO.nickname)
        }
        return byUsername
    }

    @Transactional
    fun deleteByUsername(username: String): User {
        val byUsername: User = userRepository.findByUsername(username)
        byUsername.setIsDeleted(DeletedStatusEnum.DELETED.status)
        return byUsername
    }

    @Transactional
    fun updatePasswordByUsername(username: String, password: String) {
        val byUsername: User = userRepository.findByUsername(username)
        // 关键点：调用 encode 方法
        val encodedPassword = passwordEncoder.encode(password)
        byUsername.setPassword(encodedPassword)
    }

    fun login(username: String, rawPassword: String): Boolean {
        val user: User = userRepository.findByUsername(username)

        // 关键点：使用 matches 方法对比，不能直接用 equals
        return passwordEncoder.matches(rawPassword, user.password)
    }
}