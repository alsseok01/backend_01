package org.hknu.Dto;

import lombok.Builder;
import lombok.Data;
import org.hknu.entity.Match;
import org.hknu.entity.Member;
import org.hknu.entity.Schedule;

@Data
@Builder
public class MatchResponse {

    private Long id;
    private String status;
    private ScheduleDto schedule;
    private MemberDto requester;
    private boolean requesterReviewed;
    private boolean hostReviewed;

    // 중첩 DTO: Member 정보
    @Data
    @Builder
    public static class MemberDto {
        private Long id;
        private String name;
        private String profileImage;

        public static MemberDto from(Member member) {
            if (member == null) return null;
            return MemberDto.builder()
                    .id(member.getId())
                    .name(member.getName())
                    .profileImage(member.getProfileImage())
                    .build();
        }
    }

    // 중첩 DTO: Schedule 정보
    @Data
    @Builder
    public static class ScheduleDto {
        private Long id;
        private String placeName;
        private MemberDto member; // 일정 소유자 (Host)

        public static ScheduleDto from(Schedule schedule) {
            if (schedule == null) return null;
            return ScheduleDto.builder()
                    .id(schedule.getId())
                    .placeName(schedule.getPlaceName())
                    .member(MemberDto.from(schedule.getMember())) // MemberDto로 변환
                    .build();
        }
    }

    // 엔티티를 DTO로 변환하는 메인 메서드
    public static MatchResponse from(Match match) {
        return MatchResponse.builder()
                .id(match.getId())
                .status(match.getStatus().name()) // Enum을 String으로 변환
                .schedule(ScheduleDto.from(match.getSchedule())) // ScheduleDto로 변환
                .requester(MemberDto.from(match.getRequester())) // MemberDto로 변환
                .requesterReviewed(match.isRequesterReviewed())
                .hostReviewed(match.isHostReviewed())
                .build();
    }
}