package com.multi.tracklearn.config;

import com.multi.tracklearn.auth.JwtAuthenticationFilter;
import com.multi.tracklearn.auth.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class  SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/signup/**",  // 회원가입 페이지 허용
                        "/css/**",     // css 허용
                        "/error",     // 오류 페이지 허용
                        "/login",
                        "/js/**",
                        "/images/**",
                        "/users/signup",
                        "/users/login",
                        "/users/check-nickname",
                        "/users/refresh",
                        "/users/reset-password/**",
                        "/categories"
                ).permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/users/me").hasRole("USER")// 삭제
                        .requestMatchers(HttpMethod.GET, "/users/me").hasRole("USER") // 조회
                .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
