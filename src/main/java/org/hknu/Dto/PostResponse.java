package org.hknu.Dto;

import lombok.Builder;
import lombok.Data;
import org.hknu.entity.Post;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private AuthorDto author;
    private List<String> tags;
    private LocalDateTime createdAt;
    private List<CommentResponse> comments;

    public static PostResponse from(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .author(AuthorDto.from(post.getAuthor()))
                .tags(post.getTags())
                .createdAt(post.getCreatedAt())
                .comments(post.getComments().stream()
                        .map(CommentResponse::from)
                        .collect(Collectors.toList()))
                .build();
    }
}