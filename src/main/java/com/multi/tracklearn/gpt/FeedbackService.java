package com.multi.tracklearn.gpt;

import com.multi.tracklearn.domain.*;
import com.multi.tracklearn.repository.FeedbackRepository;
import com.multi.tracklearn.repository.GoalLogRepository;
import com.multi.tracklearn.repository.NotificationRepository;
import com.multi.tracklearn.service.NotificationService;
import com.multi.tracklearn.service.UserSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final GptFeedbackService gptFeedbackService;
    private final FeedbackRepository feedbackRepository;
    private final GoalLogRepository goalLogRepository; // 필요!
    private final NotificationService notificationService;
    private final UserSettingService userSettingService;


    @Transactional
    public void generateFeedback(Diary diary) {
        List<String> goals = diary.getGoalLogIds().stream()
                .map(goalLogRepository::findById)
                .filter(Optional::isPresent)
                .map(opt -> opt.get().getGoal().getTitle())
                .toList();

        List<String> retrospectives = diary.getRetrospectives();

        List<String> goalDetails = diary.getGoalLogIds().stream()
                .map(goalLogRepository::findById)
                .filter(Optional::isPresent)
                .map(opt -> opt.get().getGoal().getGoalDetail())
                .toList();

        List<String> goalReasons = diary.getGoalLogIds().stream()
                .map(goalLogRepository::findById)
                .filter(Optional::isPresent)
                .map(opt -> opt.get().getGoal().getGoalReason())
                .toList();

        List<String> learningStyles = diary.getGoalLogIds().stream()
                .map(goalLogRepository::findById)
                .filter(Optional::isPresent)
                .map(opt -> opt.get().getGoal().getLearningStyle())
                .toList();

        // ✅ 사용자 톤 설정 반영
        User user = diary.getUser();
        UserSetting setting = userSettingService.getSetting(user.getId());
        Tone tone = setting.getTone();

        String prompt = gptFeedbackService.generatePrompt(
                diary.getTitle(),
                diary.getContent(),
                diary.getStudyTime(),
                (int) diary.getSatisfaction(),
                goals,
                retrospectives,
                goalDetails,
                goalReasons,
                learningStyles,
                diary.getDifficulty(),
                diary.getTomorrowPlan()
        );

        // ✅ 톤 기반 GPT 호출
        String response = gptFeedbackService.getFeedback(tone.name(), "학습 피드백", prompt);

        List<String> sections = Arrays.stream(response.split("\\n\\n"))
                .map(String::trim)
                .toList();

        for (int i = 0; i < sections.size(); i++) {
            Feedback feedback = new Feedback();
            feedback.setDiary(diary);
            feedback.setToneType(tone.toFeedbackToneType()); // 💡 enum 변환 메서드 만들면 좋음
            feedback.setCreatedPerson("GPT");
            feedback.setModifiedPerson("GPT");

            switch (i) {
                case 0 -> feedback.setResponseType(Feedback.ResponseType.cheer);
                case 1 -> feedback.setResponseType(Feedback.ResponseType.advice);
                case 2 -> feedback.setResponseType(Feedback.ResponseType.adjust);
                default -> feedback.setResponseType(Feedback.ResponseType.advice);
            }

            feedback.setContent(sections.get(i));
            feedbackRepository.save(feedback);
        }

        if (setting.getGptFeedbackNotify()) {
            notificationService.create(
                    user,
                    "GPT 피드백이 도착했습니다. 일지를 확인해보세요.",
                    Notification.NotificationType.feedback
            );
        }

        System.out.println("✅ diary.getUser() = " + user);
        System.out.println("✅ GPT 알림 설정 상태 = " + setting.getGptFeedbackNotify());
    }

}