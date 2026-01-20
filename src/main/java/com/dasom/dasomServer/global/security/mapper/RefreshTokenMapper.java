package com.dasom.dasomServer.global.security.mapper;

import com.dasom.dasomServer.DTO.RefreshToken;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RefreshTokenMapper {

    void save(RefreshToken refreshToken);

    RefreshToken findBySilverId(String silverId);

    // Refresh Token 값으로 조회
    RefreshToken findByRefreshToken(String refreshToken);

    // Refresh Token 삭제
    void deleteBySilverId(String silverId);
}