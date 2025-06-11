package com.multi.tracklearn.dto;

import com.multi.tracklearn.domain.Notification;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class NotificationResponseDTO {
    private final Long id;
    private final String content;
    private final boolean isRead;
    private final LocalDateTime createdAt;

    public NotificationResponseDTO(Notification n) {
        this.id = n.getId();
        this.content = n.getContent();
        this.isRead = n.isRead();
        this.createdAt = n.getCreatedAt();
    }
}
