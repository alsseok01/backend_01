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

    // In-memory storage for codes. In a real application, use Redis or a database.
    private static final Map<String, Long> qrCodeToMatchId = new ConcurrentHashMap<>();
    private static final Map<String, Long> numericCodeToMatchId = new ConcurrentHashMap<>();
    private static final Map<Long, String> matchIdToNumericCode = new ConcurrentHashMap<>();


    @Transactional
    public Map<String, String> generateReviewCodes(Long matchId, String userEmail) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("매칭 정보를 찾을 수 없습니다."));

        if (!match.getRequester().getEmail().equals(userEmail) && !match.getSchedule().getMember().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("매칭 참여자만 코드를 생성할 수 있습니다.");
        }
        if (match.getStatus() != Match.MatchStatus.CONFIRMED) {
            throw new IllegalArgumentException("확정된 매칭에 대해서만 후기를 작성할 수 있습니다.");
        }

        String qrCode = UUID.randomUUID().toString();
        String numericCode = matchIdToNumericCode.computeIfAbsent(matchId, k -> String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000)));

        qrCodeToMatchId.put(qrCode, matchId);
        numericCodeToMatchId.put(numericCode, matchId);

        Map<String, String> codes = new HashMap<>();
        codes.put("qrCode", qrCode);
        codes.put("numericCode", numericCode);
        return codes;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> verifyCodeAndGetOpponentInfo(String code, String currentUserEmail) {
        Long matchId = qrCodeToMatchId.getOrDefault(code, numericCodeToMatchId.get(code));

        if (matchId == null) {
            throw new IllegalArgumentException("유효하지 않은 코드입니다.");
        }

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("매칭 정보를 찾을 수 없습니다."));

        Member currentUser = memberRepository.findByEmail(currentUserEmail).orElseThrow();
        Member opponent;

        if (match.getRequester().getId().equals(currentUser.getId())) {
            opponent = match.getSchedule().getMember();
        } else if (match.getSchedule().getMember().getId().equals(currentUser.getId())) {
            opponent = match.getRequester();
        } else {
            throw new IllegalArgumentException("매칭 참여자가 아닙니다.");
        }

        Map<String, Object> opponentInfo = new HashMap<>();
        opponentInfo.put("matchId", matchId);
        opponentInfo.put("opponentId", opponent.getId());
        opponentInfo.put("opponentName", opponent.getName());
        opponentInfo.put("opponentProfileImage", opponent.getProfileImage());

        return opponentInfo;
    }

    @Transactional
    public void submitReview(ReviewRequest reviewRequest, String reviewerEmail) {
        Member reviewer = memberRepository.findByEmail(reviewerEmail)
                .orElseThrow(() -> new RuntimeException("리뷰어 정보를 찾을 수 없습니다."));

        Member reviewee = memberRepository.findById(reviewRequest.getRevieweeId())
                .orElseThrow(() -> new RuntimeException("리뷰 대상자 정보를 찾을 수 없습니다."));

        Match match = matchRepository.findById(reviewRequest.getMatchId())
                .orElseThrow(() -> new RuntimeException("매칭 정보를 찾을 수 없습니다."));

        Review review = Review.builder()
                .match(match)
                .reviewer(reviewer)
                .reviewee(reviewee)
                .rating(reviewRequest.getRating())
                .comment(reviewRequest.getComment())
                .build();

        reviewRepository.save(review);
    }
}