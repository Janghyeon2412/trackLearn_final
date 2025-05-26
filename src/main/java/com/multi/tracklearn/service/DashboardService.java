package com.multi.tracklearn.service;

import com.multi.tracklearn.domain.*;
import com.multi.tracklearn.dto.*;
import com.multi.tracklearn.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
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


    public List<TodayGoalDTO> getTodayGoals(String email) {
        User user = userRepository.findByEmail(email);  // User 반환

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
        User user = userRepository.findByEmail(email);  // User 반환

        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        LocalDate today = LocalDate.now();
        LocalDate onWeekAgo = today.minusWeeks(6);
        LocalDate oneWeekAgo = today.minusDays(6);
        List<Integer> dailyTimes = new ArrayList<>();

        List<Diary> diaries = diaryRepository.findByUserIdAndDateBetween(user.getId(), oneWeekAgo, today);

        List<Integer> dailyTime = new ArrayList<>();
        Map<LocalDate, List<Diary>> grouped = diaries.stream().collect(Collectors.groupingBy(Diary::getDate));

        for(int i = 0; i < 7; i++) {
            LocalDate date = onWeekAgo.plusDays(i);
            List<Diary> daily = grouped.getOrDefault(date, Collections.emptyList());
            int total = daily.stream().mapToInt(Diary::getStudyTime).sum();
            dailyTime.add(total);
        }

        int entryCount = diaries.size();

        double averageSatisfaction = diaries.stream()
                .mapToDouble(Diary::getSatisfaction)
                .average().orElse(0.0);

        String bestDay = diaries.stream()
                .max((a, b) -> Float.compare(a.getSatisfaction(), b.getSatisfaction()))
                .map(d -> d.getDate().toString())
                .orElse(null);

        int achievementRate = calculateAchievementRate(user.getId(), oneWeekAgo, today);

        return new WeeklyStatsDTO(dailyTimes, entryCount, averageSatisfaction, bestDay, achievementRate);
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

        List<Feedback> feedbacks = feedbackRepository.findTop2ByDiary_User_IdOrderByCreatedAtDesc(user.getId());

        return feedbacks.stream()
                .map(fb-> new LatestFeedbackDTO(
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
}
