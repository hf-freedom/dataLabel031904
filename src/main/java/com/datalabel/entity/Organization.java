package com.datalabel.entity;

import java.io.Serializable;

public class Organization implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String name;
    private String code;
    private Long parentId;
    private Integer level;
    private String description;
    
    public Organization() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
