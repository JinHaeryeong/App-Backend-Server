package com.dasom.dasomServer.DAO;


import com.dasom.dasomServer.DTO.HealthDataRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface HealthMapper {
    
    //새로운 데이터 저장
    void insertHealthData(HealthDataRequest healthDataRequest);
    
    //들어오는 값이 없을때 이전 값을 불러와서 덮어쓰기 하기위해 필요한거
    HealthDataRequest findLastHealthData(@Param("silverId") String silverId);

    /**
     * 특정 시간 범위 내에서 LSTM의 시퀀스 (N_STEPS=6)를 구축하기 위한 데이터 조회.
     * @param silverId 사용자 ID
     * @param startTime 시퀀스 시작 시간
     * @param endTime 시퀀스 종료 시간
     * @param limit 조회할 최대 개수 (6개)
     * @return DataPoint 리스트 (오래된 순)
     */
    List<HealthDataRequest> findSequenceData (
            @Param("silverId") String silverId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("limit") int limit
    );

    Optional<Double> findMinHeartRateDuringDeepSleep(@Param("userId") String userId, @Param("oneWeekAgo") LocalDateTime oneWeekAgo);
}
