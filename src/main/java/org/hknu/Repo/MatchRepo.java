package org.hknu.Repo;

import org.hknu.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatchRepo extends JpaRepository<Match, Long> {
    // 중복 신청을 방지하기 위해 사용
    boolean existsByScheduleIdAndRequesterId(Long scheduleId, Long requesterId);
    // 일정의 주인(memberId)과 상태로 매칭 리스트 조회
    List<Match> findBySchedule_Member_IdAndStatus(Long memberId, Match.MatchStatus status);
    // 특정 매칭이 스케줄 주인에게 속하는지 확인하고 가져오기
    Optional<Match> findByIdAndSchedule_Member_Id(Long matchId, Long memberId);
}
