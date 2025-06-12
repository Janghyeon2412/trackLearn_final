package com.multi.tracklearn.controller;

import com.multi.tracklearn.domain.Tone;
import com.multi.tracklearn.domain.User;
import com.multi.tracklearn.domain.UserSetting;
import com.multi.tracklearn.repository.UserRepository;
import com.multi.tracklearn.service.UserSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class UserSettingController {

    private final UserSettingService userSettingService;
    private final UserRepository userRepository;

    // 사용자 설정조회
    @GetMapping
    public ResponseEntity<Map<String, Object>> getSettings(@AuthenticationPrincipal User user) {
        UserSetting setting = userSettingService.getSetting(user.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("tone", setting.getTone());
        result.put("gptFeedbackNotify", setting.getGptFeedbackNotify());
        result.put("goalArrivalNotify", setting.getGoalArrivalNotify());
        result.put("diaryMissingNotify", setting.getDiaryMissingNotify());

        return ResponseEntity.ok(result);
    }


    // GPT 말투 변경
    @PatchMapping("/gpt")
    public ResponseEntity<Void> updateTone(@AuthenticationPrincipal User user,
                                           @RequestBody Map<String, String> body) {
        Tone tone = Tone.valueOf(body.get("tone").toUpperCase());
        userSettingService.updateTone(user.getId(), tone);
        return ResponseEntity.ok().build();
    }


    // 알림 변경
    @PatchMapping("/notifications")
    public ResponseEntity<Void> updateNotifications(@AuthenticationPrincipal User user,
                                                    @RequestBody Map<String, Boolean> body) {
        boolean gpt = body.getOrDefault("gptFeedbackNotify", true);
        boolean goal = body.getOrDefault("goalArrivalNotify", true);
        boolean diary = body.getOrDefault("diaryMissingNotify", true);
        userSettingService.updateNotifications(user.getId(), gpt, goal, diary);
        return ResponseEntity.ok().build();
    }

}