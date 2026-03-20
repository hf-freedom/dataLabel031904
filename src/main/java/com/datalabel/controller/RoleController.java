package com.datalabel.controller;

import com.datalabel.annotation.RequireApiPermission;
import com.datalabel.common.Result;
import com.datalabel.entity.Role;
import com.datalabel.service.RoleMenuService;
import com.datalabel.service.RoleOrganizationService;
import com.datalabel.service.RoleApiService;
import com.datalabel.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/role")
public class RoleController {
    
    @Autowired
    private RoleService roleService;
    
    @Autowired
    private RoleMenuService roleMenuService;
    
    @Autowired
    private RoleApiService roleApiService;
    
    @Autowired
    private RoleOrganizationService roleOrganizationService;
    
    @GetMapping("/list")
    @RequireApiPermission("role:list")
    public Result<List<Role>> list() {
        return Result.success(roleService.findAll());
    }
    
    @GetMapping("/{id}")
    @RequireApiPermission("role:view")
    public Result<Role> getById(@PathVariable Long id) {
        Role role = roleService.findById(id);
        if (role == null) {
            return Result.error("角色不存在");
        }
        return Result.success(role);
    }
    
    @PostMapping("/save")
    @RequireApiPermission("role:save")
    public Result<String> save(@RequestBody Role role) {
        if (roleService.save(role)) {
            return Result.success("保存成功", null);
        }
        return Result.error("保存失败");
    }
    
    @DeleteMapping("/{id}")
    @RequireApiPermission("role:delete")
    public Result<String> delete(@PathVariable Long id) {
        roleMenuService.deleteByRoleId(id);
        roleApiService.deleteByRoleId(id);
        roleOrganizationService.deleteByRoleId(id);
        if (roleService.deleteById(id)) {
            return Result.success("删除成功", null);
        }
        return Result.error("删除失败");
    }
    
    @GetMapping("/{roleId}/menus")
    @RequireApiPermission("role:menu")
    public Result<List<Long>> getRoleMenus(@PathVariable Long roleId) {
        return Result.success(roleMenuService.findMenuIdsByRoleId(roleId));
    }
    
    @PostMapping("/{roleId}/menus")
    @RequireApiPermission("role:bindMenu")
    public Result<String> bindMenus(@PathVariable Long roleId, @RequestBody List<Long> menuIds) {
        if (roleMenuService.bindMenusToRole(roleId, menuIds)) {
            return Result.success("绑定成功", null);
        }
        return Result.error("绑定失败");
    }
    
    @GetMapping("/{roleId}/apis")
    @RequireApiPermission("role:api")
    public Result<List<Long>> getRoleApis(@PathVariable Long roleId) {
        return Result.success(roleApiService.findApiIdsByRoleId(roleId));
    }
    
    @PostMapping("/{roleId}/apis")
    @RequireApiPermission("role:bindApi")
    public Result<String> bindApis(@PathVariable Long roleId, @RequestBody List<Long> apiIds) {
        if (roleApiService.bindApisToRole(roleId, apiIds)) {
            return Result.success("绑定成功", null);
        }
        return Result.error("绑定失败");
    }
    
    @GetMapping("/{roleId}/organizations")
    @RequireApiPermission("role:org")
    public Result<List<Long>> getRoleOrganizations(@PathVariable Long roleId) {
        return Result.success(roleOrganizationService.findOrgIdsByRoleId(roleId));
    }
    
    @PostMapping("/{roleId}/organizations")
    @RequireApiPermission("role:bindOrg")
    public Result<String> bindOrganizations(@PathVariable Long roleId, @RequestBody List<Long> orgIds) {
        if (roleOrganizationService.bindOrgsToRole(roleId, orgIds)) {
            return Result.success("绑定成功", null);
        }
        return Result.error("绑定失败");
    }
}
