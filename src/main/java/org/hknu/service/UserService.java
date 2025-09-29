package org.hknu.service;

import com.fasterxml.jackson.databind.ObjectMapper; // ✅ 1. ObjectMapper import
import org.hknu.Dto.ProfileSetupRequest;
import org.hknu.entity.Member;
import org.hknu.Repo.MemberRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;

@Service
public class UserService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final MemberRepo memberRepository;
    private final ObjectMapper objectMapper; // ✅ 2. ObjectMapper for JSON conversion

    @Autowired
    public UserService(MemberRepo memberRepository, ObjectMapper objectMapper) { // ✅ 3. Inject ObjectMapper
        this.memberRepository = memberRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Can't find user with this email: " + email));

        return new org.springframework.security.core.userdetails.User(
                member.getEmail(),
                member.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Transactional
    public void updateProfile(String userEmail, ProfileSetupRequest setupRequest) {

        logger.info("===== updateProfile 시작: 요청 데이터 =====");
        logger.info("요청 이메일: {}", userEmail);
        logger.info("요청 나이: {}", setupRequest.getAge());
        logger.info("요청 취향: {}", setupRequest.getPreferences());
        logger.info("==========================================");

        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("인증된 사용자 정보를 찾을 수 없습니다."));

        String preferencesJson = null;
        if (setupRequest.getPreferences() != null) {
            try {
                preferencesJson = objectMapper.writeValueAsString(setupRequest.getPreferences());
            } catch (Exception e) {
                logger.error("Preferences JSON 변환 중 오류 발생", e);
            }
        }

        // ✅ 5. Call the updated updateProfile method with all arguments
        member.updateProfile(
                setupRequest.getProfileImage(),
                setupRequest.getAge(),
                preferencesJson,
                setupRequest.getBio()
        );

        logger.info("===== DB 저장 직전 Member 객체 =====");
        logger.info("Member Email: {}", member.getEmail());
        logger.info("Member Age: {}", member.getAge());
        logger.info("Member Preferences: {}", member.getPreferences());
        logger.info("=====================================");

        memberRepository.save(member);

        logger.info("===== updateProfile 정상 종료 =====");
    }
}