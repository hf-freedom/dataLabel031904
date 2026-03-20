package com.datalabel.controller;

import com.datalabel.common.Result;
import com.datalabel.entity.*;
import com.datalabel.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @Autowired
    private MenuService menuService;
    
    @Autowired
    private ApiPermissionService apiPermissionService;
    
    @Autowired
    private RoleMenuService roleMenuService;
    
    @Autowired
    private RoleApiService roleApiService;
    
    @Autowired
    private RoleOrganizationService roleOrganizationService;
    
    @Autowired
    private RoleService roleService;
    
    @GetMapping("/menus")
    public Result<List<Menu>> getAllMenus() {
        return Result.success(menuService.findAll());
    }
    
    @GetMapping("/apis")
    public Result<List<ApiPermission>> getAllApis() {
        return Result.success(apiPermissionService.findAll());
    }
    
    @GetMapping("/roles")
    public Result<List<Role>> getAllRoles() {
        return Result.success(roleService.findAll());
    }
    
    @GetMapping("/role/{roleId}/menus")
    public Result<List<Long>> getRoleMenus(@PathVariable Long roleId) {
        return Result.success(roleMenuService.findMenuIdsByRoleId(roleId));
    }
    
    @GetMapping("/role/{roleId}/apis")
    public Result<List<Long>> getRoleApis(@PathVariable Long roleId) {
        return Result.success(roleApiService.findApiIdsByRoleId(roleId));
    }
    
    @GetMapping("/role/{roleId}/orgs")
    public Result<List<Long>> getRoleOrgs(@PathVariable Long roleId) {
        return Result.success(roleOrganizationService.findOrgIdsByRoleId(roleId));
    }
    
    @GetMapping("/menu/tree")
    public Result<List<?>> getMenuTree(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        List<Menu> menus;
        if (currentUser.getUserType() != null && currentUser.getUserType() == 1) {
            menus = menuService.findAll();
        } else {
            Long roleId = currentUser.getRoleId();
            if (roleId == null) {
                menus = java.util.Collections.emptyList();
            } else {
                menus = menuService.findMenusByRoleId(roleId);
            }
        }
        return Result.success(menuService.buildMenuTree(menus));
    }
}
