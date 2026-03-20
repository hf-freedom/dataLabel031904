package com.datalabel.service;

import com.datalabel.entity.Menu;
import com.datalabel.mapper.MenuMapper;
import com.datalabel.mapper.RoleMenuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MenuService {
    
    @Autowired
    private MenuMapper menuMapper;
    
    @Autowired
    private RoleMenuMapper roleMenuMapper;
    
    public Menu findById(Long id) {
        return menuMapper.findById(id);
    }
    
    public List<Menu> findAll() {
        return menuMapper.findAll();
    }
    
    public List<Menu> findRootMenus() {
        return menuMapper.findRootMenus();
    }
    
    public List<Menu> findByParentId(Long parentId) {
        return menuMapper.findByParentId(parentId);
    }
    
    public List<Menu> findMenusByRoleId(Long roleId) {
        List<Long> menuIds = roleMenuMapper.findMenuIdsByRoleId(roleId);
        if (menuIds.isEmpty()) {
            return new ArrayList<>();
        }
        return menuMapper.findByIds(menuIds);
    }
    
    public List<Menu> findMenusByRoleIds(List<Long> roleIds) {
        List<Long> menuIds = new ArrayList<>();
        for (Long roleId : roleIds) {
            menuIds.addAll(roleMenuMapper.findMenuIdsByRoleId(roleId));
        }
        if (menuIds.isEmpty()) {
            return new ArrayList<>();
        }
        menuIds = menuIds.stream().distinct().collect(Collectors.toList());
        return menuMapper.findByIds(menuIds);
    }
    
    public List<Map<String, Object>> buildMenuTree(List<Menu> menus) {
        List<Map<String, Object>> tree = new ArrayList<>();
        Map<Long, Map<String, Object>> menuMap = new HashMap<>();
        
        for (Menu menu : menus) {
            Map<String, Object> node = new HashMap<>();
            node.put("id", menu.getId());
            node.put("name", menu.getName());
            node.put("code", menu.getCode());
            node.put("path", menu.getPath());
            node.put("icon", menu.getIcon());
            node.put("sort", menu.getSort());
            node.put("children", new ArrayList<Map<String, Object>>());
            menuMap.put(menu.getId(), node);
        }
        
        for (Menu menu : menus) {
            Map<String, Object> node = menuMap.get(menu.getId());
            if (menu.getParentId() == null || menu.getParentId() == 0L) {
                tree.add(node);
            } else {
                Map<String, Object> parentNode = menuMap.get(menu.getParentId());
                if (parentNode != null) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> children = (List<Map<String, Object>>) parentNode.get("children");
                    children.add(node);
                }
            }
        }
        
        return tree;
    }
    
    public boolean save(Menu menu) {
        if (menu.getId() == null) {
            return menuMapper.insert(menu) > 0;
        } else {
            return menuMapper.update(menu) > 0;
        }
    }
    
    public boolean update(Menu menu) {
        return menuMapper.update(menu) > 0;
    }
    
    public boolean deleteById(Long id) {
        roleMenuMapper.deleteByMenuId(id);
        return menuMapper.deleteById(id) > 0;
    }
}
