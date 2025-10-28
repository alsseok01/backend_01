package org.hknu.Controller;

import org.hknu.Dto.*;
import org.hknu.entity.Comment;
import org.hknu.entity.Post;
import org.hknu.service.BoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/board")
public class BoardController {

    @Autowired
    private BoardService boardService;

    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        List<PostResponse> posts = boardService.getAllPosts().stream()
                .map(PostResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(posts);
    }

    @PostMapping
    public ResponseEntity<PostResponse> createPost(@RequestBody PostRequest postRequest, @AuthenticationPrincipal UserDetails userDetails) {
        Post newPost = boardService.createPost(postRequest, userDetails.getUsername());
        return ResponseEntity.ok(PostResponse.from(newPost));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostResponse> updatePost(@PathVariable Long postId, @RequestBody PostRequest postRequest, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Post updatedPost = boardService.updatePost(postId, postRequest, userDetails.getUsername());
            return ResponseEntity.ok(PostResponse.from(updatedPost));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).build();
        }
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            boardService.deletePost(postId, userDetails.getUsername());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).build();
        }
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentResponse> addComment(@PathVariable Long postId, @RequestBody CommentRequest commentRequest, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Comment newComment = boardService.addComment(postId, commentRequest, userDetails.getUsername());
            return ResponseEntity.ok(CommentResponse.from(newComment));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{postId}/view")
    public ResponseEntity<Void> increaseViewCount(@PathVariable Long postId) {
        try {
            boardService.increaseViewCount(postId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<PostResponse> likePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // ✅ [수정] 서비스가 DTO를 직접 반환
            PostResponse updatedPostResponse = boardService.likePost(postId, userDetails.getUsername());
            // ✅ [수정] DTO 변환 로직(PostResponse.from) 제거
            return ResponseEntity.ok(updatedPostResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}