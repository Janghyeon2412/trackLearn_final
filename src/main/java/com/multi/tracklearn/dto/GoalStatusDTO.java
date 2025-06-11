package com.multi.tracklearn.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class GoalStatusDTO {
    private Long goalId;
    private LocalDate date;
    private String status;
    private int progress;
}
