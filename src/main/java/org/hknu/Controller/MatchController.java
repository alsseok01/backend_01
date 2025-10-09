package org.hknu.Controller;

import org.hknu.Repo.MatchRepo;
import org.hknu.Repo.MemberRepo;
import org.hknu.entity.Match;
import org.hknu.entity.Member;
import org.hknu.service.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/matches")
public class MatchController {
    @Autowired private MatchService matchService;
    @Autowired private MatchRepo matchRepo;
    @Autowired private MemberRepo memberRepository;

    @PostMapping
    public ResponseEntity<?> requestMatch(@RequestBody Map<String, Long> payload, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long scheduleId = payload.get("scheduleId");
            String requesterEmail = userDetails.getUsername();
            matchService.createMatch(scheduleId, requesterEmail);
            return ResponseEntity.ok().body("매칭 신청이 성공적으로 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("매칭 신청 중 오류가 발생했습니다.");
        }
    }

    @GetMapping
    public ResponseEntity<List<Match>> getPendingMatches(@AuthenticationPrincipal UserDetails userDetails) {
        String hostEmail = userDetails.getUsername();
        Member host = memberRepository.findByEmail(hostEmail).orElse(null);
        if (host == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<Match> matches = matchRepo.findBySchedule_Member_IdAndStatus(host.getId(), Match.MatchStatus.PENDING);
        return ResponseEntity.ok(matches);
    }

    // 매칭 수락
    @PostMapping("/{matchId}/accept")
    public ResponseEntity<?> acceptMatch(@PathVariable Long matchId,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("인증 정보가 없습니다. 다시 로그인해 주세요.");
        }
        try {
            matchService.acceptMatch(matchId, userDetails.getUsername());
            return ResponseEntity.ok().body("매칭을 수락했습니다.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // 이 밖의 오류는 예상치 못한 상황이므로 500 대신 400으로 응답해도 좋습니다.
            return ResponseEntity.badRequest().body("매칭 수락 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 매칭 거절
    @PostMapping("/{matchId}/reject")
    public ResponseEntity<?> rejectMatch(@PathVariable Long matchId, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            matchService.rejectMatch(matchId, userDetails.getUsername());
            return ResponseEntity.ok().body("매칭을 거절했습니다.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("매칭 거절 중 오류가 발생했습니다.");
        }
    }

    @DeleteMapping("/{matchId}")
    public ResponseEntity<?> deleteMatch(@PathVariable Long matchId, @AuthenticationPrincipal UserDetails userDetails) {
        String requesterEmail = userDetails.getUsername();
        try {
            matchService.deleteMatch(matchId, requesterEmail);
            return ResponseEntity.ok().body("매칭 신청을 삭제했습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    // 내가 보낸 매칭 신청 목록 조회
    @GetMapping("/sent")
    public ResponseEntity<List<Match>> getSentMatches(@AuthenticationPrincipal UserDetails userDetails) {
        String requesterEmail = userDetails.getUsername();
        Member requester = memberRepository.findByEmail(requesterEmail).orElse(null);
        if (requester == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // 요청자 ID로 매칭 리스트 조회
        List<Match> matches = matchRepo.findByRequester_Id(requester.getId());
        return ResponseEntity.ok(matches);
    }

}