package com.dasom.dasomServer.Service;

import com.dasom.dasomServer.DAO.UserMapper;
import com.dasom.dasomServer.DTO.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    @Transactional
    public void createUser(User user) {
        userMapper.insertUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userMapper.selectUserById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userMapper.selectAllUsers();
    }

    @Override
    @Transactional
    public boolean updateUser(User user) {
        int updatedRows = userMapper.updateUser(user);
        return updatedRows > 0;
    }

    @Override
    @Transactional
    public boolean deleteUser(Long id) {
        int deletedRows = userMapper.deleteUser(id);
        return deletedRows > 0;
    }
}