package com.multi.tracklearn.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class RecentGoalDTO {
    private String title;
    private String categoryName;
    private LocalDate date;
}
