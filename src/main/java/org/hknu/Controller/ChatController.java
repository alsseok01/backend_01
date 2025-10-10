package org.hknu.Controller;

import org.hknu.Dto.ChatMessage;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    @MessageMapping("/chat/{matchId}")
    @SendTo("/topic/chat/{matchId}")
    public ChatMessage sendMessage(@DestinationVariable String matchId, ChatMessage chatMessage) {
        return chatMessage;
    }
}
