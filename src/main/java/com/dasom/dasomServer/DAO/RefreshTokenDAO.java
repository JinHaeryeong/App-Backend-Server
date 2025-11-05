package com.dasom.dasomServer.DAO;

import com.dasom.dasomServer.DTO.RefreshToken;
import org.apache.ibatis.annotations.Mapper;
// @Param 어노테이션은 save(RefreshToken) 형태에서는 필요 없으므로 import 삭제 가능

@Mapper
public interface RefreshTokenDAO {

    void save(RefreshToken refreshToken);

    RefreshToken findBySilverId(String silverId);

    // Refresh Token 값으로 조회
    RefreshToken findByRefreshToken(String refreshToken);

    // [수정 3] 'silverId' 파라미터 타입을 'String'으로 변경
    void deleteBySilverId(String silverId);
}