package com.multi.tracklearn.service;

import com.multi.tracklearn.domain.Category;
import com.multi.tracklearn.domain.Goal;
import com.multi.tracklearn.domain.GoalLog;
import com.multi.tracklearn.domain.User;
import com.multi.tracklearn.dto.GoalCreateDTO;
import com.multi.tracklearn.dto.GoalListDTO;
import com.multi.tracklearn.dto.GoalUpdateDTO;
import com.multi.tracklearn.dto.TodayGoalDTO;
import com.multi.tracklearn.repository.CategoryRepository;
import com.multi.tracklearn.repository.GoalLogRepository;
import com.multi.tracklearn.repository.GoalRepository;
import com.multi.tracklearn.repository.UserRepository;
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

    public void createGoal(String email, GoalCreateDTO goalCreateDTO) {
        User user = userRepository.findByEmail(email);
        if (user == null) throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");

        // 주 3회 고정 요일 설정
        String repeatType = goalCreateDTO.getRepeatType();
        String repeatValue = goalCreateDTO.getRepeatValue();

        if ("주 3회".equals(repeatType) ||
                ("WEEKLY".equalsIgnoreCase(repeatType) && ("3".equals(repeatValue) || "주 3회".equals(repeatValue)))) {
            goalCreateDTO.setRepeatType("WEEKLY");
            goalCreateDTO.setRepeatValue("MONDAY,WEDNESDAY,FRIDAY");
        }


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
                .build();

        goalRepository.save(goal);

        // 일정 생성
        generateGoalLogs(goal, user);

    }

    private void generateGoalLogs(Goal goal, User user) {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(6);

        List<GoalLog> logs = new ArrayList<>();

        if (goal.getRepeatType() == Goal.RepeatType.CUSTOM) {
            int repeatCount = Integer.parseInt(goal.getRepeatValue());

            List<LocalDate> weekDates = new ArrayList<>();
            for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
                weekDates.add(d);
            }

            repeatCount = Math.min(repeatCount, weekDates.size());

            double interval = (double) (weekDates.size() - 1) / (repeatCount - 1);

            Set<LocalDate> selectedDates = new TreeSet<>();
            for (int i = 0; i < repeatCount; i++) {
                int index = (int) Math.round(i * interval);
                selectedDates.add(weekDates.get(index));
            }

            for (LocalDate d : selectedDates) {
                logs.add(new GoalLog(null, goal, user, d, false, null, null, null, null));
            }
        } else {
            for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                boolean include = false;

                switch (goal.getRepeatType()) {
                    case DAILY:
                        include = true;
                        break;

                    case WEEKLY:
                        List<String> repeatDays = Arrays.stream(goal.getRepeatValue().split(","))
                                .map(String::trim)
                                .map(String::toUpperCase)
                                .toList();
                        DayOfWeek currentDay = date.getDayOfWeek();
                        include = repeatDays.contains(currentDay.name());
                        break;
                }

                if (include) {
                    logs.add(new GoalLog(null, goal, user, date, false, null, null, null, null));
                }
            }
        }

        goalLogRepository.saveAll(logs);
    }



    //오늘의 목표 (GoalLog 기반)
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
                category
        );
        // 기존 GoalLog 삭제
        goalLogRepository.deleteAll(goalLogRepository.findByGoal(goal));
        goalLogRepository.flush();

        // 새 GoalLog 생성
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
                // 고정된 주 3회 날짜: 0, 3, 6 → 월수금 느낌
                int[] offsets = {0, 3, 6};
                for (int offset : offsets) {
                    logs.add(GoalLog.of(goal, start.plusDays(offset)));
                }
            }
            case CUSTOM -> {
                int repeat = Integer.parseInt(goal.getRepeatValue());
                if (repeat == 1) {
                    logs.add(GoalLog.of(goal, start)); // 오늘만
                } else {
                    for (int i = 0; i < repeat; i++) {
                        int offset = i * 6 / (repeat - 1); // 0~6 범위 균등 분포
                        logs.add(GoalLog.of(goal, start.plusDays(offset)));
                    }
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

        goal.complete(); // 완료 상태로 설정
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

    // GoalService 내부
    private List<GoalListDTO> convertToDTO(List<Goal> goals) {
        return goals.stream()
                .map(goal -> {
                    int progress = calculateProgress(goal);

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
                            goal.getCategory() != null ? goal.getCategory().getId() : null
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

        goal.softDelete();  // isDeleted = true 로 설정됨
    }

}
