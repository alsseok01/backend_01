package org.hknu.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.hknu.entity.Member;
import org.hknu.entity.Post;
import org.hknu.entity.Schedule;
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

    public void sendMatchRequestEmail(Member host, Member requester, Schedule schedule) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
        helper.setTo(host.getEmail());
        helper.setSubject("[밥상친구] 매칭 신청이 도착했습니다");
        String text = String.format(
                "안녕하세요, %s님!\n\n" +
                        "사용자 %s(%s)가 %s %s시 ‘%s’ 일정에 매칭을 신청했습니다.\n" +
                        "웹사이트에 로그인하여 신청을 수락하거나 거절해 주세요.\n\n" +
                "→ 매칭 요청 확인하기: http://localhost:3000/match-requests",
                host.getName(), requester.getName(), requester.getEmail(),
                schedule.getDate(), schedule.getTime(), schedule.getPlaceName()
        );
        helper.setText(text);
        javaMailSender.send(message);
    }

    public void sendMatchAcceptedEmail(Member requester, Member host, Schedule schedule) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
        helper.setTo(requester.getEmail());
        helper.setSubject("[밥상친구] 매칭이 수락되었습니다");
        String text = String.format(
                "안녕하세요, %s님!\n\n" +
                        "%s님이 %s %s시 ‘%s’ 일정에 대한 매칭을 수락했습니다. 즐거운 만남 가지세요!\n\n" +
                        "→ 내가 보낸 신청 확인하기: http://localhost:3000/matches/sent",
                requester.getName(), host.getName(), schedule.getDate(), schedule.getTime(), schedule.getPlaceName()
        );
        helper.setText(text);
        javaMailSender.send(message);
    }

    public void sendMatchRejectedEmail(Member requester, Member host, Schedule schedule) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
        helper.setTo(requester.getEmail());
        helper.setSubject("[밥상친구] 매칭이 거절되었습니다");
        String text = String.format(
                "안녕하세요, %s님.\n\n" +
                        "안타깝게도 %s님이 %s %s시 ‘%s’ 일정에 대한 매칭을 거절했습니다. 다음 기회를 노려보세요.",
                requester.getName(), host.getName(), schedule.getDate(), schedule.getTime(), schedule.getPlaceName()
        );
        helper.setText(text);
        javaMailSender.send(message);
    }
    public void sendCommentNotificationEmail(Member postAuthor, Member commentAuthor,Post post) {
        // 자신의 글에 단 댓글은 알림을 보내지 않음
        if (postAuthor.getEmail().equals(commentAuthor.getEmail())) {
            return;
        }

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setTo(postAuthor.getEmail());
            helper.setSubject("[밥상친구] 회원님의 게시글에 새 댓글이 달렸습니다!");

            String text = String.format(
                    "안녕하세요, %s님!\n\n" +
                            "회원님의 게시글 '%s'에 %s님이 새 댓글을 남겼습니다.\n\n" +
                            "지금 바로 확인해보세요!\n" +
                            "→ 게시판으로 이동하기: http://localhost:3000/board",
                    postAuthor.getName(),
                    post.getTitle(),
                    commentAuthor.getName()
            );

            helper.setText(text);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("댓글 알림 메일 발송 실패: " + e.getMessage());
        }
    }

}