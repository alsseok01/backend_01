package org.hknu.Dto;

import lombok.Builder;
import lombok.Data;
import org.hknu.entity.Comment;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentResponse {
    private Long id;
    private String text;
    private AuthorDto author;
    private LocalDateTime createdAt;

    public static CommentResponse from(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .text(comment.getText())
                .author(AuthorDto.from(comment.getAuthor()))
                .createdAt(comment.getCreatedAt())
                .build();
    }
}