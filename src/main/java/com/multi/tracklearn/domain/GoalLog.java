package com.multi.tracklearn.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Entity
@Getter
@Setter
@Table(
        name = "goal_log",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"goal_id", "user_id", "date"})
        }
)
@NoArgsConstructor
@AllArgsConstructor
public class GoalLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDate date;

    @Column(name = "is_checked", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isChecked = false;



    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(length = 50)
    private String createdPerson;

    @Column (name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(length = 50)
    private String modifiedPerson;

    public static GoalLog of(Goal goal, LocalDate date) {
        return GoalLog.builder()
                .goal(goal)
                .user(goal.getUser())
                .date(date)
                .isChecked(false)
                .build();
    }

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


    public void markChecked() {
        this.isChecked = true;
        this.onUpdate();
    }

    public boolean isChecked() {
        return Boolean.TRUE.equals(this.isChecked);
    }

    public void setChecked(boolean checked) {
        this.isChecked = checked;
    }

    public void uncheck() {
        this.isChecked = false;
        this.onUpdate();
    }


}
