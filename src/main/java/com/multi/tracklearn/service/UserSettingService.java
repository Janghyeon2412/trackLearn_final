package com.multi.tracklearn.service;

import com.multi.tracklearn.domain.*;
import com.multi.tracklearn.repository.UserRepository;
import com.multi.tracklearn.repository.UserSettingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserSettingService {

    private final UserSettingRepository userSettingRepository;
    private final UserRepository userRepository;

    // 설정 조회
    public UserSetting getSetting(Long userId) {
        return userSettingRepository.findById(userId)
                .orElseGet(() -> createDefaultSetting(userId));
    }

    // GPT 톤 변경
    @Transactional
    public void updateTone(Long userId, Tone tone) {
        UserSetting setting = getSetting(userId);
        setting.setTone(tone);
    }

    // 알림 설정
    @Transactional
    public void updateNotifications(Long userId, boolean gpt, boolean goalArrival, boolean diaryMissing) {
        UserSetting setting = getSetting(userId);
        setting.setGptFeedbackNotify(gpt);
        setting.setGoalArrivalNotify(goalArrival);
        setting.setDiaryMissingNotify(diaryMissing);
    }

    @Transactional
    public UserSetting createDefaultSetting(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        UserSetting setting = UserSetting.builder()
                .user(user)
                .tone(Tone.SOFT)
                .gptFeedbackNotify(true)
                .goalArrivalNotify(true)
                .diaryMissingNotify(true)
                .goalArrivalEmailNotify(true)
                .diaryMissingEmailNotify(true)
                .build();

        return userSettingRepository.save(setting);
    }
}