package com.multi.tracklearn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TodayGoalDTO {

    private Long goalLogId;

    private Long goalId;
    private String title;
    private String repeatType;
    private int progress;
    private String categoryName;

    private String repeatText;
}

