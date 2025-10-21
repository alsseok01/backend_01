package org.hknu.service;

import com.google.firebase.messaging.FirebaseMessagingException;
import org.hknu.Dto.ChatMessage;
import org.hknu.Repo.ChatMessageRepo;
import org.hknu.Repo.MatchRepo;
import org.hknu.Repo.MemberRepo;
import org.hknu.entity.ChatMessageEntity;
import org.hknu.entity.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired
    private ChatMessageRepo chatMessageRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MatchRepo matchRepository;

    @Autowired
    private MemberRepo memberRepository;

    @Autowired
    private FCMService fcmService;

    public ChatMessage saveMessage(Long matchId, ChatMessage chatMessage) {
        ChatMessageEntity entity = ChatMessageEntity.builder()
                .matchId(matchId)
                .senderId(chatMessage.getSenderId())
                .senderName(chatMessage.getSenderName())
                .content(chatMessage.getContent())
                .timestamp(LocalDateTime.now())
                .build();
        chatMessageRepository.save(entity);

        matchRepository.findById(matchId).ifPresent(match -> {
            Member requester = match.getRequester();
            Member host = match.getSchedule().getMember();
            Long senderId = chatMessage.getSenderId();

            // 메시지를 보낸 사람이 아닌, 받는 사람의 ID를 찾습니다.
            Long recipientId = null;
            if (senderId.equals(requester.getId())) {
                recipientId = host.getId();
            } else if (senderId.equals(host.getId())) {
                recipientId = requester.getId();
            }

            // 받는 사람의 개인 알림 채널로 "새 메시지 왔어!" 라는 간단한 신호를 보냅니다.
            if (recipientId != null) {
                messagingTemplate.convertAndSend("/topic/user/" + recipientId + "/notifications", "new_message");

                memberRepository.findById(recipientId).ifPresent(recipient -> {
                    if (recipient.getFcmToken() != null && !recipient.getFcmToken().isEmpty()) {
                        try {
                            fcmService.sendNotification(
                                    recipient.getFcmToken(),
                                    chatMessage.getSenderName(), // 알림 제목
                                    chatMessage.getContent()     // 알림 내용
                            );
                        } catch (FirebaseMessagingException e) {
                            System.err.println("FCM 알림 전송 실패: " + e.getMessage());
                        }
                    }
                });
            }
        });

        return chatMessage;
    }

    public List<ChatMessage> getMessageHistory(Long matchId) {
        return chatMessageRepository.findByMatchIdOrderByTimestampAsc(matchId)
                .stream()
                .map(entity -> {
                    ChatMessage dto = new ChatMessage();
                    dto.setSenderId(entity.getSenderId());
                    dto.setSenderName(entity.getSenderName());
                    dto.setContent(entity.getContent());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}