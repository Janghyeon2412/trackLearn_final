package com.multi.tracklearn.config;
import com.multi.tracklearn.auth.JwtAuthenticationFilter;
import com.multi.tracklearn.auth.JwtTokenProvider;
import com.multi.tracklearn.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class  SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
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
                        "/",
                        "/signup/**",
                        "/css/**",
                        "/error",
                        "/login",
                        "/js/**",
                        "/images/**",
                        "/users/signup",
                        "/users/login",
                        "/users/check-nickname",
                        "/users/refresh",
                        "/users/reset-password/**",
                        "/categories",
                        "/diary/write"
                ).permitAll()
                                .requestMatchers(HttpMethod.DELETE, "/users/me").hasRole("USER")
                                .requestMatchers(HttpMethod.GET, "/users/me").hasRole("USER")
                                .requestMatchers("/api/dashboard/**").hasAuthority("ROLE_USER")
                                .requestMatchers("/main").authenticated()
                                .requestMatchers("/goals/**").authenticated()
                                .requestMatchers("/diary/write").authenticated()
                                .requestMatchers("/api/mypage/**").authenticated()
                                .requestMatchers("/api/diary/**").hasAuthority("ROLE_USER")


                                .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, userRepository), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of("http://localhost:8081"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
