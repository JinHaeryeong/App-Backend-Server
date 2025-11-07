package com.dasom.dasomServer.DAO;


import com.dasom.dasomServer.DTO.DailyHealthLogRequest;
import com.dasom.dasomServer.DTO.HealthRequest;
import lombok.Data;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface HealthMapper {

    // 새로운 데이터 저장
    void insertHealthData(HealthRequest healthDataRequest);

    // 들어오는 값이 없을 때 이전 값을 불러와서 덮어쓰기 하기 위해 필요한 메서드
    Optional<HealthRequest> findLastHealthData(@Param("silverId") String silverId);

    /**
     * 특정 시간 범위 내에서 LSTM의 시퀀스 (N_STEPS=6)를 구축하기 위한 데이터 조회.
     * @param silverId 사용자 ID
     * @param startTime 시퀀스 시작 시간
     * @param endTime 시퀀스 종료 시간
     * @param limit 조회할 최대 개수 (6개)
     * @return DataPoint 리스트 (오래된 순)
     */
    List<HealthRequest> findSequenceData (
            @Param("silverId") String silverId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("limit") int limit
    );

    @Data
    public static class StaticUserInfo {
        private String gender; // M/F 또는 1/0 값으로 변환 필요
        private LocalDate birthday; // Age 계산용
        private double rhr; // 0.0일 수 있음
    }

    // RHR 계산 로직에 필요한 모든 사용자 ID 리스트 조회
    List<String> findAllSilverIds();

    // LSTM 입력에 필요한 정적 데이터 조회
    StaticUserInfo findUserInfo(@Param("silverId") String silverId);

    // RHR 계산 후 silvers 테이블의 RHR 값 업데이트
    void updateRhr(@Param("silverId") String silverId, @Param("rhr") double rhr);

    // 결측치 체크 및 시퀀스 시작 여부 확인을 위해 저장된 데이터 개수 조회
    int countBySilverId(@Param("silverId") String silverId);

    /**
     * RHR 계산을 위한 최소 심박수 조회 (특정 기간 수면 기록 중 가장 낮은 심박수)
     * @param silverId 사용자 ID
     * @param startDate 조회 시작일
     * @param endDate 조회 종료일
     */
    Optional<Double> findMinHeartRateDuringDeepSleep(
            @Param("silverId") String silverId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    int upsertDailyHealthLog(DailyHealthLogRequest dailyHealthLogRequest);
    void insertAnalysisResult(@Param("silverId") String silverId, @Param("label") String label);
}
