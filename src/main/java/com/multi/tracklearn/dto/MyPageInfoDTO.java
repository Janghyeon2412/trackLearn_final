package com.multi.tracklearn.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class MyPageInfoDTO {
    private String nickname;
    private String email;
    private LocalDateTime createdAt;

    private int diaryCount;
    private int totalStudyMinutes;
    private double averageSatisfaction;

    private List<RecentGoalDTO> recentGoals;
}
