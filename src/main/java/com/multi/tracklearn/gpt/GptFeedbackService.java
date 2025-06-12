package com.multi.tracklearn.gpt;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GptFeedbackService {

    private final ChatClient chatClient;

    private Resource getPromptTemplate(String tone) {
        return switch (tone.toUpperCase()) {
            case "DIRECT" -> new ClassPathResource("/prompt/diary/feedback_prompt_direct.txt");
            case "SOFT" -> new ClassPathResource("/prompt/diary/feedback_prompt_soft.txt");
            default -> throw new IllegalArgumentException("알 수 없는 tone: " + tone);
        };
    }

    private String loadTemplate(Resource resource) {
        try {
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("프롬프트 템플릿 파일 로딩 실패", e);
        }
    }

    public String getFeedback(String tone, String subject, String message) {
        Resource promptResource = getPromptTemplate(tone);
        String template = loadTemplate(promptResource);
        String systemPrompt = String.format(template, tone, subject);

        return chatClient.prompt()
                .system(systemPrompt)
                .user(message)
                .call()
                .chatResponse()
                .getResult()
                .getOutput()
                .getText();
    }


    // 프롬프트 생성 메서드
    public String generatePrompt(
            String title,
            String content,
            int studyTime,
            int satisfaction,
            List<String> goals,
            List<String> retrospectives,
            List<String> goalDetails,
            List<String> goalReasons,
            List<String> learningStyles,
            String difficulty,
            String tomorrowPlan
    )
    {
        StringBuilder goalReflectionBuilder = new StringBuilder();
        for (int i = 0; i < goals.size(); i++) {
            String goal = goals.get(i);
            String retro = (i < retrospectives.size()) ? retrospectives.get(i) : "";
            String detail = (i < goalDetails.size()) ? goalDetails.get(i) : "";
            String reason = (i < goalReasons.size()) ? goalReasons.get(i) : "";
            String style = (i < learningStyles.size()) ? learningStyles.get(i) : "";

            goalReflectionBuilder.append(String.format("""
            - 목표 %d: %s
              회고: %s
              상세 설명: %s
              설정 이유: %s
              선호 학습 방식: %s

            """, i + 1, goal, retro, detail, reason, style));
        }

        return String.format("""
                        다음은 사용자의 학습 일지입니다:
                        
                        [제목]
                        %s
                        
                        [내용]
                        %s
                        
                        [공부 시간] %d분
                        [만족도] %d / 5
                        
                        [오늘 학습 중 어려웠던 점]
                        %s
                        
                        [내일 보완하고 싶은 점]
                        %s
                        
                        [목표별 정보]
                        %s
                        
                        위 내용을 바탕으로 다음 세 가지 항목을 순차적으로 출력해주세요:
                        
                        1. 간단한 요약 (1~2줄)
                        2. 긍정적인 피드백 또는 격려
                        3. 다음 목표나 추천 학습 방향 제안
                        
                        모든 응답은 반드시 한국어로 작성하고, 각 항목은 번호를 붙여주세요.
                        """,
                title,
                content,
                studyTime,
                satisfaction,
                difficulty,
                tomorrowPlan,
                goalReflectionBuilder.toString().trim()
        );
    }
}
