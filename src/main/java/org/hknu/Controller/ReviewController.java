package org.hknu.Controller;

import org.hknu.Dto.ReviewRequest;
import org.hknu.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/match/{matchId}/code")
    public ResponseEntity<Map<String, String>> getReviewCode(@PathVariable Long matchId, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Map<String, String> codes = reviewService.generateReviewCodes(matchId, userDetails.getUsername());
            return ResponseEntity.ok(codes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyCodeAndGetOpponent(@RequestBody Map<String, String> payload, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String code = payload.get("code");
            Map<String, Object> opponentInfo = reviewService.verifyCodeAndGetOpponentInfo(code, userDetails.getUsername());
            return ResponseEntity.ok(opponentInfo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> submitReview(@RequestBody ReviewRequest reviewRequest, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            reviewService.submitReview(reviewRequest, userDetails.getUsername());
            return ResponseEntity.ok().body("후기가 성공적으로 등록되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}