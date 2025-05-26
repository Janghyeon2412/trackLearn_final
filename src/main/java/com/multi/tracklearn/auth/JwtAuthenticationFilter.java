package com.multi.tracklearn.auth;

import com.multi.tracklearn.domain.User;
import com.multi.tracklearn.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);
        if (token != null) {
            if (jwtTokenProvider.validateToken(token)) {
                String email = jwtTokenProvider.getEmailFromToken(token);
                JwtUserAuthentication authentication = new JwtUserAuthentication(email);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                // 액세스 토큰 만료 시 리프레시 토큰으로 재발급 시도
                String refreshToken = extractRefreshTokenFromCookie(request); // 아래 함수 직접 정의
                if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
                    String email = jwtTokenProvider.getEmailFromToken(refreshToken);
                    User user = userRepository.findByEmail(email);
                    String newAccessToken = jwtTokenProvider.generateToken(user);

                    // 응답에 새 액세스 토큰 설정 (쿠키로도 가능)
                    Cookie newCookie = new Cookie("accessToken", newAccessToken);
                    newCookie.setHttpOnly(true);
                    newCookie.setPath("/");
                    newCookie.setMaxAge(60 * 60 * 2); // 2시간
                    response.addCookie(newCookie);

                    // SecurityContext 갱신
                    JwtUserAuthentication authentication = new JwtUserAuthentication(email);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }



        filterChain.doFilter(request, response);
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if ("refreshToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }


    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
