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

    @EntityGraph(attributePaths = {"requester", "schedule", "schedule.member"})
    Optional<Match> findByIdAndSchedule_Member_Id(Long matchId, Long memberId);

    // ✅ [확인] "내가 보낸 신청" 쿼리
    @Query("SELECT m FROM Match m " +
            "JOIN FETCH m.requester r " +
            "JOIN FETCH m.schedule s " +
            "JOIN FETCH s.member sm " +
            "WHERE r.id = :requesterId")
    List<Match> findByRequester_Id(@Param("requesterId") Long requesterId);

    // ✅ [확인] "받은 신청" 쿼리
    @Query("SELECT m FROM Match m " +
            "JOIN FETCH m.requester r " +
            "JOIN FETCH m.schedule s " +
            "JOIN FETCH s.member sm " +
            "WHERE sm.id = :memberId AND m.status IN :statuses")
    List<Match> findBySchedule_Member_IdAndStatusIn(@Param("memberId") Long memberId, @Param("statuses") List<Match.MatchStatus> statuses);

    @EntityGraph(attributePaths = {"requester", "schedule", "schedule.member"})
    List<Match> findBySchedule_Member_IdAndStatus(Long memberId, Match.MatchStatus status);
}