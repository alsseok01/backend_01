package org.hknu.Dto;

import lombok.Data;

@Data
public class ChatMessage {
    private Long senderId;
    private String senderName;
    private String content;
}
