package org.hknu.Dto;

import lombok.Data;

import java.util.Map;

@Data
public class ProfileSetupRequest {
    private String gender;
    private String name;
    private Integer age;
    private Map<String, Boolean> preferences;
    private String profileImage;
    private String bio;
    // 자기소개 등 추가 필드가 있다면 여기에 추가할 수 있습니다.

}