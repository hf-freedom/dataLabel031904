package com.datalabel.service;

import com.datalabel.entity.User;
import com.datalabel.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    public User login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }
    
    public User findById(Long id) {
        return userMapper.findById(id);
    }
    
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }
    
    public List<User> findAll() {
        return userMapper.findAll();
    }
    
    public List<User> findByOrganizationId(Long orgId) {
        return userMapper.findByOrganizationId(orgId);
    }
    
    public List<User> findByRoleId(Long roleId) {
        return userMapper.findByRoleId(roleId);
    }
    
    public boolean save(User user) {
        if (user.getId() == null) {
            return userMapper.insert(user) > 0;
        } else {
            return userMapper.update(user) > 0;
        }
    }
    
    public boolean update(User user) {
        return userMapper.update(user) > 0;
    }
    
    public boolean deleteById(Long id) {
        return userMapper.deleteById(id) > 0;
    }
    
    public boolean bindRole(Long userId, Long roleId) {
        User user = userMapper.findById(userId);
        if (user != null) {
            user.setRoleId(roleId);
            return userMapper.update(user) > 0;
        }
        return false;
    }
}
