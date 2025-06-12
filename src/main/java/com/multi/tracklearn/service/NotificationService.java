package com.multi.tracklearn.service;

import com.multi.tracklearn.domain.Notification;
import com.multi.tracklearn.domain.User;
import com.multi.tracklearn.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void create(User user, String content, Notification.NotificationType type) {

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setContent(content);
        notification.setType(type);
        notificationRepository.save(notification);
    }

    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));

        notification.setRead(true);
        notification.setUpdatedAt(java.time.LocalDateTime.now());
        notification.setModifiedPerson("system");
    }

    public List<Notification> getRecentNotifications(User user) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        return notificationRepository.findRecentByUser(user, threshold);
    }



}