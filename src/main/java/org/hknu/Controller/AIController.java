package org.hknu.Controller;

import org.hknu.Dto.FeaturedReviewResponse;
import org.hknu.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Autowired
    private AIService aiService;

    @GetMapping("/featured-reviews")
    public ResponseEntity<List<FeaturedReviewResponse>> getFeaturedReviews() {
        List<FeaturedReviewResponse> reviews = aiService.getFeaturedReviews();
        return ResponseEntity.ok(reviews);
    }
}