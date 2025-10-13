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
        updateUserRating(reviewee.getId());

        if (reviewer.getId().equals(match.getRequester().getId())) {
            match.setRequesterReviewed(true);
        } else if (reviewer.getId().equals(match.getSchedule().getMember().getId())) {
            match.setHostReviewed(true);
        }
        matchRepository.save(match);
    }

    private void updateUserRating(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("평점을 업데이트할 사용자를 찾을 수 없습니다."));

        List<Review> reviews = reviewRepository.findByRevieweeId(memberId);
        if (reviews.isEmpty()) {
            member.setAverageRating(0.0);
            member.setReviewCount(0);
        } else {
            double average = reviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            member.setAverageRating(average);
            member.setReviewCount(reviews.size());
        }
        memberRepository.save(member);
    }
}