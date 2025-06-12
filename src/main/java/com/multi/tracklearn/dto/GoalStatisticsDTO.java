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

    private int totalCount;
    private int checkedCount;
    private int progressRate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
}