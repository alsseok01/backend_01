package org.hknu.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hknu.Config.JwtTokenProvider;
import org.hknu.Dto.LoginRequest;
import org.hknu.Dto.LoginResponse;
import org.hknu.Dto.SignUpRequest;
import org.hknu.Repo.MemberRepo;
import org.hknu.entity.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);


    @Autowired
    private MemberRepo userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    public LoginResponse  register(SignUpRequest signUpRequest) {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }
        Member user = new Member();
        user.setName(signUpRequest.getName());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setProfileImage("https://mblogthumb-phinf.pstatic.net/MjAyMDA2MTBfMTY1/MDAxNTkxNzQ2ODcyOTI2.Yw5WjjU3IuItPtqbegrIBJr3TSDMd_OPhQ2Nw-0-0ksg.8WgVjtB0fy0RCv0XhhUOOWt90Kz_394Zzb6xPjG6I8gg.PNG.lamute/user.png?type=w800");
        user.setProvider(Member.AuthProvider.LOCAL);

        Member registeredUser = userRepository.save(user);
        String token = tokenProvider.generateToken(registeredUser.getEmail());

        return LoginResponse.builder()
                .accessToken(token)
                .id(registeredUser.getId())
                .name(registeredUser.getName())
                .email(registeredUser.getEmail())
                .profileImage(registeredUser.getProfileImage())
                .isNewUser(true)
                .build();
    }

    public LoginResponse login(LoginRequest loginRequest) {
        Member member = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        logger.info("===== login 시작: DB에서 불러온 Member 객체 =====");
        logger.info("Member Email: {}", member.getEmail());
        logger.info("Member Age: {}", member.getAge());
        logger.info("Member Preferences: {}", member.getPreferences());
        logger.info("===============================================");

        if (!passwordEncoder.matches(loginRequest.getPassword(), member.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        String token = tokenProvider.generateToken(member.getEmail());
        Map<String, Boolean> preferencesMap = null;
        String preferencesJson = member.getPreferences();
        if (preferencesJson != null && !preferencesJson.isEmpty()) {
            try {
                preferencesMap = objectMapper.readValue(preferencesJson, new TypeReference<Map<String, Boolean>>() {});
            } catch (Exception e) {
                e.printStackTrace(); // 실제 서비스에서는 로깅 등으로 처리
            }
        }
        boolean isNewUser = (member.getAge() == null);
        // ✅ [수정] LoginResponse에 age와 preferences를 추가하여 반환합니다.
        return LoginResponse.builder()
                .accessToken(token)
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .profileImage(member.getProfileImage())
                .age(member.getAge()) // age 추가
                .preferences(preferencesMap) // preferences 추가
                .bio(member.getBio())
                .isNewUser(isNewUser)
                .build();

    }

}
