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

        // 자신의 일정에 신청하는 경우 방지
        if (schedule.getMember().getId().equals(requester.getId())) {
            throw new IllegalArgumentException("자신의 일정에는 매칭을 신청할 수 없습니다.");
        }
        // 중복 신청 방지
        if (matchRepository.existsByScheduleIdAndRequesterId(scheduleId, requester.getId())) {
            throw new IllegalArgumentException("이미 해당 일정에 매칭을 신청했습니다.");
        }

        Match newMatch = Match.builder()
                .schedule(schedule)
                .requester(requester)
                .status(Match.MatchStatus.PENDING)
                .build();

        Match savedMatch = matchRepository.save(newMatch);
        // 일정 주인에게 이메일 발송
        try {
            emailService.sendMatchRequestEmail(schedule.getMember(), requester, schedule);
        } catch (MessagingException e) {
            // 메일 발송 실패는 서비스 실패와 별도로 로깅만 수행
            System.err.println("매칭 신청 메일 발송 실패: " + e.getMessage());
        }
        return savedMatch;
    }

    // 매칭 수락
    @Transactional
    public void acceptMatch(Long matchId, String hostEmail) {
        Member host = memberRepository.findByEmail(hostEmail)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));
        Match match = matchRepository.findByIdAndSchedule_Member_Id(matchId, host.getId())
                .orElseThrow(() -> new IllegalArgumentException("권한이 없거나 존재하지 않는 매칭입니다."));
        if (match.getStatus() != Match.MatchStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 매칭입니다.");
        }
        Schedule schedule = match.getSchedule();
        // 모집 인원 초과 여부 확인
        if (schedule.getCurrentParticipants() >= schedule.getParticipants()) {
            throw new IllegalStateException("모집 인원이 가득 찼습니다.");
        }
        match.setStatus(Match.MatchStatus.ACCEPTED);
        schedule.setCurrentParticipants(schedule.getCurrentParticipants() + 1);
        // 저장
        matchRepository.save(match);
        scheduleRepository.save(schedule);
        // 신청자에게 이메일 발송
        try {
            emailService.sendMatchAcceptedEmail(match.getRequester(), host, schedule);
        } catch (MessagingException e) {
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
        } catch (MessagingException e) {
            System.err.println("매칭 거절 메일 발송 실패: " + e.getMessage());
        }
    }

}