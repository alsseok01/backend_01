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

import java.util.Arrays;
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

    // ✅ [수정] '/api/matches' 주소를 사용하는 메서드는 이것 하나만 남겨둡니다.
    // 이 메서드가 '받은 신청' 목록을 '대기중'과 '수락됨' 상태 모두 포함하여 리스트로 반환합니다.
    @GetMapping
    public ResponseEntity<List<Match>> getReceivedMatches(@AuthenticationPrincipal UserDetails userDetails) {
        String hostEmail = userDetails.getUsername();
        Member host = memberRepository.findByEmail(hostEmail)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

        List<Match.MatchStatus> statuses = Arrays.asList(Match.MatchStatus.PENDING, Match.MatchStatus.ACCEPTED);
        List<Match> matches = matchRepo.findBySchedule_Member_IdAndStatusIn(host.getId(), statuses);

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

    // '내가 보낸 신청' 목록을 리스트로 조회합니다.
    @GetMapping("/sent")
    public ResponseEntity<List<Match>> getSentMatches(@AuthenticationPrincipal UserDetails userDetails) {
        String requesterEmail = userDetails.getUsername();
        Member requester = memberRepository.findByEmail(requesterEmail).orElse(null);
        if (requester == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<Match> matches = matchRepo.findByRequester_Id(requester.getId());
        return ResponseEntity.ok(matches);
    }
}