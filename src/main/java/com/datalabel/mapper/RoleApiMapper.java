package com.datalabel.mapper;

import com.datalabel.cache.LocalCache;
import com.datalabel.entity.RoleApi;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class RoleApiMapper {
    
    private final LocalCache cache = LocalCache.getInstance();
    
    public RoleApi findById(Long id) {
        return (RoleApi) cache.get(id);
    }
    
    public List<RoleApi> findAll() {
        return cache.getAll(RoleApi.class);
    }
    
    public List<RoleApi> findByRoleId(Long roleId) {
        return cache.getAll(RoleApi.class).stream()
                .filter(ra -> roleId.equals(ra.getRoleId()))
                .collect(Collectors.toList());
    }
    
    public List<RoleApi> findByApiId(Long apiId) {
        return cache.getAll(RoleApi.class).stream()
                .filter(ra -> apiId.equals(ra.getApiId()))
                .collect(Collectors.toList());
    }
    
    public RoleApi findByRoleIdAndApiId(Long roleId, Long apiId) {
        return cache.getAll(RoleApi.class).stream()
                .filter(ra -> roleId.equals(ra.getRoleId()) && apiId.equals(ra.getApiId()))
                .findFirst()
                .orElse(null);
    }
    
    public List<Long> findApiIdsByRoleId(Long roleId) {
        return cache.getAll(RoleApi.class).stream()
                .filter(ra -> roleId.equals(ra.getRoleId()))
                .map(RoleApi::getApiId)
                .collect(Collectors.toList());
    }
    
    public int insert(RoleApi roleApi) {
        if (roleApi.getId() == null) {
            roleApi.setId(cache.generateId());
        }
        cache.put(roleApi.getId(), roleApi);
        return 1;
    }
    
    public int deleteById(Long id) {
        cache.remove(id);
        return 1;
    }
    
    public int deleteByRoleId(Long roleId) {
        List<RoleApi> list = findByRoleId(roleId);
        for (RoleApi ra : list) {
            cache.remove(ra.getId());
        }
        return list.size();
    }
    
    public int deleteByApiId(Long apiId) {
        List<RoleApi> list = findByApiId(apiId);
        for (RoleApi ra : list) {
            cache.remove(ra.getId());
        }
        return list.size();
    }
}
