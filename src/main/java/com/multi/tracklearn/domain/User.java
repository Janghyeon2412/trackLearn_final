package com.multi.tracklearn.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table (name = "user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(length = 255)
    private String profileImageUrl;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    // 추가
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    // 추가(탈퇴한 게정 구분)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    // 추가 (로그인 실패 횟수)
    @Column(name = "login_fail_count")
    private Integer loginFailCount = 0;

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
