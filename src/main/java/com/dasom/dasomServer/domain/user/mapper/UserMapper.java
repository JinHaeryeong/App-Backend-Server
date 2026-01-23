package com.dasom.dasomServer.domain.user.mapper;

import com.dasom.dasomServer.domain.user.dto.User;
import com.dasom.dasomServer.domain.user.dto.UserImage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {

    int insertUser(User request);

    User findByLoginId(String loginId);
    // 이미지 정보 저장
    int insertUserImage(UserImage userImage);

    int existsByLoginId(String loginId);

    User selectUserById(Long id);

    List<User> selectAllUsers();

}