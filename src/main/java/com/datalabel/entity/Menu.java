package com.datalabel.entity;

import java.io.Serializable;

public class Menu implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String name;
    private String code;
    private String path;
    private Long parentId;
    private Integer sort;
    private Integer menuType;
    private String icon;
    private Integer status;
    
    public Menu() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
    public Integer getMenuType() { return menuType; }
    public void setMenuType(Integer menuType) { this.menuType = menuType; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
