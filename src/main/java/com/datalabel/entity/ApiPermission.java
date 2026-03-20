package com.datalabel.entity;

import java.io.Serializable;

public class ApiPermission implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String name;
    private String code;
    private String url;
    private String method;
    private Long menuId;
    private String description;
    
    public ApiPermission() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public Long getMenuId() { return menuId; }
    public void setMenuId(Long menuId) { this.menuId = menuId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
