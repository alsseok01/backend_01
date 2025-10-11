package org.hknu.service;

import org.hknu.Dto.ChatMessage;
import org.hknu.Repo.ChatMessageRepo;
import org.hknu.entity.ChatMessageEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired
    private ChatMessageRepo chatMessageRepository;

    public ChatMessage saveMessage(Long matchId, ChatMessage chatMessage) {
        ChatMessageEntity entity = ChatMessageEntity.builder()
                .matchId(matchId)
                .senderId(chatMessage.getSenderId())
                .senderName(chatMessage.getSenderName())
                .content(chatMessage.getContent())
                .timestamp(LocalDateTime.now())
                .build();
        chatMessageRepository.save(entity);
        return chatMessage; // 저장 후, 받은 메시지를 그대로 반환하여 broadcast
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