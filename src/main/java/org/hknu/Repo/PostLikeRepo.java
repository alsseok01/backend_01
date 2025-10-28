package org.hknu.Repo;

import org.hknu.entity.Member;
import org.hknu.entity.Post;
import org.hknu.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepo extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByMemberAndPost(Member member, Post post);

    boolean existsByMemberAndPost(Member member, Post post);
}