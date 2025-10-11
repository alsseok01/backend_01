package org.hknu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "chat_messages")
public class ChatMessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long matchId; // 채팅방 ID
    private Long senderId;
    private String senderName;
    @Column(columnDefinition = "TEXT")
    private String content;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}