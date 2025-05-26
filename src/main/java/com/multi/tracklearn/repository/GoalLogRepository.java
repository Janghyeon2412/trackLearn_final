package com.multi.tracklearn.repository;

import com.multi.tracklearn.domain.Goal;
import com.multi.tracklearn.domain.GoalLog;
import com.multi.tracklearn.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface GoalLogRepository extends JpaRepository<GoalLog, Long> {


    @Query("SELECT gl FROM GoalLog gl WHERE gl.user.id = :userId AND gl.date BETWEEN :start AND :end AND gl.goal.deleted = false")
    List<GoalLog> findByUserIdAndDateBetween(@Param("userId") Long userId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT gl FROM GoalLog gl WHERE gl.user.id = :userId AND gl.date = :date AND gl.goal.deleted = false")
    List<GoalLog> findByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);


    List<GoalLog> findByUserIdAndDateAfterOrderByDateAsc(Long userId, LocalDate date);

    List<GoalLog> findByGoal(Goal goal);

    // 오늘의 목표만 조회
    @Query("SELECT gl FROM GoalLog gl WHERE gl.user = :user AND gl.date = :date AND gl.goal.deleted = false AND gl.goal.isCompleted = false")
    List<GoalLog> findByUserAndDate(@Param("user") User user, @Param("date") LocalDate date);



    // 오늘 이후 목표만 조회 (오늘은 제외)
    @Query("SELECT gl FROM GoalLog gl WHERE gl.user = :user AND gl.date > :date AND gl.goal.deleted = false AND gl.goal.isCompleted = false")
    List<GoalLog> findByUserAndDateAfter(@Param("user") User user, @Param("date") LocalDate date);


    // 수정 시 GoalLog 삭제 후 재생성
    void deleteByGoal(Goal goal);


    int countByGoal(Goal goal);

    int countByGoalAndIsCheckedIsTrue(Goal goal);

    @Query("SELECT gl FROM GoalLog gl WHERE gl.date = :date AND gl.isChecked = false AND gl.goal.deleted = false")
    List<GoalLog> findByDateAndIsCheckedFalse(LocalDate date);

    boolean existsByGoalAndDate(Goal goal, LocalDate date);



}