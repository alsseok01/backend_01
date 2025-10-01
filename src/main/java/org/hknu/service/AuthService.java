package org.hknu.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import org.hknu.Config.JwtTokenProvider;
import org.hknu.Dto.LoginRequest;
import org.hknu.Dto.LoginResponse;
import org.hknu.Dto.SignUpRequest;
import org.hknu.Repo.MemberRepo;
import org.hknu.entity.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

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

    @Autowired
    private EmailService emailService;

    @Autowired
    private JavaMailSender emailSender;

    @ResponseStatus(HttpStatus.CONFLICT)
    public class EmailAlreadyUsedException extends RuntimeException {
        public EmailAlreadyUsedException(String message) { super(message); }
    }

    public LoginResponse  register(SignUpRequest signUpRequest) {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new EmailAlreadyUsedException("이미 사용 중인 이메일입니다.");
        }
        Member user = new Member();
        user.setName(signUpRequest.getName());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setProfileImage("https://mblogthumb-phinf.pstatic.net/MjAyMDA2MTBfMTY1/MDAxNTkxNzQ2ODcyOTI2.Yw5WjjU3IuItPtqbegrIBJr3TSDMd_OPhQ2Nw-0-0ksg.8WgVjtB0fy0RCv0XhhUOOWt90Kz_394Zzb6xPjG6I8gg.PNG.lamute/user.png?type=w800");
        user.setProvider(Member.AuthProvider.LOCAL);
        user.setEmailVerified(false);

        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);

        Member registeredUser = userRepository.save(user);

        try {
            emailService.sendVerificationEmail(registeredUser.getEmail(), token);
        } catch (MessagingException e) {
            logger.error("인증 이메일 발송 실패: {}", registeredUser.getEmail(), e);
            // 이메일 발송 실패 시 처리 로직 (예: 트랜잭션 롤백, 사용자에게 알림 등)
        }

        // 로그인과 동일하게 토큰을 생성하고 필요한 정보를 함께 반환합니다.
        String accessToken = tokenProvider.generateToken(registeredUser.getEmail());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .id(registeredUser.getId())
                .name(registeredUser.getName())
                .email(registeredUser.getEmail())
                .profileImage(registeredUser.getProfileImage())
                .isNewUser(true)
                .build();
    }

    public void verifyEmail(String token) {
        Member user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("유효하지 않은 인증 토큰입니다."));

        user.setEmailVerified(true);
        user.setVerificationToken(null); // 인증 후 토큰은 초기화
        userRepository.save(user);
    }

    public void sendVerificationEmail(String email) {
        // 6자리 인증 코드 생성
        Random random = new Random();
        String verificationCode = String.valueOf(100000 + random.nextInt(900000));

        // TODO: 생성된 인증 코드를 Redis나 DB에 잠시 저장하여 나중에 검증할 수 있도록 해야 합니다.
        // 예: redisTemplate.opsForValue().set(email, verificationCode, 5, TimeUnit.MINUTES);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[Launch Group Match] 회원가입 이메일 인증 코드");
        message.setText("인증 코드: " + verificationCode);
        emailSender.send(message);
    }

    public LoginResponse login(LoginRequest loginRequest) {
        Member member = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!member.isEmailVerified()) {
            throw new RuntimeException("로그인하기 전에 이메일 인증을 완료해주세요.");
        }

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
