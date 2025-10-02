package org.hknu.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hknu.Dto.ScheduleCreateRequest;
import org.hknu.Dto.ScheduleResponse;
import org.hknu.Repo.MemberRepo;
import org.hknu.Repo.ScheduleRepo;
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
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        // 날짜가 지난 스케줄을 모두 조회한 후 하나씩 삭제
        List<Schedule> outdatedSchedules = scheduleRepository.findByDateLessThan(today);
        for (Schedule schedule : outdatedSchedules) {
            scheduleRepository.delete(schedule);  // matches도 함께 삭제됨
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

    // [기존 코드 유지] (내부의 deletePastSchedules 호출 제거)
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

    // [기존 코드 유지] (내부의 deletePastSchedules 호출 제거)
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

    // [기존 코드 유지] (내부의 deletePastSchedules 호출 제거)
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

    // [기존 코드 유지] (내부의 deletePastSchedules 호출 제거)
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getMySchedules(String userEmail) {
        List<Schedule> schedules = scheduleRepository.findByMemberEmail(userEmail);
        return schedules.stream()
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