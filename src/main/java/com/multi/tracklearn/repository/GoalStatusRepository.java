package com.multi.tracklearn.repository;

import com.multi.tracklearn.domain.GoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface GoalStatusRepository extends JpaRepository<GoalStatus, Long> {
    List<GoalStatus> findByGoalIdAndDateBetween(Long goalId, LocalDate start, LocalDate end);
    List<GoalStatus> findByGoal_User_EmailAndDateBetween(String email, LocalDate start, LocalDate end);
}
