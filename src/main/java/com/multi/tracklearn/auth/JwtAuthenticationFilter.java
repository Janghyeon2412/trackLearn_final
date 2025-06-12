package com.multi.tracklearn.auth;

import com.multi.tracklearn.domain.User;
import com.multi.tracklearn.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println("JwtAuthenticationFilter");

        String token = resolveToken(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            System.out.println("accessToken 유효");

            String email = jwtTokenProvider.getEmailFromToken(token);
            User user = userRepository.findByEmail(email);

            if (user != null) {
                JwtUserAuthentication authentication = new JwtUserAuthentication(user);
                SecurityContextHolder.getContext().setAuthentication(authentication);

            }


        } else {
            System.out.println("accessToken 없음");

            String refreshToken = extractRefreshTokenFromCookie(request);
            System.out.println("refreshToken from cookie");

            if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
                System.out.println("refreshToken 유효");

                String email = jwtTokenProvider.getEmailFromToken(refreshToken);
                User user = userRepository.findByEmail(email);

                if (user != null) {
                    String newAccessToken = jwtTokenProvider.generateToken(user);
                    System.out.println("accessToken 재발급 성공");

                    Cookie newCookie = new Cookie("accessToken", newAccessToken);
                    newCookie.setHttpOnly(true);
                    newCookie.setPath("/");
                    newCookie.setMaxAge(60 * 60 * 2);
                    response.addCookie(newCookie);

                    JwtUserAuthentication authentication = new JwtUserAuthentication(user);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } else {
                System.out.println("refreshToken 없음");
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
        System.out.println("[resolveToken] Authorization Header: " + bearer);

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                System.out.println("[resolveToken] Cookie: " + cookie.getName() + " = " + cookie.getValue());
            }
        } else {
            System.out.println("[resolveToken] request.getCookies() is null");
        }

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
