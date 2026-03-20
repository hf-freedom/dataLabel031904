package com.datalabel.mapper;

import com.datalabel.cache.LocalCache;
import com.datalabel.entity.Role;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RoleMapper {
    
    private final LocalCache cache = LocalCache.getInstance();
    
    public Role findById(Long id) {
        return (Role) cache.get(id);
    }
    
    public Role findByCode(String code) {
        return cache.getAll(Role.class).stream()
                .filter(r -> code.equals(r.getCode()))
                .findFirst()
                .orElse(null);
    }
    
    public List<Role> findAll() {
        return cache.getAll(Role.class);
    }
    
    public int insert(Role role) {
        if (role.getId() == null) {
            role.setId(cache.generateId());
        }
        cache.put(role.getId(), role);
        return 1;
    }
    
    public int update(Role role) {
        if (cache.containsKey(role.getId())) {
            cache.put(role.getId(), role);
            return 1;
        }
        return 0;
    }
    
    public int deleteById(Long id) {
        cache.remove(id);
        return 1;
    }
}
