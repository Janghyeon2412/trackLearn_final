package com.multi.tracklearn.service;


import com.multi.tracklearn.domain.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendGoalArrivalEmail(User user) {
        send(user.getEmail(), "TrackLearn 오늘의 목표 알림", "오늘 도달해야 할 목표가 있어요! 도전해보세요!");
    }

    public void sendDiaryReminderEmail(User user) {
        send(user.getEmail(), "TrackLearn 일지 미작성 알림", "오늘의 일지를 아직 작성하지 않았어요!");
    }

    private void send(String to, String subject, String content) {
        System.out.println("📨 이메일 전송 시도 to = " + to);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(to);
            helper.setFrom("janggahyeon3@gmail.com");
            helper.setSubject(subject);
            helper.setText(content, false);
            mailSender.send(message);
            log.info("✅ 이메일 전송 완료 to: {}", to);
        } catch (MessagingException e) {
            log.error("❌ 이메일 전송 실패 to: {}", to, e);
        }
    }
}