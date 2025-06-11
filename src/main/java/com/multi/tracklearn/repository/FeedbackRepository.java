package com.multi.tracklearn.repository;

import com.multi.tracklearn.domain.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findTop2ByDiary_User_IdOrderByCreatedAtDesc(Long userId);
    List<Feedback> findByDiaryId(Long diaryId);

    @Query("""
    SELECT f FROM Feedback f
    WHERE f.id IN (
        SELECT MIN(f2.id) FROM Feedback f2
        WHERE f2.diary.user.id = :userId
        GROUP BY f2.diary.id
    )
    ORDER BY f.createdAt DESC
""")
    List<Feedback> findFirstFeedbacksPerDiary(@Param("userId") Long userId);


    @Query("SELECT f.responseType, COUNT(f) FROM Feedback f WHERE f.diary.user.email = :email GROUP BY f.responseType")
    List<Object[]> countTypesByUserEmail(@Param("email") String email);

    @Query("SELECT f.content FROM Feedback f WHERE f.diary.user.email = :email ORDER BY f.createdAt DESC")
    List<String> findRecentContentsByUserEmail(@Param("email") String email);

}
