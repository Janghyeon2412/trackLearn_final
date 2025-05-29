package com.multi.tracklearn.repository;

import com.multi.tracklearn.domain.Diary;
import com.multi.tracklearn.domain.GoalLog;
import com.multi.tracklearn.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DiaryRepository extends JpaRepository<Diary, Long> {

    List<Diary> findByUserIdAndDateBetween(Long userId, LocalDate start, LocalDate end);

    List<Diary> findByUserIdOrderByDateDesc(Long userId);

    Page<Diary> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Diary> findByUserIdOrderByCreatedAtAsc(Long userId, Pageable pageable);

    Page<Diary> findByUserIdOrderBySatisfactionDesc(Long userId, Pageable pageable);

    Page<Diary> findByUserIdOrderByIsFavoriteDescCreatedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT d FROM Diary d WHERE d.user = :user AND d.date = :date")
    Optional<Diary> findByUserAndDate(@Param("user") User user, @Param("date") LocalDate date);

    @Query("SELECT d FROM Diary d WHERE :goalLogId IN elements(d.goalLogIds) AND d.user = :user")
    Optional<Diary> findByUserAndGoalLogId(@Param("user") User user, @Param("goalLogId") Long goalLogId);


}
