package org.hknu.Controller;

import org.hknu.Dto.ChatMessage;
import org.hknu.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController // @Controller 대신 @RestController를 사용
public class ChatController {

    @Autowired
    private ChatService chatService;

    @MessageMapping("/chat/{matchId}")
    @SendTo("/topic/chat/{matchId}")
    public ChatMessage sendMessage(@DestinationVariable("matchId") Long matchId, ChatMessage chatMessage) {
        // ✅ [수정] 메시지를 DB에 저장한 후 모든 구독자에게 전송
        return chatService.saveMessage(matchId, chatMessage);
    }

    // ✅ [추가] 채팅 내역을 가져오는 API 엔드포인트
    @GetMapping("/api/chat/{matchId}/history")
    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable Long matchId) {
        return ResponseEntity.ok(chatService.getMessageHistory(matchId));
    }
}