package org.hknu.Controller;

import org.hknu.Dto.FCMTokenRequest;
import org.hknu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fcm")
public class FCMController {

    @Autowired
    private UserService userService;

    @PostMapping("/token")
    public ResponseEntity<?> saveFCMToken(@RequestBody FCMTokenRequest tokenRequest, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            userService.saveFCMToken(userDetails.getUsername(), tokenRequest.getToken());
            return ResponseEntity.ok().body("토큰이 성공적으로 저장되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("토큰 저장에 실패했습니다: " + e.getMessage());
        }
    }
}