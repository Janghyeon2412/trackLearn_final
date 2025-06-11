package com.multi.tracklearn.dto;

import com.multi.tracklearn.domain.Feedback;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackDTO {
    private String responseType;
    private String toneType;
    private String content;

    public static FeedbackDTO fromEntity(Feedback feedback) {
        return new FeedbackDTO(
                feedback.getResponseType().name(),
                feedback.getToneType().name(),
                feedback.getContent()
        );
    }
}
