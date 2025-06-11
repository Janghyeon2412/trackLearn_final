package com.multi.tracklearn.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_setting")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSetting {

    @Id
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tone tone = Tone.SOFT;

    @Column(nullable = false)
    private Boolean gptFeedbackNotify = true;

    @Column(nullable = false)
    private Boolean goalArrivalNotify = true;

    @Column(nullable = false)
    private Boolean diaryMissingNotify = true;

    @Column(nullable = false)
    private Boolean goalArrivalEmailNotify = false;

    @Column(nullable = false)
    private Boolean diaryMissingEmailNotify = false;


    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_person")
    private String createdPerson;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "modified_person")
    private String modifiedPerson;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.createdPerson = "system";
        this.updatedAt = LocalDateTime.now();
        this.modifiedPerson = "system";
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        this.modifiedPerson = "system";
    }
}
