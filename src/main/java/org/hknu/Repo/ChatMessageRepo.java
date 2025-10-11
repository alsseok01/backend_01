package org.hknu.Repo;

import org.hknu.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepo extends JpaRepository<ChatMessageEntity, Long> {
    // 채팅방 ID(matchId)를 기준으로 모든 메시지를 시간순으로 정렬하여 찾기
    List<ChatMessageEntity> findByMatchIdOrderByTimestampAsc(Long matchId);
}