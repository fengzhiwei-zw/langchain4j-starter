package com.feng.langchain4jstarter.service.impl

import com.feng.langchain4jstarter.pojo.User
import com.feng.langchain4jstarter.repository.UserRepository
import com.feng.langchain4jstarter.util.LoginUser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService : UserDetailsService {
    @Autowired
    private lateinit var userRepository: UserRepository

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String?): UserDetails {
        val userEntity: User = userRepository.findByUsername(username)
        val authorities: MutableList<GrantedAuthority> = ArrayList()

        // 获取角色、权限
        // for (Role role : user.getRoles()) {
        //     authorities.add(new SimpleGrantedAuthority(role.getName()));
        //     for (Permission permission : role.getPermissions()) {
        //         authorities.add(new SimpleGrantedAuthority(permission.getName()));
        //     }
        // }
        return LoginUser(userEntity, authorities)
    }
}