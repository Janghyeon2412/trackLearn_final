package com.multi.tracklearn.service;


import com.multi.tracklearn.domain.Diary;
import com.multi.tracklearn.domain.Goal;
import com.multi.tracklearn.domain.GoalLog;
import com.multi.tracklearn.domain.User;
import com.multi.tracklearn.dto.DiarySaveDTO;
import com.multi.tracklearn.repository.DiaryRepository;
import com.multi.tracklearn.repository.GoalLogRepository;
import com.multi.tracklearn.repository.GoalRepository;
import com.multi.tracklearn.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final UserRepository userRepository;
    private final DiaryRepository diaryRepository;
    private final GoalLogRepository goalLogRepository;
    private final GoalRepository goalRepository;

    @Transactional
    public void saveDiary(String email, DiarySaveDTO diarySaveDTO) {

        User user = userRepository.findByEmail(email);  // User 반환
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        List<Long> logIds = diarySaveDTO.getCompletedGoalIds();
        List<String> retros = diarySaveDTO.getRetrospectives();

        if (logIds != null && !logIds.isEmpty()) {
            List<GoalLog> logs = goalLogRepository.findAllById(logIds);

            for (int i = 0; i < logs.size(); i++) {
                GoalLog log = logs.get(i);

                log = goalLogRepository.findById(log.getId())
                        .orElseThrow(() -> new IllegalArgumentException("GoalLog 조회 실패"));

                Goal goal = log.getGoal();
                String retrospection = (retros != null && i < retros.size()) ? retros.get(i) : "";

                Diary diary = new Diary();
                diary.setUser(user);
                diary.setGoal(goal);
                diary.setTitle(diarySaveDTO.getTitle());
                diary.setContent(diarySaveDTO.getContent());
                diary.setSummary(retrospection);
                diary.setSatisfaction(diarySaveDTO.getSatisfaction());
                diary.setStudyTime(diarySaveDTO.getStudyTime());
                diary.setDate(LocalDate.now());

                diaryRepository.save(diary);

                log.markChecked();
                goalLogRepository.save(log);
                goalLogRepository.flush();
            }

            // 달성률 재계산
            Set<Long> goalIds = logs.stream()
                    .map(log -> log.getGoal().getId())
                    .collect(Collectors.toSet());

            goalIds.forEach(this::updateGoalProgress);
        }
    }


    private void updateGoalProgress(Long goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 목표"));

        int total = goalLogRepository.countByGoal(goal);
        int completed = goalLogRepository.countByGoalAndIsCheckedIsTrue(goal);

        int rate = total == 0 ? 0 : (int) ((completed / (double) total) * 100);
        goal.updateProgress(rate);
        goalRepository.save(goal);
        goalRepository.flush();
    }

}
