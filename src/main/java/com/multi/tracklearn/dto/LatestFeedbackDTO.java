package com.multi.tracklearn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LatestFeedbackDTO {

    private String date;
    private String feedbackContent;
    private String diaryTitle;
}
