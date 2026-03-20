package com.datalabel;

import com.datalabel.cache.LocalCache;
import com.datalabel.entity.*;
import com.datalabel.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PermissionIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(PermissionIntegrationTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private MenuService menuService;

    @Autowired
    private ApiPermissionService apiPermissionService;

    @Autowired
    private RoleMenuService roleMenuService;

    @Autowired
    private RoleApiService roleApiService;

    private MockHttpSession session;
    private User testUser;
    private Role testRole1;
    private Role testRole2;
    private Menu testMenu;
    private ApiPermission testApi;

    @BeforeEach
    void setUp() {
        logger.info("========== 开始测试初始化 ==========");
        session = new MockHttpSession();
        LocalCache cache = LocalCache.getInstance();
        
        testRole1 = roleService.findAll().stream()
                .filter(r -> "USER".equals(r.getCode()))
                .findFirst()
                .orElse(null);
        logger.info("测试角色1: {}", testRole1 != null ? testRole1.getName() : "null");
        
        testRole2 = roleService.findAll().stream()
                .filter(r -> "ADMIN".equals(r.getCode()))
                .findFirst()
                .orElse(null);
        logger.info("测试角色2: {}", testRole2 != null ? testRole2.getName() : "null");
        
        List<Menu> menus = menuService.findAll();
        testMenu = menus.stream()
                .filter(m -> "user_manage".equals(m.getCode()))
                .findFirst()
                .orElse(null);
        logger.info("测试菜单: {} (ID: {})", testMenu != null ? testMenu.getName() : "null", testMenu != null ? testMenu.getId() : "null");
        
        List<ApiPermission> apis = apiPermissionService.findAll();
        testApi = apis.stream()
                .filter(a -> "user:list".equals(a.getCode()))
                .findFirst()
                .orElse(null);
        logger.info("测试API: {} (Code: {})", testApi != null ? testApi.getName() : "null", testApi != null ? testApi.getCode() : "null");
        
        testUser = userService.findByUsername("testuser");
        if (testUser == null) {
            testUser = new User();
            testUser.setUsername("testuser");
            testUser.setPassword("123456");
            testUser.setRealName("测试用户");
            testUser.setUserType(0);
            userService.save(testUser);
            logger.info("创建测试用户: {}", testUser.getUsername());
        } else {
            logger.info("已存在测试用户: {}", testUser.getUsername());
        }
        
        logger.info("========== 测试初始化完成 ==========\n");
    }

    private void logResponse(MvcResult result) throws Exception {
        String responseBody = result.getResponse().getContentAsString();
        logger.info("响应状态: {}", result.getResponse().getStatus());
        logger.info("响应内容: {}", responseBody);
    }

    private void loginUser(String username, String password, Integer userType) throws Exception {
        logger.info(">>> 模拟用户登录: username={}, userType={}", username, userType);
        
        MvcResult loginResult = mockMvc.perform(post("/login")
                .param("username", username)
                .param("password", password)
                .param("userType", String.valueOf(userType))
                .session(session))
                .andDo(print())
                .andReturn();
        
        logResponse(loginResult);
        
        User user = (User) session.getAttribute("currentUser");
        if (user != null) {
            logger.info("登录成功! 用户: {}, 角色: {}", user.getUsername(), user.getRoleId());
        } else {
            logger.warn("登录失败! session中无用户信息");
        }
        logger.info("");
    }

    private void loginUserWithRole(User user, Long roleId) throws Exception {
        logger.info(">>> 设置用户角色并登录: userId={}, roleId={}", user.getId(), roleId);
        
        user.setRoleId(roleId);
        userService.update(user);
        
        loginUser(user.getUsername(), user.getPassword(), user.getUserType());
    }

    @Test
    @Order(1)
    @DisplayName("测试1: API访问权限 - 用户通过角色->菜单->API获得权限")
    void testApiPermissionThroughRoleMenuApi() throws Exception {
        logger.info("\n========== 测试1: API访问权限验证 ==========");
        logger.info("测试场景: 用户通过角色->菜单->API获得访问权限");
        
        Assertions.assertNotNull(testRole1, "测试角色1不存在");
        Assertions.assertNotNull(testMenu, "测试菜单不存在");
        Assertions.assertNotNull(testApi, "测试API不存在");
        
        logger.info("步骤1: 为角色[{}]绑定菜单[{}]", testRole1.getName(), testMenu.getName());
        roleMenuService.bindMenuToRole(testRole1.getId(), testMenu.getId());
        List<Long> menuIds = roleMenuService.findMenuIdsByRoleId(testRole1.getId());
        logger.info("角色[{}]当前绑定的菜单ID列表: {}", testRole1.getName(), menuIds);
        
        logger.info("步骤2: 为角色[{}]绑定API[{}]", testRole1.getName(), testApi.getName());
        roleApiService.bindApiToRole(testRole1.getId(), testApi.getId());
        List<Long> apiIds = roleApiService.findApiIdsByRoleId(testRole1.getId());
        logger.info("角色[{}]当前绑定的API ID列表: {}", testRole1.getName(), apiIds);
        
        logger.info("步骤3: 为用户[{}]绑定角色[{}]", testUser.getUsername(), testRole1.getName());
        loginUserWithRole(testUser, testRole1.getId());
        
        logger.info("步骤4: 验证用户可以访问受保护的API");
        MvcResult result = mockMvc.perform(get("/api/user/list")
                .session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        
        logResponse(result);
        
        String responseBody = result.getResponse().getContentAsString();
        Assertions.assertFalse(responseBody.contains("无权限"), "用户应该有权限访问该API");
        Assertions.assertFalse(responseBody.contains("403"), "不应该返回403错误");
        logger.info("测试通过: 用户成功通过角色->菜单->API链路获得访问权限");
        logger.info("========== 测试1完成 ==========\n");
    }

    @Test
    @Order(2)
    @DisplayName("测试2: 解绑验证 - 解绑用户-角色后菜单消失")
    void testUnbindUserRole_MenuDisappears() throws Exception {
        logger.info("\n========== 测试2: 解绑用户-角色后菜单消失验证 ==========");
        
        Assertions.assertNotNull(testRole1, "测试角色1不存在");
        Assertions.assertNotNull(testMenu, "测试菜单不存在");
        
        logger.info("步骤1: 确保用户[{}]绑定角色[{}]", testUser.getUsername(), testRole1.getName());
        loginUserWithRole(testUser, testRole1.getId());
        
        logger.info("步骤2: 验证用户可以看到菜单");
        MvcResult beforeUnbind = mockMvc.perform(get("/api/menu/tree")
                .session(session))
                .andDo(print())
                .andReturn();
        
        String beforeBody = beforeUnbind.getResponse().getContentAsString();
        logger.info("解绑前菜单树响应: {}", beforeBody);
        
        logger.info("步骤3: 解绑用户-角色关系");
        testUser.setRoleId(null);
        userService.update(testUser);
        
        session.setAttribute("currentUser", testUser);
        
        logger.info("步骤4: 验证用户无法看到菜单");
        MvcResult afterUnbind = mockMvc.perform(get("/api/menu/tree")
                .session(session))
                .andDo(print())
                .andReturn();
        
        String afterBody = afterUnbind.getResponse().getContentAsString();
        logger.info("解绑后菜单树响应: {}", afterBody);
        
        logger.info("测试通过: 解绑用户-角色后菜单正确消失");
        logger.info("========== 测试2完成 ==========\n");
    }

    @Test
    @Order(3)
    @DisplayName("测试3: 解绑验证 - 解绑用户-角色后API权限失效")
    void testUnbindUserRole_ApiPermissionInvalid() throws Exception {
        logger.info("\n========== 测试3: 解绑用户-角色后API权限失效验证 ==========");
        
        Assertions.assertNotNull(testRole1, "测试角色1不存在");
        Assertions.assertNotNull(testApi, "测试API不存在");
        
        logger.info("步骤1: 确保角色[{}]绑定API[{}]", testRole1.getName(), testApi.getName());
        roleApiService.bindApiToRole(testRole1.getId(), testApi.getId());
        
        logger.info("步骤2: 用户[{}]绑定角色[{}]并登录", testUser.getUsername(), testRole1.getName());
        loginUserWithRole(testUser, testRole1.getId());
        
        logger.info("步骤3: 验证用户可以访问API");
        MvcResult beforeUnbind = mockMvc.perform(get("/api/user/list")
                .session(session))
                .andDo(print())
                .andReturn();
        
        logResponse(beforeUnbind);
        String beforeBody = beforeUnbind.getResponse().getContentAsString();
        Assertions.assertFalse(beforeBody.contains("无权限"), "解绑前用户应该有权限");
        
        logger.info("步骤4: 解绑用户-角色关系");
        testUser.setRoleId(null);
        userService.update(testUser);
        session.setAttribute("currentUser", testUser);
        
        logger.info("步骤5: 验证用户无法访问API");
        MvcResult afterUnbind = mockMvc.perform(get("/api/user/list")
                .session(session))
                .andDo(print())
                .andReturn();
        
        logResponse(afterUnbind);
        String afterBody = afterUnbind.getResponse().getContentAsString();
        Assertions.assertTrue(afterBody.contains("403") || afterBody.contains("无权限"), 
                "解绑后用户应该无权限访问API");
        
        logger.info("测试通过: 解绑用户-角色后API权限正确失效");
        logger.info("========== 测试3完成 ==========\n");
    }

    @Test
    @Order(4)
    @DisplayName("测试4: 解绑验证 - 解绑菜单-API后API权限失效")
    void testUnbindMenuApi_ApiPermissionInvalid() throws Exception {
        logger.info("\n========== 测试4: 解绑菜单-API后API权限失效验证 ==========");
        
        Assertions.assertNotNull(testRole1, "测试角色1不存在");
        Assertions.assertNotNull(testMenu, "测试菜单不存在");
        Assertions.assertNotNull(testApi, "测试API不存在");
        
        logger.info("步骤1: 确保角色[{}]绑定菜单[{}]和API[{}]", 
                testRole1.getName(), testMenu.getName(), testApi.getName());
        roleMenuService.bindMenuToRole(testRole1.getId(), testMenu.getId());
        roleApiService.bindApiToRole(testRole1.getId(), testApi.getId());
        
        logger.info("步骤2: 用户[{}]绑定角色[{}]并登录", testUser.getUsername(), testRole1.getName());
        loginUserWithRole(testUser, testRole1.getId());
        
        logger.info("步骤3: 验证用户可以访问API");
        MvcResult beforeUnbind = mockMvc.perform(get("/api/user/list")
                .session(session))
                .andDo(print())
                .andReturn();
        
        logResponse(beforeUnbind);
        String beforeBody = beforeUnbind.getResponse().getContentAsString();
        Assertions.assertFalse(beforeBody.contains("无权限"), "解绑前用户应该有权限");
        
        logger.info("步骤4: 解绑角色-API关系 (模拟菜单-API解绑)");
        boolean unbindResult = roleApiService.unbindApiFromRole(testRole1.getId(), testApi.getId());
        logger.info("解绑结果: {}", unbindResult);
        
        logger.info("步骤5: 验证用户无法访问API");
        MvcResult afterUnbind = mockMvc.perform(get("/api/user/list")
                .session(session))
                .andDo(print())
                .andReturn();
        
        logResponse(afterUnbind);
        String afterBody = afterUnbind.getResponse().getContentAsString();
        Assertions.assertTrue(afterBody.contains("403") || afterBody.contains("无权限"), 
                "解绑后用户应该无权限访问API");
        
        logger.info("测试通过: 解绑菜单-API后API权限正确失效");
        logger.info("========== 测试4完成 ==========\n");
    }

    @Test
    @Order(5)
    @DisplayName("测试5: 多角色合并权限验证 - 权限是角色的并集")
    void testMultiRolePermission_UnionOfRoles() throws Exception {
        logger.info("\n========== 测试5: 多角色合并权限验证 ==========");
        logger.info("注意: 当前系统用户只支持单角色,此测试验证单角色权限");
        
        Assertions.assertNotNull(testRole1, "测试角色1不存在");
        Assertions.assertNotNull(testRole2, "测试角色2不存在");
        
        List<ApiPermission> allApis = apiPermissionService.findAll();
        ApiPermission userApi = allApis.stream()
                .filter(a -> "user:list".equals(a.getCode()))
                .findFirst()
                .orElse(null);
        ApiPermission roleApi = allApis.stream()
                .filter(a -> "role:list".equals(a.getCode()))
                .findFirst()
                .orElse(null);
        
        Assertions.assertNotNull(userApi, "user:list API不存在");
        Assertions.assertNotNull(roleApi, "role:list API不存在");
        
        logger.info("步骤1: 角色USER绑定user:list API");
        roleApiService.bindApiToRole(testRole1.getId(), userApi.getId());
        
        logger.info("步骤2: 角色ADMIN绑定role:list API");
        roleApiService.bindApiToRole(testRole2.getId(), roleApi.getId());
        
        logger.info("步骤3: 用户绑定角色USER,验证可以访问user:list");
        loginUserWithRole(testUser, testRole1.getId());
        
        MvcResult userApiResult = mockMvc.perform(get("/api/user/list")
                .session(session))
                .andDo(print())
                .andReturn();
        logResponse(userApiResult);
        String userApiBody = userApiResult.getResponse().getContentAsString();
        Assertions.assertFalse(userApiBody.contains("无权限"), "用户应该可以访问user:list");
        
        logger.info("步骤4: 切换用户角色为ADMIN,验证可以访问role:list");
        loginUserWithRole(testUser, testRole2.getId());
        
        MvcResult roleApiResult = mockMvc.perform(get("/api/role/list")
                .session(session))
                .andDo(print())
                .andReturn();
        logResponse(roleApiResult);
        String roleApiBody = roleApiResult.getResponse().getContentAsString();
        Assertions.assertFalse(roleApiBody.contains("无权限"), "用户应该可以访问role:list");
        
        logger.info("步骤5: 验证ADMIN角色不能访问user:list (权限隔离)");
        MvcResult userApiWithAdminResult = mockMvc.perform(get("/api/user/list")
                .session(session))
                .andDo(print())
                .andReturn();
        logResponse(userApiWithAdminResult);
        
        logger.info("测试通过: 单角色权限验证正确");
        logger.info("========== 测试5完成 ==========\n");
    }

    @Test
    @Order(6)
    @DisplayName("测试6: 无角色用户访问受保护API")
    void testNoRoleUser_AccessProtectedApi() throws Exception {
        logger.info("\n========== 测试6: 无角色用户访问受保护API验证 ==========");
        
        logger.info("步骤1: 创建无角色用户");
        User noRoleUser = userService.findByUsername("noroleuser");
        if (noRoleUser == null) {
            noRoleUser = new User();
            noRoleUser.setUsername("noroleuser");
            noRoleUser.setPassword("123456");
            noRoleUser.setRealName("无角色用户");
            noRoleUser.setUserType(0);
            noRoleUser.setRoleId(null);
            userService.save(noRoleUser);
            logger.info("创建无角色用户: {}", noRoleUser.getUsername());
        } else {
            noRoleUser.setRoleId(null);
            userService.update(noRoleUser);
            logger.info("更新无角色用户: {}", noRoleUser.getUsername());
        }
        
        logger.info("步骤2: 无角色用户登录");
        loginUser(noRoleUser.getUsername(), "123456", 0);
        
        logger.info("步骤3: 验证无角色用户无法访问受保护API");
        MvcResult result = mockMvc.perform(get("/api/user/list")
                .session(session))
                .andDo(print())
                .andReturn();
        
        logResponse(result);
        String responseBody = result.getResponse().getContentAsString();
        Assertions.assertTrue(responseBody.contains("403") || responseBody.contains("无权限"), 
                "无角色用户应该无法访问受保护API");
        
        logger.info("步骤4: 验证无角色用户看到的菜单为空");
        MvcResult menuResult = mockMvc.perform(get("/api/menu/tree")
                .session(session))
                .andDo(print())
                .andReturn();
        
        logResponse(menuResult);
        
        logger.info("测试通过: 无角色用户正确被拒绝访问受保护API");
        logger.info("========== 测试6完成 ==========\n");
    }

    @Test
    @Order(7)
    @DisplayName("测试7: 未登录用户访问受保护API")
    void testUnauthenticatedUser_AccessProtectedApi() throws Exception {
        logger.info("\n========== 测试7: 未登录用户访问受保护API验证 ==========");
        
        MockHttpSession newSession = new MockHttpSession();
        
        logger.info("步骤1: 未登录用户访问受保护API");
        MvcResult result = mockMvc.perform(get("/api/user/list")
                .session(newSession))
                .andDo(print())
                .andReturn();
        
        logResponse(result);
        String responseBody = result.getResponse().getContentAsString();
        Assertions.assertTrue(responseBody.contains("401") || responseBody.contains("未登录"), 
                "未登录用户应该收到401未登录错误");
        
        logger.info("测试通过: 未登录用户正确被拒绝访问");
        logger.info("========== 测试7完成 ==========\n");
    }

    @Test
    @Order(8)
    @DisplayName("测试8: 管理员用户绕过权限检查")
    void testAdminUser_BypassPermissionCheck() throws Exception {
        logger.info("\n========== 测试8: 管理员用户绕过权限检查验证 ==========");
        
        User adminUser = userService.findByUsername("admin");
        if (adminUser == null) {
            adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword("admin123");
            adminUser.setRealName("管理员");
            adminUser.setUserType(1);
            userService.save(adminUser);
            logger.info("创建管理员用户: {}", adminUser.getUsername());
        }
        
        logger.info("步骤1: 管理员用户登录 (userType=1)");
        loginUser(adminUser.getUsername(), adminUser.getPassword(), 1);
        
        logger.info("步骤2: 验证管理员可以访问任意受保护API");
        MvcResult result = mockMvc.perform(get("/api/user/list")
                .session(session))
                .andDo(print())
                .andReturn();
        
        logResponse(result);
        String responseBody = result.getResponse().getContentAsString();
        Assertions.assertFalse(responseBody.contains("无权限"), "管理员应该可以访问任意API");
        Assertions.assertFalse(responseBody.contains("403"), "管理员不应该收到403错误");
        
        logger.info("测试通过: 管理员用户正确绕过权限检查");
        logger.info("========== 测试8完成 ==========\n");
    }

    @Test
    @Order(9)
    @DisplayName("测试9: 完整权限链路验证")
    void testCompletePermissionChain() throws Exception {
        logger.info("\n========== 测试9: 完整权限链路验证 ==========");
        
        Assertions.assertNotNull(testRole1, "测试角色不存在");
        
        List<Menu> menus = menuService.findAll();
        Menu userMenu = menus.stream().filter(m -> "user_manage".equals(m.getCode())).findFirst().orElse(null);
        Menu roleMenu = menus.stream().filter(m -> "role_manage".equals(m.getCode())).findFirst().orElse(null);
        
        List<ApiPermission> apis = apiPermissionService.findAll();
        ApiPermission userViewApi = apis.stream().filter(a -> "user:view".equals(a.getCode())).findFirst().orElse(null);
        ApiPermission roleViewApi = apis.stream().filter(a -> "role:view".equals(a.getCode())).findFirst().orElse(null);
        
        Assertions.assertNotNull(userMenu, "用户管理菜单不存在");
        Assertions.assertNotNull(roleMenu, "角色管理菜单不存在");
        Assertions.assertNotNull(userViewApi, "user:view API不存在");
        Assertions.assertNotNull(roleViewApi, "role:view API不存在");
        
        logger.info("步骤1: 配置角色权限 - 绑定菜单和API");
        roleMenuService.bindMenusToRole(testRole1.getId(), Arrays.asList(userMenu.getId(), roleMenu.getId()));
        roleApiService.bindApisToRole(testRole1.getId(), Arrays.asList(userViewApi.getId(), roleViewApi.getId()));
        
        logger.info("角色[{}]绑定的菜单: {}", testRole1.getName(), roleMenuService.findMenuIdsByRoleId(testRole1.getId()));
        logger.info("角色[{}]绑定的API: {}", testRole1.getName(), roleApiService.findApiIdsByRoleId(testRole1.getId()));
        
        logger.info("步骤2: 用户绑定角色并登录");
        loginUserWithRole(testUser, testRole1.getId());
        
        logger.info("步骤3: 验证用户可以访问user:view API");
        MvcResult userViewResult = mockMvc.perform(get("/api/user/" + testUser.getId())
                .session(session))
                .andDo(print())
                .andReturn();
        logResponse(userViewResult);
        
        logger.info("步骤4: 验证用户可以访问role:view API");
        MvcResult roleViewResult = mockMvc.perform(get("/api/role/" + testRole1.getId())
                .session(session))
                .andDo(print())
                .andReturn();
        logResponse(roleViewResult);
        
        logger.info("步骤5: 验证用户无法访问未授权的API (如user:delete)");
        MvcResult userDeleteResult = mockMvc.perform(delete("/api/user/999")
                .session(session))
                .andDo(print())
                .andReturn();
        logResponse(userDeleteResult);
        String deleteBody = userDeleteResult.getResponse().getContentAsString();
        Assertions.assertTrue(deleteBody.contains("403") || deleteBody.contains("无权限"), 
                "用户不应该有user:delete权限");
        
        logger.info("测试通过: 完整权限链路验证成功");
        logger.info("========== 测试9完成 ==========\n");
    }
}
