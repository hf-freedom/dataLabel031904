package com.datalabel.controller;

import com.datalabel.annotation.RequireApiPermission;
import com.datalabel.common.Result;
import com.datalabel.entity.ApiPermission;
import com.datalabel.service.ApiPermissionService;
import com.datalabel.service.RoleApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permission")
public class ApiPermissionController {
    
    @Autowired
    private ApiPermissionService apiPermissionService;
    
    @Autowired
    private RoleApiService roleApiService;
    
    @GetMapping("/list")
    @RequireApiPermission("api:list")
    public Result<List<ApiPermission>> list() {
        return Result.success(apiPermissionService.findAll());
    }
    
    @GetMapping("/{id}")
    @RequireApiPermission("api:view")
    public Result<ApiPermission> getById(@PathVariable Long id) {
        ApiPermission api = apiPermissionService.findById(id);
        if (api == null) {
            return Result.error("API权限不存在");
        }
        return Result.success(api);
    }
    
    @GetMapping("/menu/{menuId}")
    @RequireApiPermission("api:list")
    public Result<List<ApiPermission>> getByMenuId(@PathVariable Long menuId) {
        return Result.success(apiPermissionService.findByMenuId(menuId));
    }
    
    @PostMapping("/save")
    @RequireApiPermission("api:save")
    public Result<String> save(@RequestBody ApiPermission api) {
        if (apiPermissionService.save(api)) {
            return Result.success("保存成功", null);
        }
        return Result.error("保存失败");
    }
    
    @DeleteMapping("/{id}")
    @RequireApiPermission("api:delete")
    public Result<String> delete(@PathVariable Long id) {
        if (apiPermissionService.deleteById(id)) {
            return Result.success("删除成功", null);
        }
        return Result.error("删除失败");
    }
    
    @GetMapping("/role/{roleId}")
    @RequireApiPermission("api:role")
    public Result<List<Long>> getApiIdsByRoleId(@PathVariable Long roleId) {
        return Result.success(roleApiService.findApiIdsByRoleId(roleId));
    }
    
    @PostMapping("/bindRole")
    @RequireApiPermission("api:bindRole")
    public Result<String> bindApisToRole(@RequestParam Long roleId, @RequestBody List<Long> apiIds) {
        if (roleApiService.bindApisToRole(roleId, apiIds)) {
            return Result.success("绑定成功", null);
        }
        return Result.error("绑定失败");
    }
}
