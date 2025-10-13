package org.hknu.Dto;

import lombok.Builder;
import lombok.Data;
import org.hknu.entity.Member;

@Data
@Builder
public class AuthorDto {
    private Long id;
    private String name;

    public static AuthorDto from(Member member) {
        return AuthorDto.builder()
                .id(member.getId())
                .name(member.getName())
                .build();
    }
}