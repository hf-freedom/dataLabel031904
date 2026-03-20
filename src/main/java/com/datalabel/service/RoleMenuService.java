package com.datalabel.service;

import com.datalabel.entity.RoleMenu;
import com.datalabel.mapper.RoleMenuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleMenuService {
    
    @Autowired
    private RoleMenuMapper roleMenuMapper;
    
    public List<RoleMenu> findByRoleId(Long roleId) {
        return roleMenuMapper.findByRoleId(roleId);
    }
    
    public List<Long> findMenuIdsByRoleId(Long roleId) {
        return roleMenuMapper.findMenuIdsByRoleId(roleId);
    }
    
    public boolean bindMenuToRole(Long roleId, Long menuId) {
        RoleMenu existing = roleMenuMapper.findByRoleIdAndMenuId(roleId, menuId);
        if (existing != null) {
            return true;
        }
        RoleMenu roleMenu = new RoleMenu();
        roleMenu.setRoleId(roleId);
        roleMenu.setMenuId(menuId);
        return roleMenuMapper.insert(roleMenu) > 0;
    }
    
    public boolean unbindMenuFromRole(Long roleId, Long menuId) {
        RoleMenu roleMenu = roleMenuMapper.findByRoleIdAndMenuId(roleId, menuId);
        if (roleMenu != null) {
            return roleMenuMapper.deleteById(roleMenu.getId()) > 0;
        }
        return false;
    }
    
    public boolean bindMenusToRole(Long roleId, List<Long> menuIds) {
        roleMenuMapper.deleteByRoleId(roleId);
        for (Long menuId : menuIds) {
            RoleMenu roleMenu = new RoleMenu();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            roleMenuMapper.insert(roleMenu);
        }
        return true;
    }
    
    public boolean deleteByRoleId(Long roleId) {
        return roleMenuMapper.deleteByRoleId(roleId) >= 0;
    }
    
    public boolean deleteByMenuId(Long menuId) {
        return roleMenuMapper.deleteByMenuId(menuId) >= 0;
    }
}
