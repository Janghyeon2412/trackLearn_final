package com.multi.tracklearn.controller;

import com.multi.tracklearn.domain.User;
import com.multi.tracklearn.repository.UserRepository;
import com.multi.tracklearn.service.NotificationScheduler;
import com.multi.tracklearn.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test-notify")
@RequiredArgsConstructor
public class NotificationTestController {

    private final NotificationScheduler scheduler;
    private final UserRepository userRepository;

    @PostMapping("/goal-arrival")
    public void testGoalArrivalForOne(@AuthenticationPrincipal User user) {
        scheduler.sendGoalArrivalReminderFor(user);
    }

    @PostMapping("/diary-missing")
    public void testDiaryMissingForOne(@AuthenticationPrincipal User user) {
        scheduler.sendDiaryMissingReminderFor(user);
    }


}
