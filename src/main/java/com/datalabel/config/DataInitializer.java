package com.datalabel.config;

import com.datalabel.cache.LocalCache;
import com.datalabel.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private AdminConfig adminConfig;
    
    @Override
    public void run(String... args) throws Exception {
        LocalCache cache = LocalCache.getInstance();
        
        User admin = new User(cache.generateId(), adminConfig.getUsername(), adminConfig.getPassword(), "管理员");
        admin.setUserType(1);
        cache.put(admin.getId(), admin);
        
        Role role1 = new Role();
        role1.setId(cache.generateId());
        role1.setName("管理员");
        role1.setCode("ADMIN");
        role1.setDescription("系统管理员角色");
        cache.put(role1.getId(), role1);
        
        Role role2 = new Role();
        role2.setId(cache.generateId());
        role2.setName("普通用户");
        role2.setCode("USER");
        role2.setDescription("普通用户角色");
        cache.put(role2.getId(), role2);
        
        Organization org1 = new Organization();
        org1.setId(cache.generateId());
        org1.setName("总公司");
        org1.setCode("HQ");
        org1.setParentId(0L);
        org1.setLevel(1);
        cache.put(org1.getId(), org1);
        
        Organization org2 = new Organization();
        org2.setId(cache.generateId());
        org2.setName("技术部");
        org2.setCode("TECH");
        org2.setParentId(org1.getId());
        org2.setLevel(2);
        cache.put(org2.getId(), org2);
        
        Organization org3 = new Organization();
        org3.setId(cache.generateId());
        org3.setName("市场部");
        org3.setCode("MARKET");
        org3.setParentId(org1.getId());
        org3.setLevel(2);
        cache.put(org3.getId(), org3);
        
        Menu menu1 = new Menu();
        menu1.setId(cache.generateId());
        menu1.setName("系统管理");
        menu1.setCode("system");
        menu1.setPath("/system");
        menu1.setParentId(0L);
        menu1.setSort(1);
        menu1.setMenuType(1);
        menu1.setStatus(1);
        cache.put(menu1.getId(), menu1);
        
        Menu menu2 = new Menu();
        menu2.setId(cache.generateId());
        menu2.setName("用户管理");
        menu2.setCode("user_manage");
        menu2.setPath("/system/user");
        menu2.setParentId(menu1.getId());
        menu2.setSort(1);
        menu2.setMenuType(1);
        menu2.setStatus(1);
        cache.put(menu2.getId(), menu2);
        
        Menu menu3 = new Menu();
        menu3.setId(cache.generateId());
        menu3.setName("角色管理");
        menu3.setCode("role_manage");
        menu3.setPath("/system/role");
        menu3.setParentId(menu1.getId());
        menu3.setSort(2);
        menu3.setMenuType(1);
        menu3.setStatus(1);
        cache.put(menu3.getId(), menu3);
        
        Menu menu4 = new Menu();
        menu4.setId(cache.generateId());
        menu4.setName("菜单管理");
        menu4.setCode("menu_manage");
        menu4.setPath("/system/menu");
        menu4.setParentId(menu1.getId());
        menu4.setSort(3);
        menu4.setMenuType(1);
        menu4.setStatus(1);
        cache.put(menu4.getId(), menu4);
        
        Menu menu5 = new Menu();
        menu5.setId(cache.generateId());
        menu5.setName("组织机构");
        menu5.setCode("org_manage");
        menu5.setPath("/system/org");
        menu5.setParentId(menu1.getId());
        menu5.setSort(4);
        menu5.setMenuType(1);
        menu5.setStatus(1);
        cache.put(menu5.getId(), menu5);
        
        Menu menu6 = new Menu();
        menu6.setId(cache.generateId());
        menu6.setName("API权限");
        menu6.setCode("api_manage");
        menu6.setPath("/system/api");
        menu6.setParentId(menu1.getId());
        menu6.setSort(5);
        menu6.setMenuType(1);
        menu6.setStatus(1);
        cache.put(menu6.getId(), menu6);
        
        String[] userApiCodes = {"user:list", "user:view", "user:save", "user:update", "user:delete", "user:bindRole"};
        for (String code : userApiCodes) {
            ApiPermission api = new ApiPermission();
            api.setId(cache.generateId());
            api.setName("用户" + code.split(":")[1]);
            api.setCode(code);
            api.setUrl("/api/user/**");
            api.setMethod("ALL");
            api.setMenuId(menu2.getId());
            cache.put(api.getId(), api);
        }
        
        String[] roleApiCodes = {"role:list", "role:view", "role:save", "role:delete", 
                "role:menu", "role:bindMenu", "role:api", "role:bindApi", "role:org", "role:bindOrg"};
        for (String code : roleApiCodes) {
            ApiPermission api = new ApiPermission();
            api.setId(cache.generateId());
            api.setName("角色" + code.split(":")[1]);
            api.setCode(code);
            api.setUrl("/api/role/**");
            api.setMethod("ALL");
            api.setMenuId(menu3.getId());
            cache.put(api.getId(), api);
        }
        
        String[] menuApiCodes = {"menu:list", "menu:view", "menu:save", "menu:delete", "menu:role", "menu:bindRole"};
        for (String code : menuApiCodes) {
            ApiPermission api = new ApiPermission();
            api.setId(cache.generateId());
            api.setName("菜单" + code.split(":")[1]);
            api.setCode(code);
            api.setUrl("/api/menu/**");
            api.setMethod("ALL");
            api.setMenuId(menu4.getId());
            cache.put(api.getId(), api);
        }
        
        String[] apiPermCodes = {"api:list", "api:view", "api:save", "api:delete", "api:role", "api:bindRole"};
        for (String code : apiPermCodes) {
            ApiPermission api = new ApiPermission();
            api.setId(cache.generateId());
            api.setName("API" + code.split(":")[1]);
            api.setCode(code);
            api.setUrl("/api/permission/**");
            api.setMethod("ALL");
            api.setMenuId(menu6.getId());
            cache.put(api.getId(), api);
        }
        
        String[] orgApiCodes = {"org:list", "org:view", "org:save", "org:delete", "org:role"};
        for (String code : orgApiCodes) {
            ApiPermission api = new ApiPermission();
            api.setId(cache.generateId());
            api.setName("组织机构" + code.split(":")[1]);
            api.setCode(code);
            api.setUrl("/api/org/**");
            api.setMethod("ALL");
            api.setMenuId(menu5.getId());
            cache.put(api.getId(), api);
        }
        
        RoleMenu rm1 = new RoleMenu();
        rm1.setId(cache.generateId());
        rm1.setRoleId(role1.getId());
        rm1.setMenuId(menu1.getId());
        cache.put(rm1.getId(), rm1);
        
        RoleMenu rm2 = new RoleMenu();
        rm2.setId(cache.generateId());
        rm2.setRoleId(role1.getId());
        rm2.setMenuId(menu2.getId());
        cache.put(rm2.getId(), rm2);
        
        RoleMenu rm3 = new RoleMenu();
        rm3.setId(cache.generateId());
        rm3.setRoleId(role1.getId());
        rm3.setMenuId(menu3.getId());
        cache.put(rm3.getId(), rm3);
        
        RoleMenu rm4 = new RoleMenu();
        rm4.setId(cache.generateId());
        rm4.setRoleId(role1.getId());
        rm4.setMenuId(menu4.getId());
        cache.put(rm4.getId(), rm4);
        
        RoleMenu rm5 = new RoleMenu();
        rm5.setId(cache.generateId());
        rm5.setRoleId(role1.getId());
        rm5.setMenuId(menu5.getId());
        cache.put(rm5.getId(), rm5);
        
        RoleMenu rm6 = new RoleMenu();
        rm6.setId(cache.generateId());
        rm6.setRoleId(role1.getId());
        rm6.setMenuId(menu6.getId());
        cache.put(rm6.getId(), rm6);
        
        RoleOrganization ro1 = new RoleOrganization();
        ro1.setId(cache.generateId());
        ro1.setRoleId(role1.getId());
        ro1.setOrganizationId(org1.getId());
        cache.put(ro1.getId(), ro1);
        
        RoleOrganization ro2 = new RoleOrganization();
        ro2.setId(cache.generateId());
        ro2.setRoleId(role1.getId());
        ro2.setOrganizationId(org2.getId());
        cache.put(ro2.getId(), ro2);
        
        RoleOrganization ro3 = new RoleOrganization();
        ro3.setId(cache.generateId());
        ro3.setRoleId(role1.getId());
        ro3.setOrganizationId(org3.getId());
        cache.put(ro3.getId(), ro3);
        
        RoleOrganization ro4 = new RoleOrganization();
        ro4.setId(cache.generateId());
        ro4.setRoleId(role2.getId());
        ro4.setOrganizationId(org2.getId());
        cache.put(ro4.getId(), ro4);
        
        System.out.println("初始化数据完成，管理员账号: " + adminConfig.getUsername());
        System.out.println("已初始化菜单、API权限、角色菜单绑定、角色组织机构绑定");
    }
}
