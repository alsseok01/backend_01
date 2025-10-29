package org.hknu.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // DB가 ID를 자동으로 생성하고 관리합니다.
    private Long id; // ✅ [수정] 기본 키(Primary Key)

    @Column(nullable = false, unique = true) // 이메일은 고유해야 하며 비어있을 수 없습니다.
    private String email;

    @Column(nullable = true)
    private String password;

    @Column(nullable = false)
    private String name;

    private String profileImage;

    private Integer age;

    @Column(columnDefinition = "TEXT")
    private String preferences;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String verificationToken;

    private boolean emailVerified = false;

    @Builder.Default
    private Double averageRating = 0.0;

    @Builder.Default
    private Integer reviewCount = 0;

    @Builder.Default
    private Integer totalRatingSum = 0;

    @Column(columnDefinition = "TEXT")
    private String fcmToken;

    public enum AuthProvider {
        LOCAL, GOOGLE, KAKAO
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<PostLike> postLikes = new HashSet<>();

    public void updateProfile(String profileImage, Integer age, String preferences, String bio) {
        if (profileImage != null) this.profileImage = profileImage;
        if (age != null) this.age = age;
        if (preferences != null) this.preferences = preferences;
        if (bio != null) this.bio = bio; // bio 업데이트 추가
    }
}