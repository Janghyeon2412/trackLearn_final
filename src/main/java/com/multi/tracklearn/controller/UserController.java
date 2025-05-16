package com.multi.tracklearn.controller;

import com.multi.tracklearn.auth.JwtTokenProvider;
import com.multi.tracklearn.domain.User;
import com.multi.tracklearn.dto.ResetPasswordChangeDTO;
import com.multi.tracklearn.dto.ResetPasswordRequestDTO;
import com.multi.tracklearn.dto.UserLoginDTO;
import com.multi.tracklearn.dto.UserSignupDTO;
import com.multi.tracklearn.service.UserService;
import com.multi.tracklearn.service.UserTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserTokenService userTokenService;

    @Autowired
    public UserController(UserService userService, JwtTokenProvider jwtTokenProvider, UserTokenService userTokenService) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userTokenService = userTokenService;
    }

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody UserSignupDTO userSignupDTO) {
        if (userService.existsByEmail(userSignupDTO.getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        userService.signup(userSignupDTO);
        return ResponseEntity.ok("User created");
    }

    // 로그인 (토큰)
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody UserLoginDTO userLoginDTO) {
        try {
            User user = userService.login(userLoginDTO);

            String accessToken = jwtTokenProvider.generateToken(user);
            String refreshToken = jwtTokenProvider.generateRefreshToken(user);

            // 리프레시 토큰 저장
            LocalDateTime refreshTokenExpiry = LocalDateTime.now().plusDays(7);
            userTokenService.saveToken(user, refreshToken, refreshTokenExpiry);

            Map<String, String> response = new HashMap<>();
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 현재 사용자 이메일 반환 (인증 사용자)
    @RequestMapping(value = "/me", method = RequestMethod.GET)
    public ResponseEntity<String> getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = (String) authentication.getPrincipal();
        return ResponseEntity.ok(email);
    }

    @RequestMapping(value = "/me", method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = (String) authentication.getPrincipal();

        userService.deleteByEmail(email);
        return ResponseEntity.ok("User deleted");
    }

    @GetMapping("/check-nickname")
    public ResponseEntity<Boolean> checkNickname(@RequestParam String nickname) {
        boolean isAvailable = !userService.existsByEmail(nickname);
        return ResponseEntity.ok(isAvailable);
    }

    @PostMapping("/reset-password/request")
    public ResponseEntity<String> requestPasswordReset(@RequestBody ResetPasswordRequestDTO resetPasswordRequestDTO) {
        try {
            userService.sendResetPasswordEmail(resetPasswordRequestDTO.getEmail());
            return ResponseEntity.ok("Email sent");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Something went wrong");
        }
    }

    @PostMapping("/reset-password/confirm")
    public ResponseEntity<String> confirmPasswordReset(@RequestBody ResetPasswordChangeDTO resetPasswordChangeDTO) {
        try {
            String token = userService.resetPassword(resetPasswordChangeDTO);
            return ResponseEntity.ok(token);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshAccessToken(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refresh_token");

            // 검증, 유저 조회
            User user = userTokenService.getByRefreshToken(refreshToken).getUser();

            // access token 재발급
            String newAccessToken = jwtTokenProvider.generateToken(user);

            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Refresh token expired. Please login again"));
        }
    }
}

