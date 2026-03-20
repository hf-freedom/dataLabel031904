package com.datalabel.entity;

import java.io.Serializable;

public class RoleOrganization implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private Long roleId;
    private Long organizationId;
    
    public RoleOrganization() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }
}
