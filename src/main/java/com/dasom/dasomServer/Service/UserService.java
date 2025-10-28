package com.dasom.dasomServer.Service;

import com.dasom.dasomServer.DTO.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserService {

    void createUser(User user);

    User getUserById(Long id);

    List<User> getAllUsers();

    boolean updateUser(User user);

    boolean deleteUser(Long id);
}