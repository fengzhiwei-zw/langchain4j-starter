package com.feng.langchain4jstarter.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.*
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource


@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Value($$"${spring.cors.origins}")
    private lateinit var origins: String

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        // BCrypt 是一种自适应哈希算法，自带随机盐，极其安全
        return BCryptPasswordEncoder()
    }

    @Bean
    @Throws(Exception::class)
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager {
        return config.getAuthenticationManager()
    }

    // JWT
    // @Bean
    // public JwtAuthenticationFilter jwtAuthenticationFilter() {
    //     return new JwtAuthenticationFilter();
    // }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = origins.split(",") // 或您的前端地址
        configuration.setAllowedMethods(listOf("GET", "POST", "PUT", "DELETE", "OPTIONS"))
        configuration.allowedHeaders = mutableListOf("*")
        configuration.allowCredentials = true
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    @Bean
    @Throws(java.lang.Exception::class)
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http // ❌ 关闭 CSRF（单体 + 表单/接口混用常见做法）
            .csrf(Customizer { obj: CsrfConfigurer<HttpSecurity> -> obj.disable() }) // ✅ 使用 Session（默认就是 IF_REQUIRED，这里可以不写）

            .sessionManagement(Customizer { session: SessionManagementConfigurer<HttpSecurity> ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.IF_REQUIRED
                )
            }
            ) // .formLogin(AbstractHttpConfigurer::disable)
            // ✅ 使用 Spring Security 默认登录页

            .formLogin(Customizer { form: FormLoginConfigurer<HttpSecurity> ->
                form
                    .loginPage("/login.html") // 自定义登录页
                    .loginProcessingUrl("/login") // 登录接口
                    // .defaultSuccessUrl("/index.html", true)
                    .usernameParameter("username")
                    .passwordParameter("password")
                    .permitAll()
                    .successHandler(AuthenticationSuccessHandler { request: HttpServletRequest, response: HttpServletResponse, authentication: Authentication ->
                        response.contentType = "application/json;charset=UTF-8"
                        response.writer.write("{\"status\":\"success\",\"msg\":\"登录成功\"}")
                    })
                    .failureHandler(AuthenticationFailureHandler { request: HttpServletRequest, response: HttpServletResponse, exception: AuthenticationException ->
                        response.contentType = "application/json;charset=UTF-8"
                        response.status = HttpServletResponse.SC_UNAUTHORIZED
                        response.writer.write("{\"status\":\"error\",\"msg\":\"用户名或密码错误\"}")
                    })
            }
            )

            .logout(Customizer { logout: LogoutConfigurer<HttpSecurity> ->
                logout
                    .logoutUrl("/logout")
                    .logoutSuccessHandler(LogoutSuccessHandler { request: HttpServletRequest, response: HttpServletResponse, authentication: Authentication ->
                        response.contentType = "application/json;charset=UTF-8"
                        response.writer.write("{\"status\":\"success\",\"msg\":\"已退出登录\"}")
                    })
                    .deleteCookies("JSESSIONID")
                    .invalidateHttpSession(true)
            }
            ) // ❌ 不用 JWT
            // .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            // ❌ HTTP Basic 禁用

            .httpBasic(Customizer { obj: HttpBasicConfigurer<HttpSecurity> -> obj.disable() }) // ✅ CORS

            .cors(Customizer { cors: CorsConfigurer<HttpSecurity> ->
                cors.configurationSource(
                    corsConfigurationSource()
                )
            }) // 🔐 权限配置

            .authorizeHttpRequests { auth ->
                auth.requestMatchers(
                    "/login.html",
                    "/login",
                    "/register",
                    "/css/**",
                    "/js/**",
                    "/images/**"
                ).permitAll() // 主页必须登录（但登录成功后自动可访问）
                    .requestMatchers("/index.html").authenticated()
                    .anyRequest().authenticated()
            }

            .build()
    }

}