package com.multi.tracklearn.service;

import com.multi.tracklearn.domain.Goal;
import com.multi.tracklearn.domain.GoalLog;
import com.multi.tracklearn.repository.GoalLogRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoalLogRescheduler {

    private final GoalLogRepository goalLogRepository;

    // 매일 새벽 1시 실행
    @Scheduled(cron = "0 0 1 * * *")
    public void reassignUncompletedGoals() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<GoalLog> uncompletedLogs = goalLogRepository.findByDateAndIsCheckedFalse(yesterday);

        for (GoalLog goalLog : uncompletedLogs) {
            Goal goal = goalLog.getGoal();

            // 이미 완료되었거나 삭제된 목표는 건너뜀
            if (goal.getIsCompleted() || goal.getDeleted()) continue;

            // 남은 날짜 중 빈 날짜 찾기 (이 주에 한정)
            Optional<LocalDate> nextDate = findNextAvailableDate(goal);

            nextDate.ifPresent(date -> {
                GoalLog newLog = GoalLog.of(goal, date);
                goalLogRepository.save(newLog);
                log.info("목표 [{}] 가 {} → {} 로 재배정됨", goal.getTitle(), yesterday, date);
            });
        }
    }

    private Optional<LocalDate> findNextAvailableDate(Goal goal) {
        LocalDate today = LocalDate.now();
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);

        List<LocalDate> candidates = new ArrayList<>();
        for (LocalDate date = today; !date.isAfter(endOfWeek); date = date.plusDays(1)) {
            candidates.add(date);
        }

        // 무작위 섞기
        Collections.shuffle(candidates);

        for (LocalDate date : candidates) {
            boolean exists = goalLogRepository.existsByGoalAndDate(goal, date);
            if (!exists) {
                return Optional.of(date);
            }
        }

        return Optional.empty(); // 가능한 날짜가 없으면
    }


    @PostConstruct
    public void testReassignmentNow() {
        reassignUncompletedGoals();
    }

}