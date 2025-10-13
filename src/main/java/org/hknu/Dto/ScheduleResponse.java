package org.hknu.Dto;

import lombok.Builder;
import lombok.Data;
import org.hknu.entity.Schedule;
import java.util.Map;

@Data
@Builder
public class ScheduleResponse {
    private Long id;
    private String date;
    private Integer time;
    private String text;
    private String placeName;
    private String placeCategory;
    private Integer participants;
    private Integer currentParticipants;
    private UserDto user; // ✅ 일정을 만든 사용자 정보

    @Data
    @Builder
    public static class UserDto {
        private Long id;
        private String name;
        private String profileImage;
        private Map<String, Boolean> preferences;
        private Double averageRating;
        private Integer reviewCount;
    }
}