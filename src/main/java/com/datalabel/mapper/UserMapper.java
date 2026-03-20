package com.datalabel.mapper;

import com.datalabel.cache.LocalCache;
import com.datalabel.entity.User;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class UserMapper {
    
    private final LocalCache cache = LocalCache.getInstance();
    
    public User findById(Long id) {
        return (User) cache.get(id);
    }
    
    public User findByUsername(String username) {
        return cache.getAll(User.class).stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }
    
    public List<User> findAll() {
        return cache.getAll(User.class);
    }
    
    public List<User> findByOrganizationId(Long orgId) {
        return cache.getAll(User.class).stream()
                .filter(u -> orgId.equals(u.getOrganizationId()))
                .collect(Collectors.toList());
    }
    
    public List<User> findByRoleId(Long roleId) {
        return cache.getAll(User.class).stream()
                .filter(u -> roleId.equals(u.getRoleId()))
                .collect(Collectors.toList());
    }
    
    public int insert(User user) {
        if (user.getId() == null) {
            user.setId(cache.generateId());
        }
        cache.put(user.getId(), user);
        return 1;
    }
    
    public int update(User user) {
        if (cache.containsKey(user.getId())) {
            cache.put(user.getId(), user);
            return 1;
        }
        return 0;
    }
    
    public int deleteById(Long id) {
        cache.remove(id);
        return 1;
    }
}
