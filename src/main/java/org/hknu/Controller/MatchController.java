package org.hknu.Controller;

import org.hknu.entity.Match;
import org.hknu.service.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/matches")
public class MatchController {
    @Autowired
    private MatchService matchService;

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
}