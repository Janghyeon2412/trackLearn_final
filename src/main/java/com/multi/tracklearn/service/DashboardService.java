package com.multi.tracklearn.service;

import com.multi.tracklearn.domain.*;
import com.multi.tracklearn.dto.*;
import com.multi.tracklearn.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final GoalLogRepository goalLogRepository;
    private final GoalRepository goalRepository;
    private final UserRepository userRepository;
    private final DiaryRepository diaryRepository;
    private final FeedbackRepository feedbackRepository;
    private final GoalStatusRepository goalStatusRepository;



    public List<TodayGoalDTO> getTodayGoals(String email) {
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
        LocalDate today = LocalDate.now();
        List<GoalLog> todayLogs = goalLogRepository.findByUserAndDate(user, today);


        return todayLogs.stream()
                .limit(3)
                .map(log -> {
                    Goal goal = log.getGoal();
                    String categoryName = goal.getCategory() != null ? goal.getCategory().getName() : "미지정";
                    int progress = goal.getProgress();


                    String repeatText;
                    switch (goal.getRepeatType()) {
                        case DAILY -> repeatText = "매일";
                        case WEEKLY -> repeatText = "주 3회";
                        case CUSTOM -> repeatText = "주 " + goal.getRepeatValue() + "회";
                        default -> repeatText = goal.getRepeatType().name();
                    }


                    return new TodayGoalDTO(
                            log.getId(),
                            goal.getId(),
                            goal.getTitle(),
                            goal.getRepeatType().name(),
                            goal.getProgress(),
                            categoryName,
                            repeatText
                    );

                })
                .collect(Collectors.toList());
    }

    private int calculateProgress(Goal goal) {
    return Boolean.TRUE.equals(goal.getIsCompleted()) ? 100 : 0;
    }




    public WeeklyStatsDTO getWeeklyStats(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }


        LocalDate startDate = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate endDate = startDate.plusDays(6);

        // 이번 주 월~일 범위 계산
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(java.time.DayOfWeek.MONDAY);
        LocalDate sunday = monday.plusDays(6);

        List<Diary> diaries = diaryRepository.findByUserIdAndDateBetween(user.getId(), monday, sunday);

        List<Integer> dailyTime = new ArrayList<>();
        Map<LocalDate, List<Diary>> grouped = diaries.stream()
                .collect(Collectors.groupingBy(Diary::getDate));

        for (int i = 0; i < 7; i++) {
            LocalDate date = monday.plusDays(i);
            List<Diary> daily = grouped.getOrDefault(date, Collections.emptyList());
            int total = daily.stream().mapToInt(Diary::getStudyTime).sum();
            dailyTime.add(total);
        }

        int entryCount = diaries.size();

        double averageSatisfaction = diaries.stream()
                .mapToDouble(Diary::getSatisfaction)
                .average().orElse(0.0);

        String bestDay = diaries.stream()
                .max(Comparator.comparing(Diary::getSatisfaction))
                .map(d -> d.getDate().toString())
                .orElse(null);

        int achievementRate = calculateAchievementRate(user.getId(), monday, sunday);

        return new WeeklyStatsDTO(
                dailyTime,
                entryCount,
                averageSatisfaction,
                bestDay,
                achievementRate,
                startDate.format(DateTimeFormatter.ISO_DATE),
                endDate.format(DateTimeFormatter.ISO_DATE)
        );
    }

    private int calculateAchievementRate(Long userId, LocalDate start, LocalDate end) {
        List<GoalLog> logs = goalLogRepository.findByUserIdAndDateBetween(userId, start, end);
        long total = logs.size();
        long checked = logs.stream().filter(GoalLog::getIsChecked).count();
        if (total == 0) return 0;
        return (int) ((double) checked / total * 100);
    }

    public List<LatestFeedbackDTO> getLatestFeedbacks(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        List<Feedback> feedbacks = feedbackRepository.findFirstFeedbacksPerDiary(user.getId());

        return feedbacks.stream()
                .limit(2)
                .map(fb -> new LatestFeedbackDTO(
                        fb.getDiary().getId(),
                        fb.getCreatedAt().toLocalDate().toString(),
                        fb.getContent(),
                        fb.getDiary().getTitle()
                ))
                .collect(Collectors.toList());
    }







    public List<NextScheduleDTO> getNextSchedule(String email) {
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        LocalDate today = LocalDate.now();


        List<GoalLog> futureLogs = goalLogRepository.findByUserAndDateAfter(user, today);

        // 날짜별로
        Map<LocalDate, List<GoalLog>> grouped = futureLogs.stream()
                .sorted(Comparator.comparing(GoalLog::getDate))
                .collect(Collectors.groupingBy(GoalLog::getDate, LinkedHashMap::new, Collectors.toList()));

        return grouped.entrySet().stream()
                .limit(2)
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<NextScheduleGoalDTO> goalDTOs = entry.getValue().stream()
                            .map(log -> {
                                Goal goal = log.getGoal();
                                String title = goal.getTitle();
                                String category = (goal.getCategory() != null) ? goal.getCategory().getName() : "미지정";
                                return new NextScheduleGoalDTO(title, category);
                            })
                            .distinct()
                            .collect(Collectors.toList());

                    long dDay = ChronoUnit.DAYS.between(today, date);
                    String dDayStr = "D-" + dDay;

                    return new NextScheduleDTO(
                            date.toString(),
                            dDayStr,
                            goalDTOs
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CalendarGoalDTO> getCalendarGoals(String email, LocalDate start, LocalDate end) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        List<GoalLog> logs = goalLogRepository.findByUserAndDateBetween(user, start, end);
        List<Long> goalLogIds = logs.stream().map(GoalLog::getId).toList();

        Map<Long, Long> diaryMap = diaryRepository.findByUserAndGoalLogIds(user, goalLogIds).stream()
                .flatMap(d -> d.getGoalLogIds().stream()
                        .map(goalLogId -> Map.entry(goalLogId, d.getId())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return logs.stream()
                .map(log -> {
                    Goal goal = log.getGoal();
                    LocalDate date = log.getDate();
                    LocalDate startDate = goal.getCreatedValue();
                    LocalDate endDate = startDate.plusDays(7);
                    Long diaryId = diaryMap.get(log.getId());

                    return new CalendarGoalDTO(
                            log.getId(),
                            goal.getId(),
                            goal.getTitle(),
                            date,
                            Boolean.TRUE.equals(log.getIsChecked()),
                            startDate,
                            endDate,
                            diaryId,
                            goal.getIsCompleted()
                    );
                })
                .collect(Collectors.toList());
    }



    public List<GoalStatusDTO> getGoalStatusBetween(String email, LocalDate start, LocalDate end) {
        List<GoalStatus> statuses = goalStatusRepository.findByGoal_User_EmailAndDateBetween(email, start, end);
        return statuses.stream()
                .map(status -> new GoalStatusDTO(
                        status.getGoal().getId(),
                        status.getDate(),
                        status.getStatus().name(),
                        status.getProgress()
                ))
                .collect(Collectors.toList());
    }


    public GptStatsDTO getGptSummary(String email) {
        List<Object[]> counts = feedbackRepository.countTypesByUserEmail(email);
        Map<String, Integer> typeCounts = new HashMap<>();
        for (Object[] row : counts) {
            Feedback.ResponseType type = (Feedback.ResponseType) row[0];
            Long count = (Long) row[1];
            typeCounts.put(type.name().toLowerCase(), count.intValue());
        }

        List<String> recent = feedbackRepository.findRecentContentsByUserEmail(email)
                .stream()
                .limit(6)
                .collect(Collectors.toList());

        return new GptStatsDTO(typeCounts, recent);
    }


    public MonthlyStatsDTO getMonthlyStats(String email, int year, int month) {
        User user = userRepository.findByEmail(email);
        if (user == null) throw new IllegalArgumentException("사용자 없음");

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<Diary> diaries = diaryRepository.findByUserIdAndDateBetween(user.getId(), start, end);
        Map<LocalDate, List<Diary>> grouped = diaries.stream()
                .collect(Collectors.groupingBy(Diary::getDate));

        List<Integer> daily = new ArrayList<>();
        for (int i = 0; i < yearMonth.lengthOfMonth(); i++) {
            LocalDate d = start.plusDays(i);
            List<Diary> list = grouped.getOrDefault(d, List.of());
            daily.add(list.stream().mapToInt(Diary::getStudyTime).sum());
        }

        double avg = diaries.stream().mapToDouble(Diary::getSatisfaction).average().orElse(0);
        String bestDay = diaries.stream()
                .max(Comparator.comparing(Diary::getSatisfaction))
                .map(d -> d.getDate().toString())
                .orElse(null);

        int rate = calculateAchievementRate(user.getId(), start, end);

        // 연도 - 월
        String formattedMonth = yearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        return new MonthlyStatsDTO(daily, diaries.size(), avg, bestDay, rate, formattedMonth);
    }


}
