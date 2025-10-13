package org.hknu.service;

import org.hknu.Dto.CommentRequest;
import org.hknu.Dto.PostRequest;
import org.hknu.Repo.CommentRepo;
import org.hknu.Repo.MemberRepo;
import org.hknu.Repo.PostRepo;
import org.hknu.entity.Comment;
import org.hknu.entity.Member;
import org.hknu.entity.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
}