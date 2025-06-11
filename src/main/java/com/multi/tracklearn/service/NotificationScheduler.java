package com.multi.tracklearn.service;


import com.multi.tracklearn.domain.GoalLog;
import com.multi.tracklearn.domain.User;
import com.multi.tracklearn.domain.Notification.NotificationType;
import com.multi.tracklearn.domain.UserSetting;
import com.multi.tracklearn.repository.DiaryRepository;
import com.multi.tracklearn.repository.GoalLogRepository;
import com.multi.tracklearn.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final GoalLogRepository goalLogRepository;
    private final DiaryRepository diaryRepository;
    private final UserSettingService userSettingService;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final UserRepository userRepository;

    @Scheduled(cron = "0 0 10 * * *") // 오전 10시: 오늘 목표 도달 알림
    public void sendGoalArrivalReminders() {
        LocalDate today = LocalDate.now();

        List<GoalLog> todayGoals = goalLogRepository.findByDate(today);
        for (GoalLog gl : todayGoals) {
            User user = gl.getUser();
            UserSetting setting = userSettingService.getSetting(user.getId());

            if (setting.getGoalArrivalNotify()) {
                notificationService.create(
                        user,
                        "오늘 도달해야 할 목표가 있어요! 도전해보세요!",
                        NotificationType.goal
                );

                if (Boolean.TRUE.equals(setting.getGoalArrivalEmailNotify())) {
                    emailService.sendGoalArrivalEmail(user);
                }
            }
        }

        log.info("✅ 목표 도달 알림 완료: {}", todayGoals.size());
    }

    @Scheduled(cron = "0 0 22 * * *")
    public void sendDiaryMissingReminders() {
        LocalDate today = LocalDate.now();

        List<User> allUsers = userRepository.findAll();

        for (User user : allUsers) {
            boolean hasDiary = diaryRepository.existsByUserAndDate(user, today);
            UserSetting setting = userSettingService.getSetting(user.getId());

            if (!hasDiary && setting.getDiaryMissingNotify()) {
                notificationService.create(
                        user,
                        "오늘의 일지를 아직 작성하지 않았어요!",
                        NotificationType.goal
                );

                if (Boolean.TRUE.equals(setting.getDiaryMissingEmailNotify())) {
                    emailService.sendDiaryReminderEmail(user);
                }
            }
        }

        log.info("✅ 일지 미작성 알림 전송 완료");
    }


    public void sendGoalArrivalReminderFor(User user) {
        LocalDate today = LocalDate.now();
        List<GoalLog> logs = goalLogRepository.findByUserAndDate(user, today);

        UserSetting setting = userSettingService.getSetting(user.getId());
        if (setting.getGoalArrivalNotify() && !logs.isEmpty()) {
            notificationService.create(
                    user,
                    "오늘 도달해야 할 목표가 있어요! 도전해보세요!",
                    NotificationType.goal
            );
            if (Boolean.TRUE.equals(setting.getGoalArrivalEmailNotify())) {
                emailService.sendGoalArrivalEmail(user);
            }
        }
    }


    public void sendDiaryMissingReminderFor(User user) {
        LocalDate today = LocalDate.now();
        boolean hasDiary = diaryRepository.existsByUserAndDate(user, today);
        UserSetting setting = userSettingService.getSetting(user.getId());

        if (!hasDiary && setting.getDiaryMissingNotify()) {
            notificationService.create(
                    user,
                    "오늘의 일지를 아직 작성하지 않았어요!",
                    NotificationType.goal
            );
            if (Boolean.TRUE.equals(setting.getDiaryMissingEmailNotify())) {
                emailService.sendDiaryReminderEmail(user);
            }
        }
    }

}
