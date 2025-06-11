package com.multi.tracklearn.repository;

import com.multi.tracklearn.domain.Notification;
import com.multi.tracklearn.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.createdAt >= :threshold ORDER BY n.createdAt DESC")
    List<Notification> findRecentByUser(@Param("user") User user, @Param("threshold") LocalDateTime threshold);

    long countByUserAndIsReadFalse(User user);
    List<Notification> findByUserAndIsReadFalse(User user);

}