package com.multi.tracklearn.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Entity
@Table(name = "goal")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 100)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "repeat_type", nullable = false)
    private RepeatType repeatType;

    private String repeatValue;

    private LocalDate createdValue;

    private Boolean isCompleted = false;

    
    //추가
    @Column(nullable = false)
    private Boolean deleted = false;


    @Column(nullable = false)
    private int progress = 0;



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

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        this.modifiedPerson = "system";
    }

    public void markComplete() {
        this.isCompleted = true;
        this.onUpdate();
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }


    public enum RepeatType {
        DAILY, WEEKLY, CUSTOM
    }

    public void updateGoal(String title, RepeatType repeatType, String repeatValue, Category category) {
        this.title = title;
        this.repeatType = repeatType;
        this.repeatValue = repeatValue;
        this.category = category;
        this.onUpdate();
    }

    public void complete() {
        this.isCompleted = true;
        this.onUpdate();
    }

    public void softDelete() {
        this.deleted = true;
        this.onUpdate();
    }


    // Goal 엔티티 내부에 수동 추가
    public void updateProgress(int progress) {
        this.progress = progress;
        this.onUpdate();
    }


}
