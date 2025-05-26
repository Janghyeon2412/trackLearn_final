package com.multi.tracklearn.repository;

import com.multi.tracklearn.domain.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findTop2ByDiary_User_IdOrderByCreatedAtDesc(Long userId);
}
