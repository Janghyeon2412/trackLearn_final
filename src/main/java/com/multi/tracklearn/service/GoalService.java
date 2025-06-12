package com.multi.tracklearn.service;

import com.multi.tracklearn.domain.*;
import com.multi.tracklearn.dto.*;
import com.multi.tracklearn.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final GoalRepository goalRepository;
    private final GoalLogRepository goalLogRepository;
    private final UserService userService;
    private final DiaryRepository diaryRepository;


    public void createGoal(String email, GoalCreateDTO goalCreateDTO) {
        User user = userRepository.findByEmail(email);
        if (user == null) throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");


        Category category = categoryRepository.findById(goalCreateDTO.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));

        Goal goal = Goal.builder()
                .user(user)
                .category(category)
                .title(goalCreateDTO.getTitle())
                .repeatType(Goal.RepeatType.valueOf(goalCreateDTO.getRepeatType().toUpperCase()))
                .repeatValue(goalCreateDTO.getRepeatValue())
                .createdValue(LocalDate.now())
                .isCompleted(false)
                .deleted(false)
                .goalDetail(goalCreateDTO.getGoalDetail())
                .goalReason(goalCreateDTO.getGoalReason())
                .learningStyle(goalCreateDTO.getLearningStyle())

                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(7))

                .build();


        goalRepository.save(goal);

        generateGoalLogs(goal, user);

    }

    private List<Integer> getCustomRepeatOffsets(int repeat) {
        return switch (repeat) {
            case 1 -> List.of(0);
            case 2 -> List.of(0, 3);
            case 3 -> List.of(0, 3, 6);
            case 4 -> List.of(0, 2, 4, 6);
            case 5 -> List.of(0, 1, 3, 4, 6);
            case 6 -> List.of(0, 1, 2, 4, 5, 6);
            default -> throw new IllegalArgumentException("지원하지 않는 반복 횟수입니다: " + repeat);
        };
    }



    private void generateGoalLogs(Goal goal, User user) {
        LocalDate start = LocalDate.now();
        List<GoalLog> logs = new ArrayList<>();

        if (goal.getRepeatType() == Goal.RepeatType.CUSTOM) {
            int repeat = Integer.parseInt(goal.getRepeatValue());
            List<Integer> offsets = getCustomRepeatOffsets(repeat);

            for (int offset : offsets) {
                logs.add(new GoalLog(null, goal, user, start.plusDays(offset), false, null, null, null, null));
            }

        } else if (goal.getRepeatType() == Goal.RepeatType.WEEKLY) {

            if (goal.getRepeatValue().matches("\\d+")) {
                int repeat = Integer.parseInt(goal.getRepeatValue());
                List<Integer> offsets = getCustomRepeatOffsets(repeat);
                for (int offset : offsets) {
                    LocalDate d = start.plusDays(offset);
                    if (!d.isAfter(start.plusDays(6))) {
                        logs.add(new GoalLog(null, goal, user, d, false, null, null, null, null));
                    }
                }
            } else {
                // 기존 요일
                LocalDate end = start.plusDays(6);
                List<String> repeatDays = Arrays.stream(goal.getRepeatValue().split(","))
                        .map(String::trim)
                        .map(String::toUpperCase)
                        .toList();

                for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                    DayOfWeek currentDay = date.getDayOfWeek();
                    if (repeatDays.contains(currentDay.name())) {
                        logs.add(new GoalLog(null, goal, user, date, false, null, null, null, null));
                    }
                }
            }

        } else if (goal.getRepeatType() == Goal.RepeatType.DAILY) {
            for (int i = 0; i < 7; i++) {
                logs.add(new GoalLog(null, goal, user, start.plusDays(i), false, null, null, null, null));
            }
        }

        goalLogRepository.saveAll(logs);
    }




    public List<TodayGoalDTO> getTodayGoals(String email) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자"));

        LocalDate today = LocalDate.now();
        List<GoalLog> logs = goalLogRepository.findByUserAndDate(user, today);

        return logs.stream()
                .map(log -> {
                    Goal goal = log.getGoal();
                    String categoryName = goal.getCategory() != null ? goal.getCategory().getName() : "미지정";
                    String repeatText = switch (goal.getRepeatType()) {
                        case DAILY -> "매일";
                        case WEEKLY -> "주 3회";
                        case CUSTOM -> "주 " + goal.getRepeatValue() + "회";
                    };

                    return new TodayGoalDTO(
                            log.getId(),
                            goal.getId(),
                            goal.getTitle(),
                            goal.getRepeatType().name(),
                            calculateProgress(goal),
                            categoryName,
                            repeatText
                    );
                })
                .toList();
    }

    // 다가오는 일정
    public List<GoalListDTO> getUpcomingGoals(String email) {
        User user = userRepository.findByEmail(email);
        List<Goal> goals = goalRepository.findUpcomingGoals(user);
        return convertToDTO(goals);
    }



    public List<GoalListDTO> getGoals(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        List<Goal> goals = goalRepository.findByUser(user);


        return convertToDTO(goals);
    }




    @Transactional
    public void updateGoal(String email, Long goalId, GoalUpdateDTO goalUpdateDTO) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("해당 목표를 찾을 수 없습니다."));

        if (!goal.getUser().getId().equals(user.getId())) {
            throw new SecurityException("수정 권한이 없습니다.");
        }

        Category category = categoryRepository.findById(goalUpdateDTO.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));

        goal.updateGoal(
                goalUpdateDTO.getTitle(),
                Goal.RepeatType.valueOf(goalUpdateDTO.getRepeatType()),
                goalUpdateDTO.getRepeatValue(),
                category,
                goalUpdateDTO.getGoalDetail(),
                goalUpdateDTO.getGoalReason(),
                goalUpdateDTO.getLearningStyle()
        );

        goalLogRepository.deleteAll(goalLogRepository.findByGoal(goal));
        goalLogRepository.flush();

        regenerateGoalLogs(goal);
    }

    private void regenerateGoalLogs(Goal goal) {
        LocalDate start = goal.getCreatedValue();
        List<GoalLog> logs = new ArrayList<>();

        switch (goal.getRepeatType()) {
            case DAILY -> {
                for (int i = 0; i < 7; i++) {
                    logs.add(GoalLog.of(goal, start.plusDays(i)));
                }
            }

            case WEEKLY -> {
                int[] offsets = {0, 3, 6}; // 월수금 느낌
                for (int offset : offsets) {
                    logs.add(GoalLog.of(goal, start.plusDays(offset)));
                }
            }

            case CUSTOM -> {
                int repeat = Integer.parseInt(goal.getRepeatValue());
                List<Integer> offsets = getCustomRepeatOffsets(repeat);
                for (int offset : offsets) {
                    logs.add(GoalLog.of(goal, start.plusDays(offset)));
                }
            }
        }

        goalLogRepository.saveAll(logs);
    }




    @Transactional
    public void deleteGoal(String email, Long goalId) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("해당 목표를 찾을 수 없습니다."));

        if (!goal.getUser().getId().equals(user.getId())) {
            throw new SecurityException("삭제 권한이 없습니다.");
        }

        goal.setDeleted(true);

    }


    @Transactional
    public void completeGoal(String email, Long goalId) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("해당 목표를 찾을 수 없습니다."));

        if (!goal.getUser().getId().equals(user.getId())) {
            throw new SecurityException("종료 권한이 없습니다.");
        }

        goal.complete();
        goalRepository.save(goal);

        List<GoalLog> logs = goalLogRepository.findByGoal(goal);
        for (GoalLog log : logs) {
            log.setIsChecked(true);
        }
    }



    @Transactional(readOnly = true)
    public int calculateProgress(Goal goal) {
        List<GoalLog> logs = goalLogRepository.findByGoal(goal);

        long total = logs.size();
        long completed = logs.stream()
                .filter(GoalLog::getIsChecked)
                .count();

        if (total == 0) return 0;
        return (int) Math.round((completed / (double) total) * 100);

    }




    public List<GoalListDTO> getGoalsPaged(String email, Long cursor, int size) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "id"));

        List<Goal> goals = (cursor == null)
                ? goalRepository.findByUserAndDeletedFalse(user, pageable)
                : goalRepository.findByUserAndIdLessThanAndDeletedFalse(user, cursor, pageable);

        return convertToDTO(goals);
    }

    private List<GoalListDTO> convertToDTO(List<Goal> goals) {
        return goals.stream()
                .map(goal -> {
                    int progress = goal.getIsCompleted() ? 100 : calculateProgress(goal);

                    String repeatText;
                    switch (goal.getRepeatType()) {
                        case DAILY -> repeatText = "매일";
                        case WEEKLY -> repeatText = "주 3회";
                        case CUSTOM -> repeatText = "주 " + goal.getRepeatValue() + "회";
                        default -> repeatText = "";
                    }

                    return new GoalListDTO(
                            goal.getId(),
                            goal.getTitle(),
                            goal.getRepeatType().name(),
                            goal.getRepeatValue(),
                            goal.getCategory().getName(),
                            goal.getIsCompleted(),
                            goal.getCreatedValue().toString(),
                            repeatText,
                            progress,
                            goal.getCategory() != null ? goal.getCategory().getId() : null,
                            goal.getGoalDetail(),
                            goal.getGoalReason(),
                            goal.getLearningStyle()
                    );
                })
                .collect(Collectors.toList());
    }



    public List<TodayGoalDTO> getTodayGoalsByDate(String email) {
        User user = userRepository.findByEmail(email);
        LocalDate today = LocalDate.now();
        List<GoalLog> logs = goalLogRepository.findByUserAndDate(user, today);

        return logs.stream()
                .map(log -> {
                    Goal goal = log.getGoal();
                    String categoryName = goal.getCategory() != null ? goal.getCategory().getName() : "미지정";

                    String repeatText = switch (goal.getRepeatType()) {
                        case DAILY -> "매일";
                        case WEEKLY -> "주 3회";
                        case CUSTOM -> "주 " + goal.getRepeatValue() + "회";
                    };

                    return new TodayGoalDTO(
                            log.getId(),
                            goal.getId(),
                            goal.getTitle(),
                            goal.getRepeatType().name(),
                            calculateProgress(goal),
                            categoryName,
                            repeatText
                    );
                })

                .collect(Collectors.toList());
    }


    @Transactional
    public void softDeleteGoal(String email, Long goalId) {
        User user = userRepository.findByEmail(email);
        Goal goal = goalRepository.findByIdAndUser(goalId, user)
                .orElseThrow(() -> new IllegalArgumentException("해당 목표를 찾을 수 없습니다."));

        goal.softDelete();
    }

    @Transactional(readOnly = true)
    public List<CalendarGoalDTO> getCalendarGoals(String email, LocalDate startDate, LocalDate endDate) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<GoalLog> logs = goalLogRepository.findActiveByUserAndDateBetween(user, startDate, endDate);

        return logs.stream()
                .map(log -> {
                    Goal goal = log.getGoal();
                    LocalDate goalStartDate = goal.getCreatedValue();
                    LocalDate goalEndDate = goalStartDate.plusDays(7);

                    Optional<Diary> diaryOpt = diaryRepository.findByUserAndGoalLogId(user, log.getId());
                    Long diaryId = diaryOpt.map(Diary::getId).orElse(null);

                    return new CalendarGoalDTO(
                            log.getId(),
                            goal.getId(),
                            goal.getTitle(),
                            log.getDate(),
                            Boolean.TRUE.equals(log.getIsChecked()),
                            goalStartDate,
                            goalEndDate,
                            diaryId,
                            goal.getIsCompleted()
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GoalStatisticsDTO> getGoalStatistics(String email, LocalDate startDate, LocalDate endDate) {


        User user = userRepository.findByEmail(email);
        if (user == null) throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");

        List<Goal> goals = goalRepository.findByUserAndDeletedFalse(user);

        return goals.stream().map(goal -> {
            List<GoalLog> logsInRange = goalLogRepository.findByGoal(goal).stream()
                    .filter(log -> !log.getDate().isBefore(startDate) && !log.getDate().isAfter(endDate))
                    .toList();

            int totalCount = logsInRange.size();
            int checkedCount = (int) logsInRange.stream().filter(GoalLog::isChecked).count();
            int progressRate = totalCount > 0 ? (int) Math.round(checkedCount * 100.0 / totalCount) : 0;

            String categoryName = goal.getCategory() != null ? goal.getCategory().getName() : "미지정";
            String repeatText = switch (goal.getRepeatType()) {
                case DAILY -> "매일";
                case WEEKLY -> "주 3회";
                case CUSTOM -> "주 " + goal.getRepeatValue() + "회";
            };

            return new GoalStatisticsDTO(
                    goal.getId(),
                    goal.getTitle(),
                    categoryName,
                    goal.getRepeatType().name(),
                    repeatText,
                    totalCount,
                    checkedCount,
                    progressRate,
                    goal.getStartDate(),
                    goal.getEndDate()
            );
        }).toList();
    }
}