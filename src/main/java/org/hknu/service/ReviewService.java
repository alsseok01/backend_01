package org.hknu.service;

import org.hknu.Dto.ReviewRequest;
import org.hknu.Repo.MatchRepo;
import org.hknu.Repo.MemberRepo;
import org.hknu.Repo.ReviewRepo;
import org.hknu.entity.Match;
import org.hknu.entity.Member;
import org.hknu.entity.Review;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ReviewService {

    @Autowired
    private MatchRepo matchRepository;
    @Autowired
    private MemberRepo memberRepository;
    @Autowired
    private ReviewRepo reviewRepository;

    @Transactional(readOnly = true)
    public List<Review> getReviewsForUser(String userEmail) {
        Member reviewee = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));
        // reviewee ID를 기준으로 후기를 검색합니다.
        return reviewRepository.findByRevieweeIdOrderByCreatedAtDesc(reviewee.getId());
    }


    @Transactional
    public void submitReview(ReviewRequest reviewRequest, String reviewerEmail) {
        Member reviewer = memberRepository.findByEmail(reviewerEmail)
                .orElseThrow(() -> new RuntimeException("리뷰어 정보를 찾을 수 없습니다."));

        Member reviewee = memberRepository.findById(reviewRequest.getRevieweeId())
                .orElseThrow(() -> new RuntimeException("리뷰 대상자 정보를 찾을 수 없습니다."));

        Match match = matchRepository.findById(reviewRequest.getMatchId())
                .orElseThrow(() -> new RuntimeException("매칭 정보를 찾을 수 없습니다."));

        boolean alreadyReviewed = (reviewer.getId().equals(match.getRequester().getId()) && match.isRequesterReviewed()) ||
                (reviewer.getId().equals(match.getSchedule().getMember().getId()) && match.isHostReviewed());

        if (alreadyReviewed) {
            throw new IllegalStateException("이미 이 매칭에 대한 후기를 작성했습니다.");
        }

        Review review = Review.builder()
                .match(match)
                .reviewer(reviewer)
                .reviewee(reviewee)
                .rating(reviewRequest.getRating())
                .comment(reviewRequest.getComment())
                .build();

        reviewRepository.save(review);

        int currentTotalSum = (reviewee.getTotalRatingSum() == null) ? 0 : reviewee.getTotalRatingSum();
        int currentTotalCount = (reviewee.getReviewCount() == null) ? 0 : reviewee.getReviewCount();

        if (currentTotalSum == 0 && currentTotalCount > 0 && reviewee.getAverageRating() != null && reviewee.getAverageRating() > 0) {
            currentTotalSum = (int) (reviewee.getAverageRating() * currentTotalCount);
        }

        int newTotalSum = currentTotalSum + reviewRequest.getRating();
        int newTotalCount = currentTotalCount + 1;
        double newAverage = (double) newTotalSum / newTotalCount;

        reviewee.setTotalRatingSum(newTotalSum);
        reviewee.setReviewCount(newTotalCount); // "총 받은 후기 개수" 업데이트
        reviewee.setAverageRating(newAverage); // "평균 평점" 업데이트

        memberRepository.save(reviewee);

        if (reviewer.getId().equals(match.getRequester().getId())) {
            match.setRequesterReviewed(true);
        } else if (reviewer.getId().equals(match.getSchedule().getMember().getId())) {
            match.setHostReviewed(true);
        }
        matchRepository.save(match);
    }

    @Transactional
    public void deleteReview(Long reviewId, String userEmail) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 후기를 찾을 수 없습니다."));

        // 자신이 받은(reviewee) 후기만 삭제할 수 있도록 권한 확인
        if (!review.getReviewee().getId().equals(member.getId())) {
            throw new IllegalStateException("자신이 받은 후기만 삭제할 수 있습니다.");
        }

        // 후기만 삭제합니다.
        // 평점(averageRating, totalRatingSum, reviewCount)은 절대 건드리지 않습니다.
        reviewRepository.delete(review);
    }
}