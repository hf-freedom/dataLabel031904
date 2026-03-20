package com.datalabel.service;

import com.datalabel.entity.RoleOrganization;
import com.datalabel.mapper.RoleOrganizationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleOrganizationService {
    
    @Autowired
    private RoleOrganizationMapper roleOrganizationMapper;
    
    public List<RoleOrganization> findByRoleId(Long roleId) {
        return roleOrganizationMapper.findByRoleId(roleId);
    }
    
    public List<Long> findOrgIdsByRoleId(Long roleId) {
        return roleOrganizationMapper.findOrgIdsByRoleId(roleId);
    }
    
    public boolean bindOrgToRole(Long roleId, Long orgId) {
        RoleOrganization existing = roleOrganizationMapper.findByRoleIdAndOrgId(roleId, orgId);
        if (existing != null) {
            return true;
        }
        RoleOrganization roleOrg = new RoleOrganization();
        roleOrg.setRoleId(roleId);
        roleOrg.setOrganizationId(orgId);
        return roleOrganizationMapper.insert(roleOrg) > 0;
    }
    
    public boolean unbindOrgFromRole(Long roleId, Long orgId) {
        RoleOrganization roleOrg = roleOrganizationMapper.findByRoleIdAndOrgId(roleId, orgId);
        if (roleOrg != null) {
            return roleOrganizationMapper.deleteById(roleOrg.getId()) > 0;
        }
        return false;
    }
    
    public boolean bindOrgsToRole(Long roleId, List<Long> orgIds) {
        roleOrganizationMapper.deleteByRoleId(roleId);
        for (Long orgId : orgIds) {
            RoleOrganization roleOrg = new RoleOrganization();
            roleOrg.setRoleId(roleId);
            roleOrg.setOrganizationId(orgId);
            roleOrganizationMapper.insert(roleOrg);
        }
        return true;
    }
    
    public boolean deleteByRoleId(Long roleId) {
        return roleOrganizationMapper.deleteByRoleId(roleId) >= 0;
    }
    
    public boolean deleteByOrganizationId(Long orgId) {
        return roleOrganizationMapper.deleteByOrganizationId(orgId) >= 0;
    }
}
