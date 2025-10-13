package org.hknu.Dto;

import lombok.Builder;
import lombok.Data;
import org.hknu.entity.Review;

@Data
@Builder
public class FeaturedReviewResponse {
    private String reviewerName;
    private String reviewerProfileImage;
    private Integer rating;
    private String comment;

    public static FeaturedReviewResponse from(Review review) {
        return FeaturedReviewResponse.builder()
                .reviewerName(review.getReviewer().getName())
                .reviewerProfileImage(review.getReviewer().getProfileImage())
                .rating(review.getRating())
                .comment(review.getComment())
                .build();
    }
}