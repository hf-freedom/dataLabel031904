package com.datalabel.entity;

import java.io.Serializable;

public class RoleApi implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private Long roleId;
    private Long apiId;
    
    public RoleApi() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
    public Long getApiId() { return apiId; }
    public void setApiId(Long apiId) { this.apiId = apiId; }
}
