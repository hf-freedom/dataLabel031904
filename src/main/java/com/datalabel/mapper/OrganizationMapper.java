package com.datalabel.mapper;

import com.datalabel.cache.LocalCache;
import com.datalabel.entity.Organization;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class OrganizationMapper {
    
    private final LocalCache cache = LocalCache.getInstance();
    
    public Organization findById(Long id) {
        return (Organization) cache.get(id);
    }
    
    public List<Organization> findAll() {
        return cache.getAll(Organization.class);
    }
    
    public List<Organization> findByParentId(Long parentId) {
        return cache.getAll(Organization.class).stream()
                .filter(o -> parentId.equals(o.getParentId()))
                .collect(Collectors.toList());
    }
    
    public int insert(Organization org) {
        if (org.getId() == null) {
            org.setId(cache.generateId());
        }
        cache.put(org.getId(), org);
        return 1;
    }
    
    public int update(Organization org) {
        if (cache.containsKey(org.getId())) {
            cache.put(org.getId(), org);
            return 1;
        }
        return 0;
    }
    
    public int deleteById(Long id) {
        cache.remove(id);
        return 1;
    }
}
