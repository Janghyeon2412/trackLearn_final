package com.multi.tracklearn.repository;

import com.multi.tracklearn.domain.Diary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
    List<Diary> findByUserIdAndDateBetween(Long userId, LocalDate start, LocalDate end);
}
