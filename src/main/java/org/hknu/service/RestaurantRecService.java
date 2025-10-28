package org.hknu.service;

import org.hknu.Dto.PostResponse;
import org.hknu.Repo.PostRepo;
import org.hknu.entity.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RestaurantRecService {

    @Autowired
    private PostRepo postRepository;

    private static final double LIKE_SCORE = 1.0;
    private static final double VIEW_SCORE = 0.5;
    private static final double MIN_SCORE_THRESHOLD = 10.0;

    @Transactional(readOnly = true)
    public List<PostResponse> getRecommendations() {
        List<Post> allPosts = postRepository.findAll();

        return allPosts.stream()
                .filter(post -> {
                    double score = (post.getLikes() * LIKE_SCORE) + (post.getViews() * VIEW_SCORE);
                    return score >= MIN_SCORE_THRESHOLD && post.getLatitude() != null && post.getLongitude() != null;
                })
                .map(PostResponse::from)
                .collect(Collectors.toList());
    }
}
