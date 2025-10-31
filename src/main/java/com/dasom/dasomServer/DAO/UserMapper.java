package com.dasom.dasomServer.DAO;

import com.dasom.dasomServer.DTO.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    // C: 사용자 등록 (회원가입) - Triple Quotes로 변경하여 문자열 연결 오류 방지
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("""
        INSERT INTO silvers (caregiver_id, login_id, password, name, gender, birthday)
        VALUES (#{caregiverId}, #{loginId}, #{password}, #{username}, #{gender}, #{birthday})
    """)
    void insertUser(User user);

    // R: ID로 사용자 단건 조회
    @Select("SELECT id, login_id, name, gender FROM silvers WHERE id = #{id}")
    User selectUserById(Long id);

    // R: 로그인 ID로 사용자 조회 (인증에 사용)
    @Select("SELECT id, login_id, password, name FROM silvers WHERE login_id = #{loginId}")
    User selectUserByLoginId(String loginId);

    // R: 전체 사용자 조회
    @Select("SELECT id, login_id, name, gender FROM silvers")
    List<User> selectAllUsers();

    // U: 사용자 정보 업데이트
    @Update("UPDATE silvers SET name = #{username}, gender = #{gender} WHERE id = #{id}")
    int updateUser(User user);

    // D: 사용자 삭제
    @Delete("DELETE FROM silvers WHERE id = #{id}")
    int deleteUser(Long id);
}