package org.hknu.Dto;

import lombok.Builder;
import lombok.Data;
import org.hknu.entity.Post;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
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
    private Integer likes;
    private Integer views;
    private Double latitude;
    private Double longitude;
    private String address;
    private Set<Long> likedMemberIds;

    public static PostResponse from(Post post) {

        Set<Long> memberIds = post.getPostLikes().stream()
                .map(postLike -> postLike.getMember().getId())
                .collect(Collectors.toSet());

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
                .likes(post.getLikes())
                .views(post.getViews())
                .latitude(post.getLatitude())
                .longitude(post.getLongitude())
                .address(post.getAddress())
                .likedMemberIds(memberIds)
                .build();
    }
}