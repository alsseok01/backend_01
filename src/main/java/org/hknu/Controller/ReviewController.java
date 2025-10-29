package org.hknu.Controller;

import org.hknu.Dto.ReviewRequest;
import org.hknu.Dto.ReviewResponse;
import org.hknu.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/my")
    public ResponseEntity<List<ReviewResponse>> getMyReviews(@AuthenticationPrincipal UserDetails userDetails) {
        List<ReviewResponse> myReviews = reviewService.getReviewsForUser(userDetails.getUsername())
                .stream()
                .map(ReviewResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(myReviews);
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

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            reviewService.deleteReview(reviewId, userDetails.getUsername());
            return ResponseEntity.ok().body("후기가 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            // 후기를 찾을 수 없는 경우
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            // 삭제 권한이 없는 경우
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}