package com.datalabel.mapper;

import com.datalabel.cache.LocalCache;
import com.datalabel.entity.ApiPermission;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ApiPermissionMapper {
    
    private final LocalCache cache = LocalCache.getInstance();
    
    public ApiPermission findById(Long id) {
        return (ApiPermission) cache.get(id);
    }
    
    public ApiPermission findByCode(String code) {
        return cache.getAll(ApiPermission.class).stream()
                .filter(a -> code.equals(a.getCode()))
                .findFirst()
                .orElse(null);
    }
    
    public List<ApiPermission> findAll() {
        return cache.getAll(ApiPermission.class);
    }
    
    public List<ApiPermission> findByMenuId(Long menuId) {
        return cache.getAll(ApiPermission.class).stream()
                .filter(a -> menuId.equals(a.getMenuId()))
                .collect(Collectors.toList());
    }
    
    public List<ApiPermission> findByIds(List<Long> ids) {
        return cache.getAll(ApiPermission.class).stream()
                .filter(a -> ids.contains(a.getId()))
                .collect(Collectors.toList());
    }
    
    public int insert(ApiPermission api) {
        if (api.getId() == null) {
            api.setId(cache.generateId());
        }
        cache.put(api.getId(), api);
        return 1;
    }
    
    public int update(ApiPermission api) {
        if (cache.containsKey(api.getId())) {
            cache.put(api.getId(), api);
            return 1;
        }
        return 0;
    }
    
    public int deleteById(Long id) {
        cache.remove(id);
        return 1;
    }
}
