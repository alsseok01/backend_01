package org.hknu.Repo;

import org.hknu.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MatchRepo extends JpaRepository<Match, Long> {
    // 중복 신청을 방지하기 위해 사용
    boolean existsByScheduleIdAndRequesterId(Long scheduleId, Long requesterId);
}
