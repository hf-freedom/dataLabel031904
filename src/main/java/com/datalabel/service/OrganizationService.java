package com.datalabel.service;

import com.datalabel.entity.Organization;
import com.datalabel.mapper.OrganizationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrganizationService {
    
    @Autowired
    private OrganizationMapper organizationMapper;
    
    public Organization findById(Long id) {
        return organizationMapper.findById(id);
    }
    
    public List<Organization> findAll() {
        return organizationMapper.findAll();
    }
    
    public List<Organization> findByParentId(Long parentId) {
        return organizationMapper.findByParentId(parentId);
    }
    
    public boolean save(Organization org) {
        if (org.getId() == null) {
            return organizationMapper.insert(org) > 0;
        } else {
            return organizationMapper.update(org) > 0;
        }
    }
    
    public boolean update(Organization org) {
        return organizationMapper.update(org) > 0;
    }
    
    public boolean deleteById(Long id) {
        return organizationMapper.deleteById(id) > 0;
    }
}
