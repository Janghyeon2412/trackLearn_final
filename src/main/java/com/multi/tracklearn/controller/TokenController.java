package com.multi.tracklearn.controller;

import com.multi.tracklearn.auth.JwtTokenProvider;
import com.multi.tracklearn.domain.User;
import com.multi.tracklearn.domain.UserToken;
import com.multi.tracklearn.repository.UserTokenRepository;
import com.multi.tracklearn.service.UserService;
import com.multi.tracklearn.service.UserTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/users")
public class TokenController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserTokenService userTokenService;
    private final UserService userService;

    @Autowired
    public TokenController(JwtTokenProvider jwtTokenProvider, UserTokenService userTokenService, UserService userService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userTokenService = userTokenService;
        this.userService = userService;
    }

    public ResponseEntity<String> reissueToken(@RequestHeader("Authorization") String bearerToken) {
        try {
            // Bearer 제거
            String refreshToken = bearerToken.replace("Bearer ", "");

            //토큰 유효성
            if (!jwtTokenProvider.validateToken(refreshToken)) {
                return ResponseEntity.badRequest().body("Invalid token");
            }

            // DB에 저장된 토큰과 비교
            UserToken storedToken = userTokenService.getByRefreshToken(refreshToken);
            if (storedToken.getExpiresDate().isBefore(java.time.LocalDateTime.now())) {
                return ResponseEntity.badRequest().body("Expired token");
            }

            User user = storedToken.getUser();
            String newAccessToken = jwtTokenProvider.generateToken(user);
            return ResponseEntity.ok(newAccessToken);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid token");
        }
    }
}
