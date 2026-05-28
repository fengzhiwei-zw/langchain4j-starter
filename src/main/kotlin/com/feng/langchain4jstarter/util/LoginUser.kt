package com.feng.langchain4jstarter.util

import com.feng.langchain4jstarter.pojo.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails


class LoginUser(private val user: User, private val authorities: MutableList<GrantedAuthority>) : UserDetails {

    fun getUser(): User {
        return user
    }

    val userId: Long
        get() = user.id

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return authorities
    }

    override fun getPassword(): String {
        return user.password
    }

    override fun getUsername(): String {
        return user.username
    }
}