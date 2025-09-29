package org.hknu.service;

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
    @Autowired
    private MatchRepo matchRepository;
    @Autowired
    private MemberRepo memberRepository;
    @Autowired
    private ScheduleRepo scheduleRepository;

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

        return matchRepository.save(newMatch);
    }
}