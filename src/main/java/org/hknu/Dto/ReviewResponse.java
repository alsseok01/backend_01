package org.hknu.Dto;

import lombok.Builder;
import lombok.Data;
import org.hknu.entity.Review;

import java.time.LocalDateTime;

@Data
@Builder
public class ReviewResponse {
    private Long id;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    // ✅ [수정] 후기를 받은 사람(reviewee) 대신 작성한 사람(reviewer) 정보를 담도록 변경합니다.
    private ReviewerDto reviewer;

    @Data
    @Builder
    public static class ReviewerDto {
        private String name;
        private String profileImage;
    }

    // ✅ [수정] Entity를 DTO로 변환하는 로직을 reviewer 기준으로 변경합니다.
    public static ReviewResponse from(Review review) {
        ReviewerDto reviewerDto = ReviewerDto.builder()
                .name(review.getReviewer().getName())
                .profileImage(review.getReviewer().getProfileImage())
                .build();

        return ReviewResponse.builder()
                .id(review.getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .reviewer(reviewerDto)
                .build();
    }
}