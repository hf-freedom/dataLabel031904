package com.datalabel.controller;

import com.datalabel.annotation.RequireApiPermission;
import com.datalabel.common.Result;
import com.datalabel.entity.Menu;
import com.datalabel.entity.User;
import com.datalabel.service.MenuService;
import com.datalabel.service.RoleMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/menu")
public class MenuController {
    
    @Autowired
    private MenuService menuService;
    
    @Autowired
    private RoleMenuService roleMenuService;
    
    @GetMapping("/list")
    @RequireApiPermission("menu:list")
    public Result<List<Menu>> list() {
        return Result.success(menuService.findAll());
    }
    
    @GetMapping("/tree")
    public Result<List<Map<String, Object>>> tree(HttpSession session) {
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
                return Result.success(menuService.buildMenuTree(Collections.emptyList()));
            }
            menus = menuService.findMenusByRoleId(roleId);
        }
        
        return Result.success(menuService.buildMenuTree(menus));
    }
    
    @GetMapping("/{id}")
    @RequireApiPermission("menu:view")
    public Result<Menu> getById(@PathVariable Long id) {
        Menu menu = menuService.findById(id);
        if (menu == null) {
            return Result.error("菜单不存在");
        }
        return Result.success(menu);
    }
    
    @PostMapping("/save")
    @RequireApiPermission("menu:save")
    public Result<String> save(@RequestBody Menu menu) {
        if (menuService.save(menu)) {
            return Result.success("保存成功", null);
        }
        return Result.error("保存失败");
    }
    
    @DeleteMapping("/{id}")
    @RequireApiPermission("menu:delete")
    public Result<String> delete(@PathVariable Long id, HttpSession session) {
        if (menuService.deleteById(id)) {
            return Result.success("删除成功", null);
        }
        return Result.error("删除失败");
    }
    
    @GetMapping("/role/{roleId}")
    @RequireApiPermission("menu:role")
    public Result<List<Long>> getMenuIdsByRoleId(@PathVariable Long roleId) {
        return Result.success(roleMenuService.findMenuIdsByRoleId(roleId));
    }
    
    @PostMapping("/bindRole")
    @RequireApiPermission("menu:bindRole")
    public Result<String> bindMenusToRole(@RequestParam Long roleId, @RequestBody List<Long> menuIds) {
        if (roleMenuService.bindMenusToRole(roleId, menuIds)) {
            return Result.success("绑定成功", null);
        }
        return Result.error("绑定失败");
    }
}
