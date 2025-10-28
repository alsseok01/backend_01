package org.hknu.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "post_likes",
        uniqueConstraints = {
                // 한 명의 유저가 하나의 게시글에 한 번만 좋아요를 누를 수 있도록
                // member_id와 post_id 조합을 유니크(unique)하게 설정합니다.
                @UniqueConstraint(
                        name = "post_like_uk",
                        columnNames = {"member_id", "post_id"}
                )
        }
)
public class PostLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;
}