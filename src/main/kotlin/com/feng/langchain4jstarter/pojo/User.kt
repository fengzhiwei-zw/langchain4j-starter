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
class User(
    // 1. 将主要属性放入主构造函数中，并提供默认值

    @Column(nullable = false, unique = true)
    var username: String = "",

    @Column(nullable = false)
    var password: String = "",

    var nickname: String = "",

    var email: String? = null, // 数据库允许为 null 的字段，用 String?

    var phone: String? = null,

    var status: Int = 0,

    @Column(name = "is_deleted")
    var isDeleted: Int = 0

) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0
        private set

    @Column(name = "create_time", updatable = false)
    @CreatedDate
    // 自动填入创建时间
    var createTime: LocalDateTime? = null
        private set

    @Column(name = "update_time")
    @LastModifiedDate // 自动填入修改时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    var updateTime: LocalDateTime? = null
        private set

}