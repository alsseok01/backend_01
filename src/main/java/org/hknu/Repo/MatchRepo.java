package org.hknu.Repo;

import org.hknu.entity.Match;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatchRepo extends JpaRepository<Match, Long> {
    // 중복 신청을 방지하기 위해 사용
    boolean existsByScheduleIdAndRequesterId(Long scheduleId, Long requesterId);
    // 일정의 주인(memberId)과 상태로 매칭 리스트 조회
    @EntityGraph(attributePaths = {"requester", "schedule", "schedule.member"})
    List<Match> findBySchedule_Member_IdAndStatus(Long memberId, Match.MatchStatus status);

    @EntityGraph(attributePaths = {"requester", "schedule"})
    Optional<Match> findByIdAndSchedule_Member_Id(Long matchId, Long memberId);

    @EntityGraph(attributePaths = {"requester", "schedule", "schedule.member"})
    List<Match> findByRequester_Id(Long requesterId);
}
