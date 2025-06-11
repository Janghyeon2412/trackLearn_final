package com.multi.tracklearn.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.multi.tracklearn.domain.Diary;
import com.multi.tracklearn.domain.Feedback;
import com.multi.tracklearn.domain.GoalLog;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DiaryDetailDTO {

    private Long id;

    private String title;
    private String date;
    private int studyTime;
    private float satisfaction;
    private String content;
    private List<String> goalTitles;
    private List<String> retrospectives;

    private boolean favorite;

    private List<FeedbackDTO> feedbacks;

    private List<String> hardships;
    private List<String> improvements;




    public static DiaryDetailDTO fromEntity(Diary diary, List<GoalLog> goalLogs, List<Feedback> feedbacks) {
        List<String> goalTitles = goalLogs.stream()
                .map(gl -> gl.getGoal().getTitle())
                .collect(Collectors.toList());  // ← 괄호 닫힘

        List<String> retrospectives = diary.getRetrospectives() != null
                ? diary.getRetrospectives()
                : new ArrayList<>();

        List<FeedbackDTO> feedbackDTOs = feedbacks.stream()
                .map(FeedbackDTO::fromEntity)
                .toList();

        List<String> hardships = new ArrayList<>();
        if (diary.getDifficulty() != null && !diary.getDifficulty().isBlank()) {
            hardships.add(diary.getDifficulty());
        }

        List<String> improvements = new ArrayList<>();
        if (diary.getTomorrowPlan() != null && !diary.getTomorrowPlan().isBlank()) {
            improvements.add(diary.getTomorrowPlan());
        }

        return new DiaryDetailDTO(
                diary.getId(),
                diary.getTitle(),
                diary.getDate().toString(),
                diary.getStudyTime(),
                diary.getSatisfaction(),
                diary.getContent(),
                goalTitles,
                retrospectives,
                diary.isFavorite(),
                feedbackDTOs,
                hardships,
                improvements
        );
    }




    public int getSatisfactionInt() {
        return Math.round(this.satisfaction); // float → int
    }

}
