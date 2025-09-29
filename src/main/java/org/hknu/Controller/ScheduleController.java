package org.hknu.Controller;

import org.hknu.Dto.ScheduleCreateRequest;
import org.hknu.Dto.ScheduleResponse;
import org.hknu.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(
            @PathVariable Long scheduleId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // 서비스 계층에 scheduleId와 사용자 이메일을 넘겨 삭제를 시도합니다.
            scheduleService.deleteSchedule(scheduleId, userDetails.getUsername());
            return ResponseEntity.noContent().build(); // 성공 시 204 No Content 응답
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).build(); // 권한이 없을 때 403 Forbidden
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build(); // 일정을 찾지 못했을 때 404 Not Found
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<ScheduleResponse>> getAllSchedules() {
        List<ScheduleResponse> allSchedules = scheduleService.getAllSchedules();
        return ResponseEntity.ok(allSchedules);
    }

    // ✅ 일정 생성 API
    @PostMapping
    public ResponseEntity<ScheduleResponse> createSchedule(@RequestBody ScheduleCreateRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        ScheduleResponse createdSchedule = scheduleService.createSchedule(request, userEmail);
        return ResponseEntity.ok(createdSchedule);
    }

    // ✅ 매칭 페이지를 위한 모든 일정 조회 API
    @GetMapping
    public ResponseEntity<List<ScheduleResponse>> getSchedules(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) List<String> categories) {
        String userEmail = userDetails.getUsername();
        scheduleService.deletePastSchedules();
        List<ScheduleResponse> schedules = scheduleService.getFilteredSchedules(userEmail, categories);
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/random")
    public ResponseEntity<ScheduleResponse> getRandomSchedule(@AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        scheduleService.deletePastSchedules();
        return scheduleService.getRandomSchedule(userEmail)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/my")
    public ResponseEntity<List<ScheduleResponse>> getMySchedules(@AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        scheduleService.deletePastSchedules();
        List<ScheduleResponse> schedules = scheduleService.getMySchedules(userEmail);
        return ResponseEntity.ok(schedules);
    }
}