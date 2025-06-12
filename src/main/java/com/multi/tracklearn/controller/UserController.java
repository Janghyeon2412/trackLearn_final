package com.multi.tracklearn.controller;

import com.multi.tracklearn.auth.JwtTokenProvider;
import com.multi.tracklearn.auth.JwtUserAuthentication;
import com.multi.tracklearn.domain.User;
import com.multi.tracklearn.dto.ResetPasswordChangeDTO;
import com.multi.tracklearn.dto.ResetPasswordRequestDTO;
import com.multi.tracklearn.dto.UserLoginDTO;
import com.multi.tracklearn.dto.UserSignupDTO;
import com.multi.tracklearn.service.UserService;
import com.multi.tracklearn.service.UserTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
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
    public ResponseEntity<String> signup(@Valid @RequestBody UserSignupDTO userSignupDTO) {
        if (userService.existsByEmail(userSignupDTO.getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }
        userService.signup(userSignupDTO);
        return ResponseEntity.ok("User created");
    }

    @GetMapping("/check-nickname")
    public ResponseEntity<Boolean> checkNickname(@RequestParam String nickname) {
        boolean isDuplicate = userService.existsByNickname(nickname);
        return ResponseEntity.ok(isDuplicate);
    }





    // 로그인 (토큰)
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @RequestBody @Valid UserLoginDTO userLoginDTO,
            BindingResult bindingResult,
            HttpServletResponse response
    ) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            User user = userService.login(userLoginDTO);

            String accessToken = jwtTokenProvider.generateToken(user);
            String refreshToken = jwtTokenProvider.generateRefreshToken(user);

            // 리프레시 토큰 저장
            LocalDateTime refreshTokenExpiry = LocalDateTime.now().plusDays(7);
            userTokenService.saveToken(user, refreshToken, refreshTokenExpiry);

            // accessToken 쿠키 저장
            Cookie accessCookie = new Cookie("accessToken", accessToken);
            accessCookie.setHttpOnly(true);
            accessCookie.setPath("/");
            accessCookie.setMaxAge(60 * 60 * 2); // 2시간
            response.addCookie(accessCookie);

            // refreshToken 쿠키 저장
            Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
            refreshCookie.setHttpOnly(true);
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(60 * 60 * 24 * 7); // 7일
            response.addCookie(refreshCookie);

            // response body에 토큰 포함도 가능
            return ResponseEntity.ok(Map.of(
                    "accessToken", accessToken,
                    "refreshToken", refreshToken
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 현재 사용자 이메일 반환 (인증 사용자)
    @RequestMapping(value = "/me", method = RequestMethod.GET)
    public ResponseEntity<String> getCurrentUserEmail(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(user.getEmail());
    }



    @DeleteMapping("/me")
    public ResponseEntity<String> deleteCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = (User) authentication.getPrincipal();
        userService.deleteByEmail(user.getEmail());

        return ResponseEntity.ok("User deleted");
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

