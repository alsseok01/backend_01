package org.hknu.Repo;

import org.hknu.entity.Member;
import org.hknu.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ScheduleRepo extends JpaRepository<Schedule, Long> {
    // ✅ 매칭 기능을 위해, 자신을 제외한 모든 사용자의 일정을 찾는 쿼리 메소드
    List<Schedule> findAllByMemberNot(Member member);
    @Query("SELECT s FROM Schedule s WHERE s.member <> :member AND s.placeCategory IN :categories")
    List<Schedule> findWithCategoryFilter(@Param("member") Member member, @Param("categories") List<String> categories);
    List<Schedule> findByMemberEmail(String email);

    @Modifying // 이 쿼리가 데이터를 수정하는 작업임을 명시합니다.
    @Transactional // 삭제 작업은 트랜잭션 안에서 처리되어야 합니다.
    @Query("DELETE FROM Schedule s WHERE s.date < :today")
    void deleteByDateLessThan(@Param("today") String today);
}