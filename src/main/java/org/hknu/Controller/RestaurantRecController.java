package org.hknu.Controller;

import org.hknu.Dto.PostResponse;
import org.hknu.service.RestaurantRecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RestaurantRecController {

    @Autowired
    private RestaurantRecService restaurantRecService;

    @GetMapping
    public ResponseEntity<List<PostResponse>> getRecommendations() {
        List<PostResponse> recommendations = restaurantRecService.getRecommendations();
        return ResponseEntity.ok(recommendations);
    }
}
