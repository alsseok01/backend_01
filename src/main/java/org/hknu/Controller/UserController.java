package org.hknu.Controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hknu.Dto.LoginResponse;
import org.hknu.Dto.ProfileSetupRequest;
import org.hknu.Repo.MemberRepo;
import org.hknu.entity.Member;
import org.hknu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // ✅ [추가] import
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private MemberRepo memberRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @PutMapping("/profile")
    public ResponseEntity<LoginResponse> updateUserProfile(@RequestBody ProfileSetupRequest setupRequest, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String userEmail = userDetails.getUsername();
            userService.updateProfile(userEmail, setupRequest);

            // 중요: 업데이트된 최신 사용자 정보를 다시 조회합니다.
            Member updatedMember = memberRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

            // isNewUser가 false로 바뀐 LoginResponse를 생성하여 반환합니다.
            Map<String, Boolean> preferencesMap = null;
            if (updatedMember.getPreferences() != null && !updatedMember.getPreferences().isEmpty()) {
                preferencesMap = objectMapper.readValue(updatedMember.getPreferences(), new TypeReference<>() {});
            }

            LoginResponse response = LoginResponse.builder()
                    .id(updatedMember.getId())
                    .name(updatedMember.getName())
                    .email(updatedMember.getEmail())
                    .profileImage(updatedMember.getProfileImage())
                    .age(updatedMember.getAge())
                    .preferences(preferencesMap)
                    .bio(updatedMember.getBio())
                    .isNewUser(false)
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // badRequest()는 보통 String을 반환하므로, 여기서는 그대로 둡니다.
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<LoginResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

        Map<String, Boolean> preferencesMap = null;
        String preferencesJson = member.getPreferences();
        if (preferencesJson != null && !preferencesJson.isEmpty()) {
            try {
                preferencesMap = objectMapper.readValue(preferencesJson, new TypeReference<Map<String, Boolean>>() {});
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(null);
            }
        }

        boolean isNewUser = (member.getAge() == null);


        LoginResponse userProfile = LoginResponse.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .profileImage(member.getProfileImage())
                .age(member.getAge())
                .preferences(preferencesMap)
                .bio(member.getBio())
                .isNewUser(isNewUser)
                .build();

        return ResponseEntity.ok(userProfile);
    }
}