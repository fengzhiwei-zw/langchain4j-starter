package com.feng.langchain4jstarter.pojo

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime


@Entity
@Table(name = "user")
@EntityListeners(AuditingEntityListener::class) // 开启审计监听
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0
        private set
    lateinit var username: String
        private set
    lateinit var password: String
        private set
    lateinit var nickname: String
        private set
    lateinit var email: String
        private set
    lateinit var phone: String
        private set
    var status: Int = 0
        private set

    @Column(name = "is_deleted")
    var isDeleted: Int = 0
        private set

    @Column(name = "create_time")
    @CreatedDate
    lateinit // 自动填入创建时间
    var createTime: LocalDateTime
        private set

    @Column(name = "update_time")
    @LastModifiedDate // 自动填入修改时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    lateinit var updateTime: LocalDateTime
        private set

    fun setId(id: Long): User {
        this.id = id
        return this
    }


    fun setUsername(username: String): User {
        this.username = username
        return this
    }


    fun setPassword(password: String): User {
        this.password = password
        return this
    }


    fun setNickname(nickname: String): User {
        this.nickname = nickname
        return this
    }


    fun setEmail(email: String): User {
        this.email = email
        return this
    }


    fun setPhone(phone: String): User {
        this.phone = phone
        return this
    }


    fun setStatus(status: Int): User {
        this.status = status
        return this
    }


    fun setCreateTime(createTime: LocalDateTime): User {
        this.createTime = createTime
        return this
    }

    fun setUpdateTime(updateTime: LocalDateTime): User {
        this.updateTime = updateTime
        return this
    }

    fun setIsDeleted(isDeleted: Int): User {
        this.isDeleted = isDeleted
        return this
    }
}