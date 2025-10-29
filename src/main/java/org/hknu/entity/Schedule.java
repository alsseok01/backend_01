package org.hknu.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
// ✅ [수정] @Data 삭제
import lombok.Getter; // ✅ [추가]
import lombok.NoArgsConstructor;
import lombok.Setter; // ✅ [추가]

import java.util.ArrayList;
import java.util.List;

// ✅ [수정] @Data를 @Getter와 @Setter로 변경
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String date; // "YYYY-MM-DD" 형식

    @Column(nullable = false)
    private Integer time; // 약속 시간 (예: 19)

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    private String placeName;
    private String placeCategory;

    @Column(nullable = false)
    private Integer participants = 2; // 기본 모집 인원

    @Column(nullable = false)
    @Builder.Default
    private Integer currentParticipants = 1; // 현재 참여 인원 (기본값 1)

    // ✅ Member 엔티티와 다대일(N:1) 관계 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @JsonIgnore
    @OneToMany(mappedBy = "schedule", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default // Builder 사용 시 초기화를 보장합니다.
    private List<Match> matches = new ArrayList<>();
}