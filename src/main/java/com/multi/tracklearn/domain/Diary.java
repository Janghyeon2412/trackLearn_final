package com.multi.tracklearn.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Diary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDate date;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(length = 30)
    private String summary;

    @Lob
    private String content;

    private int studyTime;

    private float satisfaction;

    @Column(name = "is_favorite")
    private boolean isFavorite = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(length = 50)
    private String createdPerson;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(length = 50)
    private String modifiedPerson;

    // ✅ 다중 GoalLog ID 저장 (양방향 아님)
    @ElementCollection
    @CollectionTable(name = "diary_goal_logs", joinColumns = @JoinColumn(name = "diary_id"))
    @Column(name = "goal_log_id")
    private List<Long> goalLogIds = new ArrayList<>();

    // ✅ 회고 리스트 저장 (다중 회고)
    @ElementCollection
    @CollectionTable(name = "diary_retrospectives", joinColumns = @JoinColumn(name = "diary_id"))
    @Column(name = "retrospective", length = 255)
    private List<String> retrospectives = new ArrayList<>();

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