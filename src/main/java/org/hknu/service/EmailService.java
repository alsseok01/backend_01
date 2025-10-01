package org.hknu.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    public void sendVerificationEmail(String to, String token) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

        // 실제 애플리케이션에서는 설정 파일에서 URL을 관리하는 것이 좋습니다.
        String verificationUrl = "http://localhost:8080/api/auth/verify-email?token=" + token;

        Context context = new Context();
        context.setVariable("verificationUrl", verificationUrl);

        String html = templateEngine.process("verification-email", context);

        helper.setTo(to);
        helper.setSubject("이메일 주소를 인증해주세요.");
        helper.setText(html, true);

        javaMailSender.send(mimeMessage);
    }
}