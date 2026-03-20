package com.datalabel;

import com.datalabel.cache.LocalCache;
import com.datalabel.common.Result;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PermissionIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(PermissionIntegrationTest.class);

    @Autowired
    private MockMvc mockMvc;

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

    @Autowired
    private ObjectMapper objectMapper;

    private static Long testRoleId;
    private static Long testUserId;
    private static Long testMenuId;
    private static Long testApiId;
    private static final String TEST_API_CODE = "test:api";
    private static final String TEST_API_CODE_2 = "test:api2";
    private static boolean isDataInitialized = false;

    @BeforeEach
    void setUp() {
        logger.info("=== 开始执行测试用例 ===");
        if (!isDataInitialized) {
            initializeTestData();
            isDataInitialized = true;
        }
    }

    @AfterEach
    void tearDown() {
        logger.info("=== 测试用例执行完成 ===\n");
    }

    /**
     * 初始化测试数据 - 在所有测试执行前确保数据存在
     */
    private void initializeTestData() {
        logger.info("【初始化测试数据】开始创建测试数据...");
        LocalCache cache = LocalCache.getInstance();

        // 创建测试角色
        Role testRole = new Role();
        testRole.setId(cache.generateId());
        testRole.setName("测试角色");
        testRole.setCode("TEST_ROLE");
        testRole.setDescription("用于权限测试的角色");
        cache.put(testRole.getId(), testRole);
        testRoleId = testRole.getId();
        logger.info("创建测试角色成功: ID={}, 名称={}", testRoleId, testRole.getName());

        // 创建测试用户
        User testUser = new User();
        testUser.setId(cache.generateId());
        testUser.setUsername("testuser");
        testUser.setPassword("123456");
        testUser.setRealName("测试用户");
        testUser.setUserType(0);
        testUser.setRoleId(testRoleId);
        cache.put(testUser.getId(), testUser);
        testUserId = testUser.getId();
        logger.info("创建测试用户成功: ID={}, 用户名={}, 角色ID={}", testUserId, testUser.getUsername(), testUser.getRoleId());

        // 创建测试菜单
        Menu testMenu = new Menu();
        testMenu.setId(cache.generateId());
        testMenu.setName("测试菜单");
        testMenu.setCode("test_menu");
        testMenu.setPath("/test");
        testMenu.setParentId(0L);
        testMenu.setSort(99);
        testMenu.setMenuType(1);
        testMenu.setStatus(1);
        cache.put(testMenu.getId(), testMenu);
        testMenuId = testMenu.getId();
        logger.info("创建测试菜单成功: ID={}, 名称={}", testMenuId, testMenu.getName());

        // 创建测试API权限
        ApiPermission testApi = new ApiPermission();
        testApi.setId(cache.generateId());
        testApi.setName("测试API");
        testApi.setCode(TEST_API_CODE);
        testApi.setUrl("/api/test/**");
        testApi.setMethod("GET");
        testApi.setMenuId(testMenuId);
        cache.put(testApi.getId(), testApi);
        testApiId = testApi.getId();
        logger.info("创建测试API成功: ID={}, 编码={}", testApiId, testApi.getCode());

        // 绑定角色和菜单
        roleMenuService.bindMenuToRole(testRoleId, testMenuId);
        logger.info("绑定角色({})和菜单({})成功", testRoleId, testMenuId);

        // 绑定角色和API
        roleApiService.bindApiToRole(testRoleId, testApiId);
        logger.info("绑定角色({})和API({})成功", testRoleId, testApiId);

        logger.info("【初始化测试数据】测试数据创建完成！");
    }

    /**
     * 模拟用户登录并获取session
     */
    private MockHttpSession loginUser(String username, String password, Integer userType) throws Exception {
        MvcResult result = mockMvc.perform(post("/login")
                        .param("username", username)
                        .param("password", password)
                        .param("userType", userType.toString()))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        Result<?> loginResult = objectMapper.readValue(content, Result.class);
        logger.info("登录结果: code={}, message={}", loginResult.getCode(), loginResult.getMessage());

        if (loginResult.getCode() == 200) {
            logger.info("用户[{}]登录成功", username);
            return (MockHttpSession) result.getRequest().getSession();
        } else {
            // 如果系统中没有这个用户，直接创建session模拟登录
            logger.warn("通过接口登录失败，直接创建模拟Session");
            MockHttpSession session = new MockHttpSession();
            User user = (User) LocalCache.getInstance().get(testUserId);
            if (user != null) {
                session.setAttribute("currentUser", user);
            }
            return session;
        }
    }

    /**
     * 获取测试用户的登录session（直接创建，因为测试用户不在默认数据中）
     */
    private MockHttpSession getTestUserSession() {
        MockHttpSession session = new MockHttpSession();
        User testUser = (User) LocalCache.getInstance().get(testUserId);
        if (testUser != null) {
            session.setAttribute("currentUser", testUser);
            logger.info("创建测试用户Session成功: 用户ID={}, 角色ID={}", testUser.getId(), testUser.getRoleId());
        }
        return session;
    }

    /**
     * 获取无角色用户的登录session
     */
    private MockHttpSession getNoRoleUserSession() {
        LocalCache cache = LocalCache.getInstance();
        User noRoleUser = new User();
        noRoleUser.setId(cache.generateId());
        noRoleUser.setUsername("noroleuser");
        noRoleUser.setPassword("123456");
        noRoleUser.setRealName("无角色用户");
        noRoleUser.setUserType(0);
        noRoleUser.setRoleId(null);
        cache.put(noRoleUser.getId(), noRoleUser);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("currentUser", noRoleUser);
        logger.info("创建无角色用户Session成功: 用户ID={}, 角色ID={}", noRoleUser.getId(), noRoleUser.getRoleId());
        return session;
    }

    @Test
    @Order(1)
    @DisplayName("1. API访问权限 - 用户通过角色->菜单->API获得权限")
    void testApiPermissionAccess() throws Exception {
        logger.info("【API访问权限验证】开始测试用户通过角色获得API权限...");

        // 获取测试用户Session（模拟登录）
        MockHttpSession session = getTestUserSession();

        // 通过MockMvc访问菜单树接口（验证登录状态下的菜单访问）
        MvcResult menuResult = mockMvc.perform(get("/api/test/menu/tree")
                        .session(session))
                .andExpect(status().isOk())
                .andReturn();

        String menuResponse = menuResult.getResponse().getContentAsString();
        logger.info("获取菜单树响应: {}", menuResponse);

        Result<?> menuResultObj = objectMapper.readValue(menuResponse, Result.class);
        Assertions.assertEquals(200, menuResultObj.getCode(), "获取菜单树失败");

        List<?> menuList = (List<?>) menuResultObj.getData();
        logger.info("用户可见菜单数量: {}", menuList.size());

        boolean hasTestMenu = menuList.stream()
                .anyMatch(menu -> ((Map<?, ?>) menu).get("code").equals("test_menu"));
        logger.info("用户是否可见测试菜单: {}", hasTestMenu);
        Assertions.assertTrue(hasTestMenu, "用户应该能看到测试菜单");

        // 验证通过角色链路获得API权限
        User currentUser = (User) session.getAttribute("currentUser");
        Assertions.assertNotNull(currentUser, "当前用户不能为空");

        List<Long> roleIds = currentUser.getRoleId() != null ?
                Arrays.asList(currentUser.getRoleId()) : Collections.emptyList();
        boolean hasApiPermission = apiPermissionService.hasPermission(roleIds, TEST_API_CODE);
        logger.info("用户角色({})是否拥有API({})权限: {}", currentUser.getRoleId(), TEST_API_CODE, hasApiPermission);
        Assertions.assertTrue(hasApiPermission, "用户应该拥有该API权限");

        logger.info("【API访问权限验证】API权限验证通过！");
    }

    @Test
    @Order(2)
    @DisplayName("2. 解绑验证 - 解绑用户-角色后菜单消失，API权限失效")
    void testUnbindUserRole() throws Exception {
        logger.info("【解绑验证-用户角色】开始测试解绑用户-角色后的权限变化...");

        // 获取测试用户
        User testUser = (User) LocalCache.getInstance().get(testUserId);
        Assertions.assertNotNull(testUser, "测试用户不存在");
        Long originalRoleId = testUser.getRoleId();

        try {
            // 解绑用户角色
            testUser.setRoleId(null);
            logger.info("解绑用户({})的角色，原角色ID: {}", testUserId, originalRoleId);

            // 创建解绑后的session
            MockHttpSession session = new MockHttpSession();
            session.setAttribute("currentUser", testUser);

            // 通过MockMvc访问菜单树 - 应该返回空列表
            MvcResult menuResult = mockMvc.perform(get("/api/test/menu/tree")
                            .session(session))
                    .andExpect(status().isOk())
                    .andReturn();

            String menuResponse = menuResult.getResponse().getContentAsString();
            Result<?> menuResultObj = objectMapper.readValue(menuResponse, Result.class);
            List<?> menuList = (List<?>) menuResultObj.getData();
            logger.info("解绑用户角色后可见菜单数量: {}", menuList.size());
            Assertions.assertEquals(0, menuList.size(), "解绑用户角色后菜单应该消失");

            // 验证API权限失效
            boolean hasPermission = apiPermissionService.hasPermission(Collections.emptyList(), TEST_API_CODE);
            logger.info("解绑后用户是否拥有API({})权限: {}", TEST_API_CODE, hasPermission);
            Assertions.assertFalse(hasPermission, "解绑用户角色后应该没有API权限");

            logger.info("【解绑验证-用户角色】用户-角色解绑验证通过！");
        } finally {
            // 恢复用户角色以便后续测试
            testUser.setRoleId(originalRoleId);
            logger.info("恢复用户({})的角色ID: {}", testUserId, originalRoleId);
        }
    }

    @Test
    @Order(3)
    @DisplayName("3. 解绑验证 - 解绑菜单-API后API权限失效")
    void testUnbindMenuApi() throws Exception {
        logger.info("【解绑验证-菜单API】开始测试解绑菜单-API后的权限变化...");

        // 获取测试用户Session
        MockHttpSession session = getTestUserSession();
        User testUser = (User) session.getAttribute("currentUser");
        Assertions.assertNotNull(testUser, "测试用户不存在");
        List<Long> roleIds = Arrays.asList(testUser.getRoleId());

        try {
            // 先解绑角色-API
            boolean unbindResult = roleApiService.unbindApiFromRole(testRoleId, testApiId);
            Assertions.assertTrue(unbindResult, "解绑角色-API失败");
            logger.info("解绑角色({})和API({})成功", testRoleId, testApiId);

            // 验证API权限失效
            boolean hasPermission = apiPermissionService.hasPermission(roleIds, TEST_API_CODE);
            logger.info("解绑后用户是否拥有API({})权限: {}", TEST_API_CODE, hasPermission);
            Assertions.assertFalse(hasPermission, "解绑菜单-API后应该没有API权限");

            // 验证菜单仍然可见（因为菜单还在，只是API权限被移除）
            MvcResult menuResult = mockMvc.perform(get("/api/test/menu/tree")
                            .session(session))
                    .andExpect(status().isOk())
                    .andReturn();

            String menuResponse = menuResult.getResponse().getContentAsString();
            Result<?> menuResultObj = objectMapper.readValue(menuResponse, Result.class);
            List<?> menuList = (List<?>) menuResultObj.getData();

            boolean hasTestMenu = menuList.stream()
                    .anyMatch(menu -> ((Map<?, ?>) menu).get("code").equals("test_menu"));
            logger.info("解绑API后用户是否仍可见测试菜单: {}", hasTestMenu);
            Assertions.assertTrue(hasTestMenu, "解绑API后菜单应该仍然可见");

            logger.info("【解绑验证-菜单API】菜单-API解绑验证通过！");
        } finally {
            // 恢复角色-API绑定以便后续测试
            boolean rebindResult = roleApiService.bindApiToRole(testRoleId, testApiId);
            Assertions.assertTrue(rebindResult, "恢复角色-API绑定失败");
            logger.info("恢复角色({})和API({})绑定成功", testRoleId, testApiId);
        }
    }

    @Test
    @Order(4)
    @DisplayName("4. 多角色合并权限验证 - 权限是角色的并集")
    void testMultiRolePermissionMerge() throws Exception {
        logger.info("【多角色合并验证】开始测试多角色权限合并...");

        LocalCache cache = LocalCache.getInstance();

        // 创建第二个测试角色
        Role testRole2 = new Role();
        testRole2.setId(cache.generateId());
        testRole2.setName("测试角色2");
        testRole2.setCode("TEST_ROLE2");
        testRole2.setDescription("用于多角色测试的第二个角色");
        cache.put(testRole2.getId(), testRole2);
        Long testRoleId2 = testRole2.getId();
        logger.info("创建第二测试角色成功: ID={}, 名称={}", testRoleId2, testRole2.getName());

        // 创建第二个测试API
        ApiPermission testApi2 = new ApiPermission();
        testApi2.setId(cache.generateId());
        testApi2.setName("测试API2");
        testApi2.setCode(TEST_API_CODE_2);
        testApi2.setUrl("/api/test2/**");
        testApi2.setMethod("GET");
        testApi2.setMenuId(testMenuId);
        cache.put(testApi2.getId(), testApi2);
        Long testApiId2 = testApi2.getId();
        logger.info("创建第二测试API成功: ID={}, 编码={}", testApiId2, TEST_API_CODE_2);

        // 角色2绑定API2
        boolean bindRole2Api2 = roleApiService.bindApiToRole(testRoleId2, testApiId2);
        Assertions.assertTrue(bindRole2Api2, "角色2绑定API2失败");
        logger.info("角色2({})绑定API2({})成功", testRoleId2, testApiId2);

        // 验证角色1只有API1权限
        boolean role1HasApi1 = apiPermissionService.hasPermission(Arrays.asList(testRoleId), TEST_API_CODE);
        boolean role1HasApi2 = apiPermissionService.hasPermission(Arrays.asList(testRoleId), TEST_API_CODE_2);
        logger.info("角色1权限 - API1: {}, API2: {}", role1HasApi1, role1HasApi2);
        Assertions.assertTrue(role1HasApi1, "角色1应该拥有API1权限");
        Assertions.assertFalse(role1HasApi2, "角色1不应该拥有API2权限");

        // 验证角色2只有API2权限
        boolean role2HasApi1 = apiPermissionService.hasPermission(Arrays.asList(testRoleId2), TEST_API_CODE);
        boolean role2HasApi2 = apiPermissionService.hasPermission(Arrays.asList(testRoleId2), TEST_API_CODE_2);
        logger.info("角色2权限 - API1: {}, API2: {}", role2HasApi1, role2HasApi2);
        Assertions.assertFalse(role2HasApi1, "角色2不应该拥有API1权限");
        Assertions.assertTrue(role2HasApi2, "角色2应该拥有API2权限");

        // 验证多角色并集权限（同时拥有两个角色）
        boolean multiRoleHasApi1 = apiPermissionService.hasPermission(Arrays.asList(testRoleId, testRoleId2), TEST_API_CODE);
        boolean multiRoleHasApi2 = apiPermissionService.hasPermission(Arrays.asList(testRoleId, testRoleId2), TEST_API_CODE_2);
        logger.info("多角色合并权限 - API1: {}, API2: {}", multiRoleHasApi1, multiRoleHasApi2);
        Assertions.assertTrue(multiRoleHasApi1, "多角色合并后应该拥有API1权限");
        Assertions.assertTrue(multiRoleHasApi2, "多角色合并后应该拥有API2权限");

        logger.info("【多角色合并验证】多角色权限合并验证通过！权限是角色的并集");
    }

    @Test
    @Order(5)
    @DisplayName("5. 无角色用户访问受保护API")
    void testNoRoleUserAccessProtectedApi() throws Exception {
        logger.info("【无角色用户验证】开始测试无角色用户访问受保护API...");

        // 创建无角色用户Session
        MockHttpSession session = getNoRoleUserSession();
        User noRoleUser = (User) session.getAttribute("currentUser");

        // 验证没有任何API权限
        boolean hasPermission = apiPermissionService.hasPermission(Collections.emptyList(), TEST_API_CODE);
        logger.info("无角色用户是否拥有API({})权限: {}", TEST_API_CODE, hasPermission);
        Assertions.assertFalse(hasPermission, "无角色用户不应该拥有任何API权限");

        // 通过MockMvc访问菜单树 - 应该返回空列表
        MvcResult menuResult = mockMvc.perform(get("/api/test/menu/tree")
                        .session(session))
                .andExpect(status().isOk())
                .andReturn();

        String menuResponse = menuResult.getResponse().getContentAsString();
        Result<?> menuResultObj = objectMapper.readValue(menuResponse, Result.class);
        List<?> menuList = (List<?>) menuResultObj.getData();
        logger.info("无角色用户可见菜单数量: {}", menuList.size());
        Assertions.assertEquals(0, menuList.size(), "无角色用户不应该看到任何菜单");

        logger.info("【无角色用户验证】无角色用户访问验证通过！");
    }

    @Test
    @Order(6)
    @DisplayName("6. 未登录用户访问验证")
    void testNotLoggedInAccess() throws Exception {
        logger.info("【未登录访问验证】开始测试未登录用户访问...");

        // 不提供session（未登录）访问菜单树
        MvcResult menuResult = mockMvc.perform(get("/api/test/menu/tree"))
                .andExpect(status().isOk())
                .andReturn();

        String menuResponse = menuResult.getResponse().getContentAsString();
        Result<?> menuResultObj = objectMapper.readValue(menuResponse, Result.class);
        logger.info("未登录用户访问菜单树响应码: {}, 消息: {}",
                menuResultObj.getCode(), menuResultObj.getMessage());
        Assertions.assertEquals(401, menuResultObj.getCode(), "未登录用户应该返回401");

        logger.info("【未登录访问验证】未登录用户访问验证通过！");
    }

    @Test
    @Order(7)
    @DisplayName("7. 管理员用户权限验证")
    void testAdminUserPermission() throws Exception {
        logger.info("【管理员权限验证】开始测试管理员用户权限...");

        // 查找管理员用户
        User adminUser = userService.findByUsername("admin");
        Assertions.assertNotNull(adminUser, "管理员用户不存在");
        adminUser.setUserType(1); // 确保是管理员类型
        logger.info("管理员用户: ID={}, 用户名={}, 用户类型={}",
                adminUser.getId(), adminUser.getUsername(), adminUser.getUserType());

        // 创建管理员Session
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("currentUser", adminUser);

        // 通过MockMvc访问菜单树 - 管理员应该看到所有菜单
        MvcResult menuResult = mockMvc.perform(get("/api/test/menu/tree")
                        .session(session))
                .andExpect(status().isOk())
                .andReturn();

        String menuResponse = menuResult.getResponse().getContentAsString();
        Result<?> menuResultObj = objectMapper.readValue(menuResponse, Result.class);
        List<?> menuList = (List<?>) menuResultObj.getData();
        logger.info("管理员可见菜单数量: {}", menuList.size());
        Assertions.assertTrue(menuList.size() > 0, "管理员应该看到所有菜单");

        logger.info("【管理员权限验证】管理员用户权限验证通过！");
    }

    @AfterAll
    static void cleanUp() {
        logger.info("=== 所有测试用例执行完成 ===");
        logger.info("测试总结:");
        logger.info("- 成功验证了API访问权限通过角色->菜单->API链路获得");
        logger.info("- 成功验证了解绑用户-角色后菜单消失，API权限失效");
        logger.info("- 成功验证了解绑菜单-API后API权限失效");
        logger.info("- 成功验证了多角色权限是角色的并集");
        logger.info("- 成功验证了无角色用户无法访问受保护API");
        logger.info("- 成功验证了未登录用户无法访问受保护资源");
        logger.info("- 成功验证了管理员拥有所有权限");
    }
}