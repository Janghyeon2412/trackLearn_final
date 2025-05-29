package com.multi.tracklearn.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.multi.tracklearn.domain.Diary;
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

    private String title;
    private String date;
    private int studyTime;
    private float satisfaction;
    private String content;
    private List<String> goalTitles;
    private List<String> retrospectives;

    private boolean favorite;


    public static DiaryDetailDTO fromEntity(Diary diary, List<GoalLog> goalLogs) {
        List<String> goalTitles = goalLogs.stream()
                .map(gl -> gl.getGoal().getTitle())
                .collect(Collectors.toList());

        List<String> retrospectives = diary.getRetrospectives() != null
                ? diary.getRetrospectives()
                : new ArrayList<>();

        return new DiaryDetailDTO(
                diary.getTitle(),
                diary.getDate().toString(),
                diary.getStudyTime(),
                diary.getSatisfaction(),
                diary.getContent(),
                goalTitles,
                retrospectives,
                diary.isFavorite()
        );
    }


    public int getSatisfactionInt() {
        return Math.round(this.satisfaction); // float → int
    }

}
