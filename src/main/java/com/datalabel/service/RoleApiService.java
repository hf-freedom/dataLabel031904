package com.datalabel.service;

import com.datalabel.entity.RoleApi;
import com.datalabel.mapper.RoleApiMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleApiService {
    
    @Autowired
    private RoleApiMapper roleApiMapper;
    
    public List<RoleApi> findByRoleId(Long roleId) {
        return roleApiMapper.findByRoleId(roleId);
    }
    
    public List<Long> findApiIdsByRoleId(Long roleId) {
        return roleApiMapper.findApiIdsByRoleId(roleId);
    }
    
    public boolean bindApiToRole(Long roleId, Long apiId) {
        RoleApi existing = roleApiMapper.findByRoleIdAndApiId(roleId, apiId);
        if (existing != null) {
            return true;
        }
        RoleApi roleApi = new RoleApi();
        roleApi.setRoleId(roleId);
        roleApi.setApiId(apiId);
        return roleApiMapper.insert(roleApi) > 0;
    }
    
    public boolean unbindApiFromRole(Long roleId, Long apiId) {
        RoleApi roleApi = roleApiMapper.findByRoleIdAndApiId(roleId, apiId);
        if (roleApi != null) {
            return roleApiMapper.deleteById(roleApi.getId()) > 0;
        }
        return false;
    }
    
    public boolean bindApisToRole(Long roleId, List<Long> apiIds) {
        roleApiMapper.deleteByRoleId(roleId);
        for (Long apiId : apiIds) {
            RoleApi roleApi = new RoleApi();
            roleApi.setRoleId(roleId);
            roleApi.setApiId(apiId);
            roleApiMapper.insert(roleApi);
        }
        return true;
    }
    
    public boolean deleteByRoleId(Long roleId) {
        return roleApiMapper.deleteByRoleId(roleId) >= 0;
    }
    
    public boolean deleteByApiId(Long apiId) {
        return roleApiMapper.deleteByApiId(apiId) >= 0;
    }
}
