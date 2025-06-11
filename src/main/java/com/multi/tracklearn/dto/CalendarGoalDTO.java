package com.multi.tracklearn.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class CalendarGoalDTO {
    @JsonProperty("goalLogId")
    private Long goalLogId;

    @JsonProperty("goalId")
    private Long goalId;


    private String title;
    private LocalDate date;

    @JsonProperty("checked")
    private boolean isChecked;

    private LocalDate startDate;
    private LocalDate endDate;

    private Long diaryId;

    private boolean goalCompleted;

}
