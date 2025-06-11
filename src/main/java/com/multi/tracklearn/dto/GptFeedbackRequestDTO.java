package com.multi.tracklearn.dto;

import com.multi.tracklearn.domain.Diary;
import com.multi.tracklearn.domain.GoalLog;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Data
public class GptFeedbackRequestDTO {

    private Long diaryId;

    private String title;
    private String content;
    private int studyTime;
    private int satisfaction;
    private List<String> goals;
    private List<String> retrospectives;
    private String tone;
    private String subject;
    private List<String> goalDetails;
    private List<String> goalReasons;
    private List<String> learningStyles;

    private String difficulty;
    private String tomorrowPlan;

    public static GptFeedbackRequestDTO fromDiary(Diary diary, List<GoalLog> goalLogs) {
        List<String> goals = goalLogs.stream()
                .map(gl -> gl.getGoal().getTitle())
                .toList();
        List<String> retrospectives = diary.getRetrospectives() != null
                ? diary.getRetrospectives()
                : List.of();

        List<String> goalDetails = goalLogs.stream()
                .map(gl -> gl.getGoal().getGoalDetail())
                .toList();
        List<String> goalReasons = goalLogs.stream()
                .map(gl -> gl.getGoal().getGoalReason())
                .toList();
        List<String> learningStyles = goalLogs.stream()
                .map(gl -> gl.getGoal().getLearningStyle())
                .toList();

        GptFeedbackRequestDTO dto = new GptFeedbackRequestDTO();
        dto.setDiaryId(diary.getId());
        dto.setTitle(diary.getTitle());
        dto.setContent(diary.getContent());
        dto.setStudyTime(diary.getStudyTime());
        dto.setSatisfaction((int) diary.getSatisfaction());
        dto.setGoals(goals);
        dto.setRetrospectives(retrospectives);
        dto.setGoalDetails(goalDetails);
        dto.setGoalReasons(goalReasons);
        dto.setLearningStyles(learningStyles);
        dto.setDifficulty(diary.getDifficulty());
        dto.setTomorrowPlan(diary.getTomorrowPlan());
        dto.setTone("친절하고 격려하는");
        dto.setSubject("학습 피드백");

        return dto;
    }



}
