package org.hknu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "matches") // DB 테이블 이름을 'matches'로 지정
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule; // 어떤 일정에 대한 매칭인지

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "requester_id", nullable = false)
    private Member requester; // 신청한 사람

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MatchStatus status = MatchStatus.PENDING; // 매칭 상태 (대기중, 수락됨, 거절됨)

    public enum MatchStatus {
        PENDING,
        ACCEPTED,
        REJECTED,
        CONFIRMED
    }
}