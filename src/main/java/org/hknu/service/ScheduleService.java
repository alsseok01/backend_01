package org.hknu.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hknu.Dto.ScheduleCreateRequest;
import org.hknu.Dto.ScheduleResponse;
import org.hknu.Repo.MemberRepo;
import org.hknu.Repo.ScheduleRepo;
import org.hknu.entity.Match;
import org.hknu.entity.Member;
import org.hknu.entity.Schedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ScheduleService {

    @Autowired
    private ScheduleRepo scheduleRepository;
    @Autowired
    private MemberRepo memberRepository;
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * ✅ [수정] 과거 일정을 삭제하는 메소드에 @Transactional을 붙여
     * 독립적인 "수정 가능" 트랜잭션으로 만듭니다.
     */
    @Transactional
    public void deletePastSchedules() {
        LocalDate today = LocalDate.now();
        LocalDate limitDate = today.plusDays(21);  // 3주(21일) 이후 날짜 계산
        List<Schedule> allSchedules = scheduleRepository.findAll();
        for (Schedule schedule : allSchedules) {
            LocalDate scheduleDate = LocalDate.parse(schedule.getDate());
            // 오늘 이전이거나 limitDate 이후이면 삭제
            if (scheduleDate.isBefore(today) || scheduleDate.isAfter(limitDate)) {
                scheduleRepository.delete(schedule);

                boolean hasReviews = schedule.getMatches().stream()
                        .anyMatch(match -> !match.getReviews().isEmpty());

                // ✅ 리뷰가 없는 경우에만 안전하게 삭제
                if (!hasReviews) {
                    scheduleRepository.delete(schedule);
                }
            }
        }
    }

    // [기존 코드 유지]
    @Transactional
    public void deleteSchedule(Long scheduleId, String userEmail) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 일정을 찾을 수 없습니다."));

        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

        if (!schedule.getMember().getId().equals(member.getId())) {
            throw new IllegalStateException("해당 일정을 삭제할 권한이 없습니다.");
        }
        scheduleRepository.delete(schedule);
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> getAllSchedules() {
        deletePastSchedules();
        List<Schedule> allSchedules = scheduleRepository.findAll();
        return allSchedules.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // [기존 코드 유지]
    @Transactional
    public ScheduleResponse createSchedule(ScheduleCreateRequest request, String userEmail) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Schedule schedule = Schedule.builder()
                .date(request.getDate())
                .time(request.getTime())
                .text(request.getText())
                .placeName(request.getPlaceName())
                .placeCategory(request.getPlaceCategory())
                .participants(request.getParticipants())
                .currentParticipants(1)
                .member(member)
                .build();

        Schedule savedSchedule = scheduleRepository.save(schedule);
        return convertToResponse(savedSchedule);
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> getFilteredSchedules(String userEmail, List<String> categories) {
        Member currentUser = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        List<Schedule> schedules;
        if (categories == null || categories.isEmpty()) {
            schedules = scheduleRepository.findAllByMemberNot(currentUser);
        } else {
            schedules = scheduleRepository.findWithCategoryFilter(currentUser, categories);
        }

        return schedules.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<ScheduleResponse> getRandomSchedule(String userEmail) {
        Member currentUser = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        List<Schedule> schedules = scheduleRepository.findAllByMemberNot(currentUser);
        if (schedules.isEmpty()) {
            return Optional.empty();
        }

        Collections.shuffle(schedules);
        return Optional.of(convertToResponse(schedules.get(0)));
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> getMySchedules(String userEmail) {
        List<Schedule> schedules = scheduleRepository.findByMemberEmail(userEmail); // 1. 내가 만든 모든 일정을 가져옵니다.

        // 2. "완료된" 일정을 필터링합니다.
        List<Schedule> filteredSchedules = schedules.stream()
                .filter(schedule -> {
                    // 2-1. 일정에 연결된 매치 목록을 가져옵니다.
                    List<Match> matches = schedule.getMatches();

                    // 2-2. 매치가 없으면(아직 매칭 안됨) 무조건 표시합니다.
                    if (matches == null || matches.isEmpty()) {
                        return true; // Keep
                    }

                    // 2-3. 'CONFIRMED' 상태인 매치 목록을 찾습니다.
                    List<Match> confirmedMatches = matches.stream()
                            .filter(m -> m.getStatus() == Match.MatchStatus.CONFIRMED)
                            .collect(Collectors.toList());

                    // 2-4. 'CONFIRMED'된 매치가 하나도 없으면(아직 약속 확정 전) 무조건 표시합니다.
                    if (confirmedMatches.isEmpty()) {
                        return true; // Keep
                    }

                    // 2-5. 'CONFIRMED'된 매치 중, *모든* 매치가 양측(호스트, 신청자) 리뷰를 완료했는지 확인합니다.
                    boolean allConfirmedMatchesAreReviewed = confirmedMatches.stream()
                            .allMatch(m -> m.isHostReviewed() && m.isRequesterReviewed());

                    // 2-6. 만약 모든 확정된 매치가 리뷰 완료(true)라면, 이 일정은 "완료됨"이므로 숨깁니다(false 반환).
                    //      하나라도 리뷰가 안 된 매치가(false) 있다면, 아직 "진행중"이므로 표시합니다(true 반환).
                    return !allConfirmedMatchesAreReviewed; // Keep if NOT all reviewed
                })
                .collect(Collectors.toList());

        // 3. 필터링된 목록만 DTO로 변환하여 반환합니다.
        return filteredSchedules.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // [기존 코드 유지]
    private ScheduleResponse convertToResponse(Schedule schedule) {
        Member member = schedule.getMember();
        Map<String, Boolean> preferencesMap = null;
        try {
            if (member.getPreferences() != null && !member.getPreferences().isEmpty()) {
                preferencesMap = objectMapper.readValue(member.getPreferences(), new TypeReference<>() {});
            }
        } catch (Exception e) {
            // 로깅 처리
        }

        ScheduleResponse.UserDto userDto = ScheduleResponse.UserDto.builder()
                .id(member.getId())
                .name(member.getName())
                .profileImage(member.getProfileImage())
                .preferences(preferencesMap)
                .averageRating(member.getAverageRating()) // 평점 정보 추가
                .reviewCount(member.getReviewCount())     // 후기 개수 정보 추가
                .build();

        return ScheduleResponse.builder()
                .id(schedule.getId())
                .date(schedule.getDate())
                .time(schedule.getTime())
                .text(schedule.getText())
                .placeName(schedule.getPlaceName())
                .placeCategory(schedule.getPlaceCategory())
                .participants(schedule.getParticipants())
                .currentParticipants(schedule.getCurrentParticipants())
                .user(userDto)
                .build();
    }
}