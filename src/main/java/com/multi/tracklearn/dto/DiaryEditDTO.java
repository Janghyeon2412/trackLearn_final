package com.multi.tracklearn.dto;

import com.multi.tracklearn.domain.Diary;
import com.multi.tracklearn.domain.GoalLog;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@Validated
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
    @Valid
    private List<@Size(max = 150, message = "각 회고는 최대 150자까지 입력 가능합니다.") String> retrospectives;


    private List<GoalLogCheckboxDTO> allGoalLogs;

    private String difficulty;
    private String tomorrowPlan;

    private List<String> goalDetails;
    private List<String> goalReasons;
    private List<String> learningStyles;



    // 수정용: 일지 + 해당 날짜의 GoalLog들
    public static DiaryEditDTO fromEntity(Diary diary, List<GoalLog> goalLogs) {
        DiaryEditDTO dto = new DiaryEditDTO();
        dto.setDiaryId(diary.getId());
        dto.setTitle(diary.getTitle());
        dto.setContent(diary.getContent());
        dto.setStudyTime(diary.getStudyTime());
        dto.setSatisfaction(diary.getSatisfaction());
        dto.setDate(diary.getDate().toString());
        dto.setDifficulty(diary.getDifficulty());
        dto.setTomorrowPlan(diary.getTomorrowPlan());


        List<Long> diaryGoalLogIds = diary.getGoalLogIds() != null ? diary.getGoalLogIds() : new ArrayList<>();
        List<String> diaryRetrospectives = diary.getRetrospectives() != null ? diary.getRetrospectives() : new ArrayList<>();

        // ✅ ID → GoalLog 매핑
        Map<Long, GoalLog> logMap = goalLogs.stream()
                .filter(gl -> gl != null)
                .collect(Collectors.toMap(GoalLog::getId, gl -> gl));

        // ✅ 회고 매핑 확인용 로그 추가
        System.out.println("📌 매핑 전 diaryGoalLogIds = " + diaryGoalLogIds);
        System.out.println("📌 매핑 전 diaryRetrospectives = " + diaryRetrospectives);

        // ✅ ID → 회고 매핑
        Map<Long, String> retrospectiveMap = new HashMap<>();
        for (int i = 0; i < Math.min(diaryGoalLogIds.size(), diaryRetrospectives.size()); i++) {
            retrospectiveMap.put(diaryGoalLogIds.get(i), diaryRetrospectives.get(i));
        }

        // ✅ 순서를 맞춘 GoalLog 리스트
        List<GoalLog> sortedGoalLogs = diaryGoalLogIds.stream()
                .map(logMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // ✅ 제목과 ID 추출
        List<String> titles = sortedGoalLogs.stream()
                .map(log -> log.getGoal().getTitle())
                .collect(Collectors.toList());

        List<Long> ids = sortedGoalLogs.stream()
                .map(GoalLog::getId)
                .collect(Collectors.toList());

        dto.setGoalTitles(titles);
        dto.setGoalLogIds(ids);

        // ✅ 각 Goal에 대한 상세 정보도 추출
        List<String> details = sortedGoalLogs.stream()
                .map(gl -> gl.getGoal().getGoalDetail())
                .collect(Collectors.toList());

        List<String> reasons = sortedGoalLogs.stream()
                .map(gl -> gl.getGoal().getGoalReason())
                .collect(Collectors.toList());

        List<String> styles = sortedGoalLogs.stream()
                .map(gl -> gl.getGoal().getLearningStyle())
                .collect(Collectors.toList());

        dto.setGoalDetails(details);
        dto.setGoalReasons(reasons);
        dto.setLearningStyles(styles);


        // ✅ 회고 순서 맞춰서 재정렬
        List<String> reorderedRetrospectives = ids.stream()
                .map(id -> retrospectiveMap.getOrDefault(id, ""))
                .toList();

        System.out.println("📌 정렬 후 goalLogIds = " + ids);
        System.out.println("📌 정렬 후 회고 = " + reorderedRetrospectives);

        dto.setRetrospectives(reorderedRetrospectives);

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

    public static DiaryEditDTO fromEntityWithAllGoalLogs(Diary diary, List<GoalLog> goalLogs) {
        DiaryEditDTO dto = fromEntity(diary, goalLogs);
        List<GoalLogCheckboxDTO> allLogDtos = goalLogs.stream()
                .map(GoalLogCheckboxDTO::fromEntity)
                .collect(Collectors.toList());
        dto.setAllGoalLogs(allLogDtos);

        // ✅ 여기에 회고 데이터를 명시적으로 추가해줘야 수정 페이지에 출력됨
        dto.setRetrospectives(diary.getRetrospectives());

        return dto;
    }




}
