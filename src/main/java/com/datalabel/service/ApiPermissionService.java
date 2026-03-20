package com.datalabel.service;

import com.datalabel.entity.ApiPermission;
import com.datalabel.mapper.ApiPermissionMapper;
import com.datalabel.mapper.RoleApiMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApiPermissionService {
    
    @Autowired
    private ApiPermissionMapper apiPermissionMapper;
    
    @Autowired
    private RoleApiMapper roleApiMapper;
    
    public ApiPermission findById(Long id) {
        return apiPermissionMapper.findById(id);
    }
    
    public ApiPermission findByCode(String code) {
        return apiPermissionMapper.findByCode(code);
    }
    
    public List<ApiPermission> findAll() {
        return apiPermissionMapper.findAll();
    }
    
    public List<ApiPermission> findByMenuId(Long menuId) {
        return apiPermissionMapper.findByMenuId(menuId);
    }
    
    public List<ApiPermission> findByRoleId(Long roleId) {
        List<Long> apiIds = roleApiMapper.findApiIdsByRoleId(roleId);
        if (apiIds.isEmpty()) {
            return new ArrayList<>();
        }
        return apiPermissionMapper.findByIds(apiIds);
    }
    
    public List<ApiPermission> findByRoleIds(List<Long> roleIds) {
        List<Long> apiIds = new ArrayList<>();
        for (Long roleId : roleIds) {
            apiIds.addAll(roleApiMapper.findApiIdsByRoleId(roleId));
        }
        if (apiIds.isEmpty()) {
            return new ArrayList<>();
        }
        apiIds = apiIds.stream().distinct().collect(Collectors.toList());
        return apiPermissionMapper.findByIds(apiIds);
    }
    
    public List<String> findApiCodesByRoleIds(List<Long> roleIds) {
        List<ApiPermission> apis = findByRoleIds(roleIds);
        return apis.stream()
                .map(ApiPermission::getCode)
                .collect(Collectors.toList());
    }
    
    public boolean hasPermission(List<Long> roleIds, String apiCode) {
        List<String> apiCodes = findApiCodesByRoleIds(roleIds);
        return apiCodes.contains(apiCode);
    }
    
    public boolean save(ApiPermission api) {
        if (api.getId() == null) {
            return apiPermissionMapper.insert(api) > 0;
        } else {
            return apiPermissionMapper.update(api) > 0;
        }
    }
    
    public boolean update(ApiPermission api) {
        return apiPermissionMapper.update(api) > 0;
    }
    
    public boolean deleteById(Long id) {
        roleApiMapper.deleteByApiId(id);
        return apiPermissionMapper.deleteById(id) > 0;
    }
}
