package com.multi.tracklearn.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "user_token")
public class UserToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Lob
    @Column(name = "refresh_token", nullable = false)
    private String refreshToken;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(length = 50)
    private String createdPerson;

    @Column (name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(length = 50)
    private String modifiedPerson;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.createdPerson = "system"; // 나중에 수정 해야함.
        this.updatedAt = LocalDateTime.now();
        this.modifiedPerson = "system";
    }

    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        this.modifiedPerson = "system";
    }
}
