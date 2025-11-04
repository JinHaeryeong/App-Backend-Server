package com.dasom.dasomServer.DAO;

import com.dasom.dasomServer.DTO.RefreshToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RefreshTokenDAO {

    // Refresh Token 저장 또는 업데이트 (Upsert 역할)
    void save(@Param("refreshToken") RefreshToken refreshToken);

    // silverId로 Refresh Token 조회
    RefreshToken findBySilverId(String silverId);

    // Refresh Token 값으로 조회
    RefreshToken findByRefreshToken(String refreshToken);

    // Refresh Token 삭제
    void deleteBySilverId(String silverId);
}
