package com.multi.tracklearn.controller;

import com.multi.tracklearn.auth.JwtUserAuthentication;
import com.multi.tracklearn.domain.Notification;
import com.multi.tracklearn.domain.User;
import com.multi.tracklearn.dto.NotificationResponseDTO;
import com.multi.tracklearn.repository.NotificationRepository;
import com.multi.tracklearn.repository.UserRepository;
import com.multi.tracklearn.service.NotificationService;
import com.multi.tracklearn.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final UserService userService;
    private final NotificationRepository notificationRepository;

    // ✅ 알림 목록 조회
    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>> getUserNotifications(Authentication authentication) {
        if (!(authentication instanceof JwtUserAuthentication auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = auth.getEmail();
        User user = userRepository.findByEmail(email);

        List<Notification> notifications = notificationService.getUserNotifications(user);

        List<NotificationResponseDTO> result = notifications.stream()
                .map(NotificationResponseDTO::new)
                .toList();

        return ResponseEntity.ok(result);
    }




    // ✅ 알림 읽음 처리 (RESTful 스타일 + PATCH 방식)
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || !(authentication instanceof JwtUserAuthentication auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }


    // 안 읽은 알림 개수 반환
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadNotificationCount(@AuthenticationPrincipal User user) {
        long count = notificationRepository.countByUserAndIsReadFalse(user);

        Map<String, Long> result = new HashMap<>();
        result.put("count", count);
        return ResponseEntity.ok(result);
    }

    // 모든 알림 읽음 처리
    @PostMapping("/mark-all-read")
    public ResponseEntity<Void> markAllNotificationsAsRead(@AuthenticationPrincipal User user) {

        List<Notification> unread = notificationRepository.findByUserAndIsReadFalse(user);
        for (Notification n : unread) {
            n.setRead(true);
            n.setUpdatedAt(LocalDateTime.now());
            n.setModifiedPerson("system");
        }

        notificationRepository.saveAll(unread);
        return ResponseEntity.ok().build();
    }

}
