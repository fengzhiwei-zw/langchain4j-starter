package com.feng.langchain4jstarter.service.impl

import com.feng.langchain4jstarter.constant.DeletedStatusEnum
import com.feng.langchain4jstarter.constant.EnableStatusEnum
import com.feng.langchain4jstarter.dto.UserSaveDTO
import com.feng.langchain4jstarter.exception.BusinessException
import com.feng.langchain4jstarter.pojo.User
import com.feng.langchain4jstarter.repository.UserRepository
import com.feng.langchain4jstarter.service.UserService
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


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

    fun findById(id: Long): User {
        return userRepository.findById(id).orElseThrow { BusinessException(404, "用户不存在") }
    }

    fun findByUsername(name: String): User {
        return userRepository.findByUsername(name).orElseThrow { BusinessException(404, "用户不存在") }
    }

    @Transactional
    fun save(userSaveDTO: UserSaveDTO): User {
        val user = User().apply {
            username = userSaveDTO.username
            email = userSaveDTO.email
            nickname = userSaveDTO.nickname
            phone = userSaveDTO.phone
            status = EnableStatusEnum.ENABLE.status
            isDeleted = DeletedStatusEnum.UN_DELETED.status
            // 关键点：调用 encode 方法
            val encodedPassword = passwordEncoder.encode(defaultPassword)
            password = encodedPassword
        }
        return userRepository.save(user)
    }

    @Transactional
    fun updateByUsername(userSaveDTO: UserSaveDTO): User {
        return userRepository.findByUsername(userSaveDTO.username)
            .orElseThrow { BusinessException(404, "用户不存在") }.apply {
                if (StringUtils.isNotBlank(userSaveDTO.phone)) {
                    phone = userSaveDTO.phone
                }
                if (StringUtils.isNotBlank(userSaveDTO.email)) {
                    email = userSaveDTO.email
                }
                if (StringUtils.isNotBlank(userSaveDTO.nickname)) {
                    nickname = userSaveDTO.nickname
                }
            }
    }

    @Transactional
    fun deleteByUsername(username: String): User {
        return userRepository.findByUsername(username).orElseThrow { BusinessException(404, "用户不存在") }
            .apply {
                isDeleted = DeletedStatusEnum.DELETED.status
            }
    }

    @Transactional
    fun updatePasswordByUsername(username: String, password: String) {
        userRepository.findByUsername(username).orElseThrow { BusinessException(404, "用户不存在") }
            .apply {
                this.password = passwordEncoder.encode(password)
            }
    }

    fun login(username: String, rawPassword: String): Boolean {
        val user: User = userRepository.findByUsername(username).orElseThrow { BusinessException(404, "用户不存在") }

        // 关键点：使用 matches 方法对比，不能直接用 equals
        return passwordEncoder.matches(rawPassword, user.password)
    }
}