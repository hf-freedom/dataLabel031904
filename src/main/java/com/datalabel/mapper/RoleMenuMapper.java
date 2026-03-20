package com.datalabel.mapper;

import com.datalabel.cache.LocalCache;
import com.datalabel.entity.RoleMenu;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class RoleMenuMapper {
    
    private final LocalCache cache = LocalCache.getInstance();
    
    public RoleMenu findById(Long id) {
        return (RoleMenu) cache.get(id);
    }
    
    public List<RoleMenu> findAll() {
        return cache.getAll(RoleMenu.class);
    }
    
    public List<RoleMenu> findByRoleId(Long roleId) {
        return cache.getAll(RoleMenu.class).stream()
                .filter(rm -> roleId.equals(rm.getRoleId()))
                .collect(Collectors.toList());
    }
    
    public List<RoleMenu> findByMenuId(Long menuId) {
        return cache.getAll(RoleMenu.class).stream()
                .filter(rm -> menuId.equals(rm.getMenuId()))
                .collect(Collectors.toList());
    }
    
    public RoleMenu findByRoleIdAndMenuId(Long roleId, Long menuId) {
        return cache.getAll(RoleMenu.class).stream()
                .filter(rm -> roleId.equals(rm.getRoleId()) && menuId.equals(rm.getMenuId()))
                .findFirst()
                .orElse(null);
    }
    
    public List<Long> findMenuIdsByRoleId(Long roleId) {
        return cache.getAll(RoleMenu.class).stream()
                .filter(rm -> roleId.equals(rm.getRoleId()))
                .map(RoleMenu::getMenuId)
                .collect(Collectors.toList());
    }
    
    public int insert(RoleMenu roleMenu) {
        if (roleMenu.getId() == null) {
            roleMenu.setId(cache.generateId());
        }
        cache.put(roleMenu.getId(), roleMenu);
        return 1;
    }
    
    public int deleteById(Long id) {
        cache.remove(id);
        return 1;
    }
    
    public int deleteByRoleId(Long roleId) {
        List<RoleMenu> list = findByRoleId(roleId);
        for (RoleMenu rm : list) {
            cache.remove(rm.getId());
        }
        return list.size();
    }
    
    public int deleteByMenuId(Long menuId) {
        List<RoleMenu> list = findByMenuId(menuId);
        for (RoleMenu rm : list) {
            cache.remove(rm.getId());
        }
        return list.size();
    }
}
