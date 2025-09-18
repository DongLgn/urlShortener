package com.dong.urlshortener.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 使用 BCrypt 算法进行密码加密
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. 禁用 CSRF 保护：因为我们使用 JWT，是无状态的，不需要 CSRF
                .csrf(csrf -> csrf.disable())

                // 2. 配置会话管理为无状态 (STATELESS)：服务器不维护会话
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. 配置 HTTP 请求的授权规则
                .authorizeHttpRequests(authz -> authz
                        // a. 公开访问路径：允许任何人访问注册和登录接口
                        .requestMatchers("/api/auth/**").permitAll()

                        // b. 公开访问路径：允许任何人访问短链接重定向
                        .requestMatchers("/*").permitAll()

                        // c. 其他所有请求：都必须经过身份验证
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}