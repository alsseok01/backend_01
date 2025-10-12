package org.hknu.Dto;

import lombok.Data;

@Data
public class ReviewRequest {
    private Long matchId;
    private Long revieweeId; // 리뷰 대상자 ID
    private Integer rating;
    private String comment;
}