package com.datalabel.controller;

import com.datalabel.annotation.RequireApiPermission;
import com.datalabel.common.Result;
import com.datalabel.entity.Organization;
import com.datalabel.service.OrganizationService;
import com.datalabel.service.RoleOrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/org")
public class OrganizationController {
    
    @Autowired
    private OrganizationService organizationService;
    
    @Autowired
    private RoleOrganizationService roleOrganizationService;
    
    @GetMapping("/list")
    @RequireApiPermission("org:list")
    public Result<List<Organization>> list() {
        return Result.success(organizationService.findAll());
    }
    
    @GetMapping("/tree")
    @RequireApiPermission("org:list")
    public Result<List<Organization>> tree() {
        return Result.success(organizationService.findAll());
    }
    
    @GetMapping("/{id}")
    @RequireApiPermission("org:view")
    public Result<Organization> getById(@PathVariable Long id) {
        Organization org = organizationService.findById(id);
        if (org == null) {
            return Result.error("组织机构不存在");
        }
        return Result.success(org);
    }
    
    @PostMapping("/save")
    @RequireApiPermission("org:save")
    public Result<String> save(@RequestBody Organization org) {
        if (organizationService.save(org)) {
            return Result.success("保存成功", null);
        }
        return Result.error("保存失败");
    }
    
    @DeleteMapping("/{id}")
    @RequireApiPermission("org:delete")
    public Result<String> delete(@PathVariable Long id) {
        roleOrganizationService.deleteByOrganizationId(id);
        if (organizationService.deleteById(id)) {
            return Result.success("删除成功", null);
        }
        return Result.error("删除失败");
    }
    
    @GetMapping("/role/{roleId}")
    @RequireApiPermission("org:role")
    public Result<List<Long>> getOrgIdsByRoleId(@PathVariable Long roleId) {
        return Result.success(roleOrganizationService.findOrgIdsByRoleId(roleId));
    }
}
