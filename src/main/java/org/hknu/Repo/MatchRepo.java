package org.hknu.Repo;

import org.hknu.entity.Match;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MatchRepo extends JpaRepository<Match, Long> {
    @Query("SELECT m FROM Match m WHERE m.schedule.id = :scheduleId AND m.requester.id = :requesterId")
    Optional<Match> findBySchedule_IdAndRequester_Id(@Param("scheduleId") Long scheduleId, @Param("requesterId") Long requesterId);

    // (이하 다른 메서드들은 기존 코드 그대로 유지)
    @EntityGraph(attributePaths = {"requester", "schedule"})
    Optional<Match> findByIdAndSchedule_Member_Id(Long matchId, Long memberId);

    @EntityGraph(attributePaths = {"requester", "schedule", "schedule.member"})
    @Query("SELECT m FROM Match m WHERE m.requester.id = :requesterId")
    List<Match> findByRequester_Id(@Param("requesterId") Long requesterId);

    @EntityGraph(attributePaths = {"requester", "schedule", "schedule.member"})
    @Query("SELECT m FROM Match m WHERE m.schedule.member.id = :memberId AND m.status IN :statuses")
    List<Match> findBySchedule_Member_IdAndStatusIn(@Param("memberId") Long memberId, @Param("statuses") List<Match.MatchStatus> statuses);

    @EntityGraph(attributePaths = {"requester", "schedule", "schedule.member"})
    List<Match> findBySchedule_Member_IdAndStatus(Long memberId, Match.MatchStatus status);
}
