package com.datalabel.mapper;

import com.datalabel.cache.LocalCache;
import com.datalabel.entity.RoleOrganization;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class RoleOrganizationMapper {
    
    private final LocalCache cache = LocalCache.getInstance();
    
    public RoleOrganization findById(Long id) {
        return (RoleOrganization) cache.get(id);
    }
    
    public List<RoleOrganization> findAll() {
        return cache.getAll(RoleOrganization.class);
    }
    
    public List<RoleOrganization> findByRoleId(Long roleId) {
        return cache.getAll(RoleOrganization.class).stream()
                .filter(ro -> roleId.equals(ro.getRoleId()))
                .collect(Collectors.toList());
    }
    
    public List<RoleOrganization> findByOrganizationId(Long orgId) {
        return cache.getAll(RoleOrganization.class).stream()
                .filter(ro -> orgId.equals(ro.getOrganizationId()))
                .collect(Collectors.toList());
    }
    
    public RoleOrganization findByRoleIdAndOrgId(Long roleId, Long orgId) {
        return cache.getAll(RoleOrganization.class).stream()
                .filter(ro -> roleId.equals(ro.getRoleId()) && orgId.equals(ro.getOrganizationId()))
                .findFirst()
                .orElse(null);
    }
    
    public List<Long> findOrgIdsByRoleId(Long roleId) {
        return cache.getAll(RoleOrganization.class).stream()
                .filter(ro -> roleId.equals(ro.getRoleId()))
                .map(RoleOrganization::getOrganizationId)
                .collect(Collectors.toList());
    }
    
    public int insert(RoleOrganization roleOrg) {
        if (roleOrg.getId() == null) {
            roleOrg.setId(cache.generateId());
        }
        cache.put(roleOrg.getId(), roleOrg);
        return 1;
    }
    
    public int deleteById(Long id) {
        cache.remove(id);
        return 1;
    }
    
    public int deleteByRoleId(Long roleId) {
        List<RoleOrganization> list = findByRoleId(roleId);
        for (RoleOrganization ro : list) {
            cache.remove(ro.getId());
        }
        return list.size();
    }
    
    public int deleteByOrganizationId(Long orgId) {
        List<RoleOrganization> list = findByOrganizationId(orgId);
        for (RoleOrganization ro : list) {
            cache.remove(ro.getId());
        }
        return list.size();
    }
}
