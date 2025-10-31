package com.dasom.dasomServer.dao; // 👈 패키지명을 소문자 'dao'로 수정

import com.dasom.dasomServer.dto.User; // 👈 User 클래스는 DTO 패키지에서 임포트
import org.apache.ibatis.annotations.Mapper;

import java.util.List; // UserServiceImpl에서 List를 사용하므로 추가 (필요시)

@Mapper
public interface UserDAO {

    /** * 회원가입 쿼리: RegisterRequest DTO를 사용
     */
    int insertUser(User request);

    /** * 로그인/조회 쿼리: 조회 결과는 User Entity/DTO에 매핑
     */
    User selectUserByLoginId(String loginId);

    // UserServiceImpl에서 사용하는 메서드를 위해 추가 (주석 처리된 메서드 포함)
    int existsByLoginId(String loginId);

    User selectUserById(Long id);

    List<User> selectAllUsers();
}