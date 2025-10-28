package org.hknu.service;

import org.hknu.Dto.CommentRequest;
import org.hknu.Dto.PostRequest;
import org.hknu.Dto.PostResponse;
import org.hknu.Repo.CommentRepo;
import org.hknu.Repo.MemberRepo;
import org.hknu.Repo.PostLikeRepo;
import org.hknu.Repo.PostRepo;
import org.hknu.entity.Comment;
import org.hknu.entity.Member;
import org.hknu.entity.Post;
import org.hknu.entity.PostLike;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BoardService {

    @Autowired
    private PostRepo postRepository;

    @Autowired
    private MemberRepo memberRepository;

    @Autowired
    private CommentRepo commentRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PostLikeRepo postLikeRepository;

    @Transactional(readOnly = true)
    public List<Post> getAllPosts() {
        return postRepository.findAllOrderByCreatedAtDesc();
    }

    @Transactional
    public Post createPost(PostRequest postRequest, String userEmail) {
        Member author = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Post newPost = Post.builder()
                .title(postRequest.getTitle())
                .content(postRequest.getContent())
                .tags(postRequest.getTags())
                .author(author)
                .latitude(postRequest.getLatitude())
                .longitude(postRequest.getLongitude())
                .address(postRequest.getAddress())
                .build();
        return postRepository.save(newPost);
    }

    @Transactional
    public Post updatePost(Long postId, PostRequest postRequest, String userEmail) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!post.getAuthor().getEmail().equals(userEmail)) {
            throw new IllegalStateException("게시글을 수정할 권한이 없습니다.");
        }

        post.setTitle(postRequest.getTitle());
        post.setContent(postRequest.getContent());
        post.setTags(postRequest.getTags());
        post.setLatitude(postRequest.getLatitude());
        post.setLongitude(postRequest.getLongitude());
        post.setAddress(postRequest.getAddress());
        return postRepository.save(post);
    }

    @Transactional
    public void deletePost(Long postId, String userEmail) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!post.getAuthor().getEmail().equals(userEmail)) {
            throw new IllegalStateException("게시글을 삭제할 권한이 없습니다.");
        }

        postRepository.delete(post);
    }

    @Transactional
    public Comment addComment(Long postId, CommentRequest commentRequest, String userEmail) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        Member author = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Comment newComment = Comment.builder()
                .text(commentRequest.getText())
                .author(author)
                .post(post)
                .build();

        Comment savedComment = commentRepository.save(newComment);

        // 게시글 작성자에게 이메일 알림을 보냅니다.
        try {
            emailService.sendCommentNotificationEmail(post.getAuthor(), author, post);
        } catch (Exception e) {
            // 이메일 발송에 실패하더라도 댓글 작성 로직에 영향을 주지 않도록 처리합니다.
            System.err.println("댓글 알림 이메일 발송 중 오류 발생: " + e.getMessage());
        }

        return savedComment;
    }

    @Transactional
    public void increaseViewCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        post.setViews(post.getViews() + 1);
        postRepository.save(post);
    }

    @Transactional
    public PostResponse likePost(Long postId, String userEmail) { // ✅ [수정] 반환 타입을 PostResponse로 변경
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // ✅ [수정] postLikeRepository에서 먼저 조회
        Optional<PostLike> existingLike = postLikeRepository.findByMemberAndPost(member, post);

        if (existingLike.isPresent()) {
            PostLike likeToRemove = existingLike.get();

            // ✅ [수정] Post의 컬렉션에서 제거 (Cascade + orphanRemoval 활성화)
            post.getPostLikes().remove(likeToRemove);

            post.setLikes(Math.max(0, post.getLikes() - 1));
        } else {
            PostLike newLike = PostLike.builder()
                    .member(member)
                    .post(post) // ✅ [수정] 연관관계의 주인(PostLike)에 Post 설정
                    .build();

            // ✅ [수정] Post의 컬렉션에 추가 (CascadeType.ALL 활성화)
            post.getPostLikes().add(newLike);

            post.setLikes(post.getLikes() + 1);
        }

        // ✅ [수정] post를 저장하면 postLikes의 변경사항(추가/삭제)이 DB에 전파됨
        Post updatedPost = postRepository.save(post);

        // ✅ [수정] DTO로 변환하여 반환
        return PostResponse.from(updatedPost);
    }
}