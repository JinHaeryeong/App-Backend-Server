package com.dasom.dasomServer.DAO;

import com.dasom.dasomServer.DTO.Caregiver;
import com.dasom.dasomServer.DTO.Guardian;
import com.dasom.dasomServer.DTO.User;
import com.dasom.dasomServer.DTO.UserImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface UserDAO {

    int insertUser(User request);

    User findByLoginId(String loginId);
    // 이미지 정보 저장
    int insertUserImage(UserImage userImage);

    int existsByLoginId(String loginId);

    User selectUserById(Long id);

    List<User> selectAllUsers();

    Caregiver findCaregiverById(Long id);

    Caregiver findCaregiverBySilverLoginId(String silverLoginId);

    Caregiver findCaregiverByLoginId(String loginId);

    String findGuardianStoredFilenameByGuardianId(Long guardianId);

    List<Guardian> findGuardiansBySilverId(String silverId);
}