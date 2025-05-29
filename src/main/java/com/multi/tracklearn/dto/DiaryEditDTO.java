package com.multi.tracklearn.dto;

import com.multi.tracklearn.domain.Diary;
import com.multi.tracklearn.domain.GoalLog;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class DiaryEditDTO {

    private Long diaryId;
    private String title;
    private String content;
    private int studyTime;
    private float satisfaction;
    private String date;

    // ✅ 여러 개의 목표 제목
    private List<String> goalTitles;

    // ✅ 여러 개의 GoalLog ID
    private List<Long> goalLogIds;

    private Long goalLogId;

    // ✅ 회고 다중 입력
    private List<String> retrospectives;

    // 수정용: 일지 + 해당 날짜의 GoalLog들
    public static DiaryEditDTO fromEntity(Diary diary, List<GoalLog> goalLogs) {
        DiaryEditDTO dto = new DiaryEditDTO();
        dto.setDiaryId(diary.getId());
        dto.setTitle(diary.getTitle());
        dto.setContent(diary.getContent());
        dto.setStudyTime(diary.getStudyTime());
        dto.setSatisfaction(diary.getSatisfaction());
        dto.setDate(diary.getDate().toString());

        List<String> titles = new ArrayList<>();
        List<Long> ids = new ArrayList<>();
        for (GoalLog log : goalLogs) {
            if (log != null && log.getGoal() != null) {
                titles.add(log.getGoal().getTitle());
                ids.add(log.getId());
            }
        }
        dto.setGoalTitles(titles);
        dto.setGoalLogIds(ids);

        dto.setRetrospectives(
                diary.getRetrospectives() != null ? diary.getRetrospectives() : new ArrayList<>()
        );
        return dto;
    }

    // 새 일지 작성용: goalLogs만 있을 경우
    public static DiaryEditDTO fromGoalLogs(List<GoalLog> goalLogs) {
        DiaryEditDTO dto = new DiaryEditDTO();
        if (!goalLogs.isEmpty()) {
            dto.setDate(goalLogs.get(0).getDate().toString());
        }

        List<String> titles = new ArrayList<>();
        List<Long> ids = new ArrayList<>();
        for (GoalLog goalLog : goalLogs) {
            if (goalLog.getGoal() != null) {
                titles.add(goalLog.getGoal().getTitle());
                ids.add(goalLog.getId());
            }
        }

        dto.setGoalTitles(titles);
        dto.setGoalLogIds(ids);
        dto.setRetrospectives(new ArrayList<>());
        return dto;
    }
}
