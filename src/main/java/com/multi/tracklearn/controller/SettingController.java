package com.multi.tracklearn.controller;

import jakarta.servlet.http.Cookie;
import com.multi.tracklearn.domain.User;
import com.multi.tracklearn.domain.UserStatus;
import com.multi.tracklearn.dto.NicknameUpdateDTO;
import com.multi.tracklearn.dto.PasswordChangeDTO;
import com.multi.tracklearn.dto.SettingProfileDTO;
import com.multi.tracklearn.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/profile")
    public ResponseEntity<SettingProfileDTO> getUserProfile(Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        SettingProfileDTO dto = new SettingProfileDTO(
                user.getNickname(),
                user.getEmail()
        );
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/nickname")
    public ResponseEntity<Void> updateNickname(@RequestBody NicknameUpdateDTO dto, Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        if (user == null || user.getStatus() == UserStatus.DELETED) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        user.setNickname(dto.getNickname());
        userRepository.save(user);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody PasswordChangeDTO dto, Authentication authentication,
                                            HttpServletResponse response) {
        User user = (User) authentication.getPrincipal();

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("현재 비밀번호가 일치하지 않습니다.");
        }

        if (!dto.getNewPassword().matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+=-]).{8,16}$")) {
            return ResponseEntity.badRequest().body("비밀번호는 영문, 숫자, 특수문자를 포함하여 8~16자여야 합니다.");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        // 토큰 무효화 (쿠키 삭제)
        Cookie accessToken = new Cookie("accessToken", null);
        accessToken.setPath("/");
        accessToken.setMaxAge(0);
        response.addCookie(accessToken);

        Cookie refreshToken = new Cookie("refreshToken", null);
        refreshToken.setPath("/");
        refreshToken.setHttpOnly(true);
        refreshToken.setMaxAge(0);
        response.addCookie(refreshToken);

        return ResponseEntity.ok("비밀번호가 변경되어 로그아웃되었습니다.");
    }


}