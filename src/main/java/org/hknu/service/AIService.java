package org.hknu.service;

import org.hknu.Dto.FeaturedReviewResponse;
import org.hknu.Repo.ReviewRepo;
import org.hknu.entity.Review;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AIService {

    @Autowired
    private ReviewRepo reviewRepository;

    @Transactional(readOnly = true)
    public List<FeaturedReviewResponse> getFeaturedReviews() {
        // 1. 평점이 4점 이상인 모든 후기를 가져옵니다.
        List<Review> highRatedReviews = reviewRepository.findAllByRatingGreaterThanEqual(4);

        // 2. 가져온 후기 목록을 무작위로 섞습니다.
        Collections.shuffle(highRatedReviews);

        // 3. 섞인 목록에서 최대 5개만 선택하여 DTO로 변환한 후 반환합니다.
        return highRatedReviews.stream()
                .limit(5)
                .map(FeaturedReviewResponse::from)
                .collect(Collectors.toList());
    }
}