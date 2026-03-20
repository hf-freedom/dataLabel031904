package com.datalabel.service;

import com.datalabel.entity.Role;
import com.datalabel.mapper.RoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {
    
    @Autowired
    private RoleMapper roleMapper;
    
    public Role findById(Long id) {
        return roleMapper.findById(id);
    }
    
    public List<Role> findAll() {
        return roleMapper.findAll();
    }
    
    public boolean save(Role role) {
        if (role.getId() == null) {
            return roleMapper.insert(role) > 0;
        } else {
            return roleMapper.update(role) > 0;
        }
    }
    
    public boolean update(Role role) {
        return roleMapper.update(role) > 0;
    }
    
    public boolean deleteById(Long id) {
        return roleMapper.deleteById(id) > 0;
    }
}
