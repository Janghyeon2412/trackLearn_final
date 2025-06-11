package com.multi.tracklearn.repository;

import com.multi.tracklearn.domain.Goal;
import com.multi.tracklearn.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GoalRepository extends JpaRepository<Goal, Long> {

    // 오늘의 목표
    @Query("SELECT g FROM Goal g WHERE g.user = :user AND g.deleted = false AND g.createdValue = CURRENT_DATE")
    List<Goal> findTodayGoals(@Param("user") User user);

    // 오늘 이후 목표 (다가오는 일정)
    @Query("SELECT g FROM Goal g WHERE g.user = :user AND g.deleted = false AND g.createdValue > CURRENT_DATE ORDER BY g.createdValue ASC")
    List<Goal> findUpcomingGoals(@Param("user") User user);

    // 전체 목표 조회 (삭제되지 않은)
    @Query("SELECT g FROM Goal g WHERE g.user = :user AND g.deleted = false ORDER BY g.createdValue DESC, g.id DESC")
    List<Goal> findByUser(@Param("user") User user);

    Optional<Goal> findByIdAndUser(Long id, User user);
    List<Goal> findByUserAndDeletedFalseOrderByCreatedValueDescIdDesc(User user);


    // 무한 스크롤 커서 기반 쿼리
    @Query("SELECT g FROM Goal g WHERE g.user = :user AND g.deleted = false AND (:cursor IS NULL OR g.id < :cursor) ORDER BY g.createdValue DESC, g.id DESC")
    List<Goal> findNextGoals(@Param("user") User user, @Param("cursor") Long cursor, Pageable pageable);


    List<Goal> findByUserAndDeletedFalse(User user, Pageable pageable);
    List<Goal> findByUserAndIdLessThanAndDeletedFalse(User user, Long cursor, Pageable pageable);

    List<Goal> findByUserAndDeletedFalseOrderByIdDesc(User user, Pageable pageable);
    List<Goal> findByUserAndIdLessThanAndDeletedFalseOrderByIdDesc(User user, Long cursor, Pageable pageable);


    List<Goal> findByUserAndDeletedFalse(User user);

}
