package com.multi.tracklearn.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class GoalStatisticsDTO {
    private Long goalId;
    private String title;
    private String categoryName;
    private String repeatType;
    private String repeatText;

    private int totalCount;     // 전체 수행 예정 횟수
    private int checkedCount;   // 실제 수행(체크)된 횟수
    private int progressRate;   // 달성률 %

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
}