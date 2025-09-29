package org.hknu.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder // Builder 패턴을 사용하면 객체 생성이 편리해집니다.
@AllArgsConstructor

public class LoginResponse {
    private String accessToken;
    private Long id;
    private String name;
    private String email;
    private String profileImage;
    private Integer age;
    private Map<String, Boolean> preferences;
    private String bio;
    private boolean isNewUser;
}