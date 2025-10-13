package org.hknu.service;

import jakarta.mail.MessagingException;
import org.hknu.Repo.MatchRepo;
import org.hknu.Repo.MemberRepo;
import org.hknu.Repo.ScheduleRepo;
import org.hknu.entity.Match;
import org.hknu.entity.Member;
import org.hknu.entity.Schedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatchService {
    @Autowired private MatchRepo matchRepository;
    @Autowired private MemberRepo memberRepository;
    @Autowired private ScheduleRepo scheduleRepository;
    @Autowired private EmailService emailService;

    @Transactional
    public Match createMatch(Long scheduleId, String requesterEmail) {
        Member requester = memberRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new RuntimeException("신청자 정보를 찾을 수 없습니다."));

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("일정 정보를 찾을 수 없습니다."));

        if (schedule.getMember().getId().equals(requester.getId())) {
            throw new IllegalArgumentException("자신의 일정에는 매칭을 신청할 수 없습니다.");
        }

        // ✅ [수정] 새로 만든 명시적인 쿼리 메서드를 사용하여 중복 신청을 확인합니다.
        if (matchRepository.findBySchedule_IdAndRequester_Id(scheduleId, requester.getId()).isPresent()) {
            throw new IllegalArgumentException("이미 해당 일정에 매칭을 신청했습니다.");
        }

        Match newMatch = Match.builder()
                .schedule(schedule)
                .requester(requester)
                .status(Match.MatchStatus.PENDING)
                .build();

        Match savedMatch = matchRepository.save(newMatch);
        try {
            emailService.sendMatchRequestEmail(schedule.getMember(), requester, schedule);
        } catch (MessagingException e) {
            System.err.println("매칭 신청 메일 발송 실패: " + e.getMessage());
        }
        return savedMatch;
    }

    // 매칭 수락
    @Transactional
    public void acceptMatch(Long matchId, String hostEmail) {
        Member host = memberRepository.findByEmail(hostEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        Match match = matchRepository.findByIdAndSchedule_Member_Id(matchId, host.getId())
                .orElseThrow(() -> new IllegalArgumentException("권한이 없거나 존재하지 않는 매칭입니다."));

        if (match.getStatus() != Match.MatchStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 매칭입니다.");
        }

        Schedule schedule = match.getSchedule();
        Integer participants = schedule.getParticipants();
        if (participants == null) {
            throw new IllegalStateException("일정의 모집 인원(participants)이 설정되지 않았습니다.");
        }
        if (schedule.getCurrentParticipants() == null) {
            schedule.setCurrentParticipants(0);
        }
        if (schedule.getCurrentParticipants() >= participants) {
            throw new IllegalStateException("모집 인원이 가득 찼습니다.");
        }

        // 1. 상태 변경 및 DB 저장을 먼저 수행합니다.
        match.setStatus(Match.MatchStatus.ACCEPTED);
        schedule.setCurrentParticipants(schedule.getCurrentParticipants() + 1);
        matchRepository.save(match);
        scheduleRepository.save(schedule);

        // 2. 그 후에 이메일 발송을 시도합니다.
        try {
            // 중복 코드를 제거하고 여기서 한 번만 이메일을 보냅니다.
            emailService.sendMatchAcceptedEmail(match.getRequester(), host, schedule);
        } catch (Exception e) {
            // 이메일 발송에 실패하더라도 매칭 수락은 이미 완료되었으므로, 에러 로그만 남깁니다.
            System.err.println("매칭 수락 메일 발송 실패: " + e.getMessage());
        }
    }

    // 매칭 거절
    @Transactional
    public void rejectMatch(Long matchId, String hostEmail) {
        Member host = memberRepository.findByEmail(hostEmail)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));
        Match match = matchRepository.findByIdAndSchedule_Member_Id(matchId, host.getId())
                .orElseThrow(() -> new IllegalArgumentException("권한이 없거나 존재하지 않는 매칭입니다."));
        if (match.getStatus() != Match.MatchStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 매칭입니다.");
        }
        match.setStatus(Match.MatchStatus.REJECTED);
        matchRepository.save(match);
        // 신청자에게 거절 메일 발송
        try {
            emailService.sendMatchRejectedEmail(match.getRequester(), host, match.getSchedule());
        } catch (Exception e) {
            System.err.println("매칭 거절 메일 발송 실패: " + e.getMessage());
        }
    }

    @Transactional
    public void deleteMatch(Long matchId, String currentUserEmail) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매칭입니다."));

        String requesterEmail = match.getRequester().getEmail();
        String hostEmail = match.getSchedule().getMember().getEmail();

        // 매칭 신청자나 호스트가 아니면 삭제 권한 없음
        if (!currentUserEmail.equals(requesterEmail) && !currentUserEmail.equals(hostEmail)) {
            throw new IllegalArgumentException("매칭에 참여한 사용자만 삭제할 수 있습니다.");
        }

        matchRepository.delete(match);
    }

    @Transactional
    public void confirmMatch(Long matchId, String currentUserEmail) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매칭입니다."));

        String requesterEmail = match.getRequester().getEmail();
        String hostEmail = match.getSchedule().getMember().getEmail();

        // 매칭 당사자만 확정 가능
        if (!currentUserEmail.equals(requesterEmail) && !currentUserEmail.equals(hostEmail)) {
            throw new IllegalArgumentException("매칭에 참여한 사용자만 확정할 수 있습니다.");
        }

        // '수락됨' 상태일 때만 확정 가능
        if (match.getStatus() != Match.MatchStatus.ACCEPTED) {
            throw new IllegalStateException("수락된 매칭만 확정할 수 있습니다.");
        }
        match.setStatus(Match.MatchStatus.CONFIRMED);

        Schedule schedule = match.getSchedule();
        schedule.setCurrentParticipants(schedule.getParticipants()); // 현재 인원을 최대 인원과 동일하게 설정

        scheduleRepository.save(schedule); // 변경된 일정 저장
        matchRepository.save(match);
    }

}