package com.datalabel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PermissionWebLayerTest {

    private static final Logger logger = LoggerFactory.getLogger(PermissionWebLayerTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private MockHttpSession adminSession;
    private Long testRoleId;
    private Long testMenuId;
    private Long testApiId;
    private Long testUserId;
    private String testApiCode;

    @BeforeAll
    public void setup() {
        logger.info("========================================");
        logger.info("开始执行Web层权限集成测试");
        logger.info("========================================");
    }

    @BeforeEach
    public void init() throws Exception {
        if (adminSession == null) {
            adminSession = loginAsAdmin();
        }
    }

    private int getResponseCode(String response) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(response);
        return jsonNode.get("code").asInt();
    }

    private Long getResponseDataId(String response) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(response);
        JsonNode data = jsonNode.get("data");
        if (data != null && data.has("id")) {
            return data.get("id").asLong();
        }
        return null;
    }

    private MockHttpSession loginAsAdmin() throws Exception {
        logger.info("【登录】管理员登录...");
        MockHttpSession session = new MockHttpSession();
        MvcResult result = mockMvc.perform(post("/login")
                .param("username", "admin")
                .param("password", "admin123")
                .param("userType", "1")
                .session(session))
                .andExpect(status().isOk())
                .andReturn();
        String response = result.getResponse().getContentAsString();
        int code = getResponseCode(response);
        logger.info("【登录】管理员登录响应: code={}", code);
        Assertions.assertEquals(200, code, "管理员登录应该成功");
        return session;
    }

    private MockHttpSession loginAsUser(String username, String password) throws Exception {
        logger.info("【登录】用户 {} 登录...", username);
        MockHttpSession session = new MockHttpSession();
        MvcResult result = mockMvc.perform(post("/login")
                .param("username", username)
                .param("password", password)
                .param("userType", "0")
                .session(session))
                .andExpect(status().isOk())
                .andReturn();
        String response = result.getResponse().getContentAsString();
        int code = getResponseCode(response);
        logger.info("【登录】用户 {} 登录响应: code={}", username, code);
        return session;
    }

    @Test
    @Order(1)
    @DisplayName("测试1: API访问权限 - 用户通过角色->菜单->API获得权限")
    public void testApiPermissionThroughRoleMenuApi() throws Exception {
        logger.info("========================================");
        logger.info("【测试1】API访问权限 - 角色->菜单->API链路验证");
        logger.info("========================================");

        String timestamp = String.valueOf(System.currentTimeMillis());

        logger.info("【步骤1】创建测试角色...");
        String roleJson = "{\"name\":\"测试角色-API权限-" + timestamp + "\",\"code\":\"TEST_ROLE_API_" + timestamp + "\",\"description\":\"用于测试API权限的角色\"}";
        MvcResult roleResult = mockMvc.perform(post("/api/role/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(roleJson)
                .session(adminSession))
                .andExpect(status().isOk())
                .andReturn();
        String roleResponse = roleResult.getResponse().getContentAsString();
        logger.info("【步骤1】角色创建响应: {}", roleResponse);
        Assertions.assertEquals(200, getResponseCode(roleResponse), "角色创建应该成功");

        MvcResult roleListResult = mockMvc.perform(get("/api/role/list")
                .session(adminSession))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode roles = objectMapper.readTree(roleListResult.getResponse().getContentAsString()).get("data");
        for (JsonNode role : roles) {
            if (role.get("code").asText().equals("TEST_ROLE_API_" + timestamp)) {
                testRoleId = role.get("id").asLong();
                break;
            }
        }
        logger.info("【步骤1】角色创建成功 - ID: {}", testRoleId);
        Assertions.assertNotNull(testRoleId, "角色ID不应为空");

        logger.info("【步骤2】创建测试菜单...");
        String menuJson = "{\"name\":\"测试菜单-API权限-" + timestamp + "\",\"code\":\"test_menu_api_" + timestamp + "\",\"path\":\"/test/api\",\"parentId\":0,\"sort\":1,\"menuType\":1,\"status\":1}";
        MvcResult menuResult = mockMvc.perform(post("/api/menu/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(menuJson)
                .session(adminSession))
                .andExpect(status().isOk())
                .andReturn();
        String menuResponse = menuResult.getResponse().getContentAsString();
        logger.info("【步骤2】菜单创建响应: {}", menuResponse);
        Assertions.assertEquals(200, getResponseCode(menuResponse), "菜单创建应该成功");

        MvcResult menuListResult = mockMvc.perform(get("/api/menu/list")
                .session(adminSession))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode menus = objectMapper.readTree(menuListResult.getResponse().getContentAsString()).get("data");
        for (JsonNode menu : menus) {
            if (menu.get("code").asText().equals("test_menu_api_" + timestamp)) {
                testMenuId = menu.get("id").asLong();
                break;
            }
        }
        logger.info("【步骤2】菜单创建成功 - ID: {}", testMenuId);
        Assertions.assertNotNull(testMenuId, "菜单ID不应为空");

        logger.info("【步骤3】创建测试API权限（使用user:list编码，与/api/user/list接口匹配）...");
        testApiCode = "user:list";
        String apiJson = "{\"name\":\"用户列表权限-测试-" + timestamp + "\",\"code\":\"user:list\",\"url\":\"/api/user/list\",\"method\":\"GET\",\"menuId\":" + testMenuId + "}";
        MvcResult apiResult = mockMvc.perform(post("/api/permission/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(apiJson)
                .session(adminSession))
                .andExpect(status().isOk())
                .andReturn();
        String apiResponse = apiResult.getResponse().getContentAsString();
        logger.info("【步骤3】API权限创建响应: {}", apiResponse);
        Assertions.assertEquals(200, getResponseCode(apiResponse), "API权限创建应该成功");

        MvcResult apiListResult = mockMvc.perform(get("/api/permission/list")
                .session(adminSession))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode apis = objectMapper.readTree(apiListResult.getResponse().getContentAsString()).get("data");
        for (JsonNode api : apis) {
            if (api.get("code").asText().equals(testApiCode)) {
                testApiId = api.get("id").asLong();
                break;
            }
        }
        logger.info("【步骤3】API权限创建成功 - ID: {}, 编码: {}", testApiId, testApiCode);
        Assertions.assertNotNull(testApiId, "API ID不应为空");

        logger.info("【步骤4】绑定角色与菜单...");
        MvcResult bindMenuResult = mockMvc.perform(post("/api/role/" + testRoleId + "/menus")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[" + testMenuId + "]")
                .session(adminSession))
                .andExpect(status().isOk())
                .andReturn();
        String bindMenuResponse = bindMenuResult.getResponse().getContentAsString();
        logger.info("【步骤4】角色-菜单绑定响应: {}", bindMenuResponse);
        Assertions.assertEquals(200, getResponseCode(bindMenuResponse), "角色-菜单绑定应该成功");

        logger.info("【步骤5】绑定角色与API...");
        MvcResult bindApiResult = mockMvc.perform(post("/api/role/" + testRoleId + "/apis")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[" + testApiId + "]")
                .session(adminSession))
                .andExpect(status().isOk())
                .andReturn();
        String bindApiResponse = bindApiResult.getResponse().getContentAsString();
        logger.info("【步骤5】角色-API绑定响应: {}", bindApiResponse);
        Assertions.assertEquals(200, getResponseCode(bindApiResponse), "角色-API绑定应该成功");

        logger.info("【步骤6】创建测试用户...");
        String userJson = "{\"username\":\"testuser_api_" + timestamp + "\",\"password\":\"password123\",\"realName\":\"测试用户-API权限\",\"userType\":0,\"roleId\":" + testRoleId + "}";
        MvcResult userResult = mockMvc.perform(post("/api/user/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson)
                .session(adminSession))
                .andExpect(status().isOk())
                .andReturn();
        String userResponse = userResult.getResponse().getContentAsString();
        logger.info("【步骤6】用户创建响应: {}", userResponse);
        Assertions.assertEquals(200, getResponseCode(userResponse), "用户创建应该成功");

        MvcResult userListResult = mockMvc.perform(get("/api/user/list")
                .session(adminSession))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode users = objectMapper.readTree(userListResult.getResponse().getContentAsString()).get("data");
        for (JsonNode user : users) {
            if (user.get("username").asText().equals("testuser_api_" + timestamp)) {
                testUserId = user.get("id").asLong();
                break;
            }
        }
        logger.info("【步骤6】用户创建成功 - ID: {}, 绑定角色ID: {}", testUserId, testRoleId);
        Assertions.assertNotNull(testUserId, "用户ID不应为空");

        logger.info("【步骤7】验证用户登录后可以访问受保护API（/api/user/list 需要 user:list 权限）...");
        MockHttpSession userSession = loginAsUser("testuser_api_" + timestamp, "password123");

        MvcResult accessResult = mockMvc.perform(get("/api/user/list")
                .session(userSession))
                .andExpect(status().isOk())
                .andReturn();
        String accessResponse = accessResult.getResponse().getContentAsString();
        int accessCode = getResponseCode(accessResponse);
        logger.info("【步骤7】用户访问API响应: code={}", accessCode);
        Assertions.assertEquals(200, accessCode, "有权限的用户应该能访问API");

        logger.info("【步骤8】验证用户可以查看菜单树...");
        MvcResult menuTreeResult = mockMvc.perform(get("/api/menu/tree")
                .session(userSession))
                .andExpect(status().isOk())
                .andReturn();
        String menuTreeResponse = menuTreeResult.getResponse().getContentAsString();
        int menuTreeCode = getResponseCode(menuTreeResponse);
        logger.info("【步骤8】用户查看菜单树响应: code={}", menuTreeCode);
        Assertions.assertEquals(200, menuTreeCode, "有角色的用户应该能看到菜单");

        logger.info("【测试1】通过 - API访问权限链路验证成功");
    }

    @Test
    @Order(2)
    @DisplayName("测试2: 解绑验证 - 解绑用户-角色后菜单消失且API权限失效")
    public void testUnbindUserRolePermissionInvalid() throws Exception {
        logger.info("========================================");
        logger.info("【测试2】解绑验证 - 用户-角色解绑后权限失效");
        logger.info("========================================");

        String timestamp = String.valueOf(System.currentTimeMillis());

        logger.info("【步骤1】创建测试角色...");
        String roleJson = "{\"name\":\"测试角色-解绑-" + timestamp + "\",\"code\":\"TEST_UNBIND_" + timestamp + "\",\"description\":\"用于测试解绑的角色\"}";
        mockMvc.perform(post("/api/role/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(roleJson)
                .session(adminSession))
                .andExpect(status().isOk());

        MvcResult roleListResult = mockMvc.perform(get("/api/role/list").session(adminSession)).andReturn();
        JsonNode roles = objectMapper.readTree(roleListResult.getResponse().getContentAsString()).get("data");
        Long unbindRoleId = null;
        for (JsonNode role : roles) {
            if (role.get("code").asText().equals("TEST_UNBIND_" + timestamp)) {
                unbindRoleId = role.get("id").asLong();
                break;
            }
        }
        Assertions.assertNotNull(unbindRoleId, "角色ID不应为空");
        logger.info("【步骤1】角色创建成功 - ID: {}", unbindRoleId);

        logger.info("【步骤2】创建测试菜单...");
        String menuJson = "{\"name\":\"测试菜单-解绑-" + timestamp + "\",\"code\":\"test_menu_unbind_" + timestamp + "\",\"path\":\"/test/unbind\",\"parentId\":0,\"sort\":1,\"menuType\":1,\"status\":1}";
        mockMvc.perform(post("/api/menu/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(menuJson)
                .session(adminSession))
                .andExpect(status().isOk());

        MvcResult menuListResult = mockMvc.perform(get("/api/menu/list").session(adminSession)).andReturn();
        JsonNode menus = objectMapper.readTree(menuListResult.getResponse().getContentAsString()).get("data");
        Long unbindMenuId = null;
        for (JsonNode menu : menus) {
            if (menu.get("code").asText().equals("test_menu_unbind_" + timestamp)) {
                unbindMenuId = menu.get("id").asLong();
                break;
            }
        }
        Assertions.assertNotNull(unbindMenuId, "菜单ID不应为空");

        logger.info("【步骤3】创建测试API权限...");
        String unbindApiCode = "test:unbind:api:" + timestamp;
        String apiJson = "{\"name\":\"测试API-解绑-" + timestamp + "\",\"code\":\"" + unbindApiCode + "\",\"url\":\"/api/test/unbind\",\"method\":\"GET\"}";
        mockMvc.perform(post("/api/permission/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(apiJson)
                .session(adminSession))
                .andExpect(status().isOk());

        MvcResult apiListResult = mockMvc.perform(get("/api/permission/list").session(adminSession)).andReturn();
        JsonNode apis = objectMapper.readTree(apiListResult.getResponse().getContentAsString()).get("data");
        Long unbindApiId = null;
        for (JsonNode api : apis) {
            if (api.get("code").asText().equals(unbindApiCode)) {
                unbindApiId = api.get("id").asLong();
                break;
            }
        }
        Assertions.assertNotNull(unbindApiId, "API ID不应为空");

        logger.info("【步骤4】绑定角色与菜单、API...");
        mockMvc.perform(post("/api/role/" + unbindRoleId + "/menus")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[" + unbindMenuId + "]")
                .session(adminSession)).andExpect(status().isOk());
        mockMvc.perform(post("/api/role/" + unbindRoleId + "/apis")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[" + unbindApiId + "]")
                .session(adminSession)).andExpect(status().isOk());

        logger.info("【步骤5】创建测试用户并绑定角色...");
        String userJson = "{\"username\":\"testuser_unbind_" + timestamp + "\",\"password\":\"password123\",\"realName\":\"测试用户-解绑\",\"userType\":0,\"roleId\":" + unbindRoleId + "}";
        mockMvc.perform(post("/api/user/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson)
                .session(adminSession))
                .andExpect(status().isOk());

        MvcResult userListResult = mockMvc.perform(get("/api/user/list").session(adminSession)).andReturn();
        JsonNode users = objectMapper.readTree(userListResult.getResponse().getContentAsString()).get("data");
        Long unbindUserId = null;
        for (JsonNode user : users) {
            if (user.get("username").asText().equals("testuser_unbind_" + timestamp)) {
                unbindUserId = user.get("id").asLong();
                break;
            }
        }
        Assertions.assertNotNull(unbindUserId, "用户ID不应为空");
        logger.info("【步骤5】用户创建成功 - ID: {}", unbindUserId);

        logger.info("【步骤6】验证用户有角色时可以访问菜单...");
        MockHttpSession userSession = loginAsUser("testuser_unbind_" + timestamp, "password123");
        MvcResult menuTreeBefore = mockMvc.perform(get("/api/menu/tree").session(userSession)).andReturn();
        int menuTreeCodeBefore = getResponseCode(menuTreeBefore.getResponse().getContentAsString());
        logger.info("【步骤6】解绑前用户查看菜单树响应: code={}", menuTreeCodeBefore);
        Assertions.assertEquals(200, menuTreeCodeBefore, "有角色的用户应该能看到菜单");

        logger.info("【步骤7】解绑用户-角色关系...");
        MvcResult unbindResult = mockMvc.perform(post("/api/user/bindRole")
                .param("userId", String.valueOf(unbindUserId))
                .param("roleId", "")
                .session(adminSession))
                .andExpect(status().isOk())
                .andReturn();
        String unbindResponse = unbindResult.getResponse().getContentAsString();
        logger.info("【步骤7】解绑响应: {}", unbindResponse);

        logger.info("【步骤8】验证解绑后用户无法看到菜单...");
        MockHttpSession userSessionAfter = loginAsUser("testuser_unbind_" + timestamp, "password123");
        MvcResult menuTreeAfter = mockMvc.perform(get("/api/menu/tree").session(userSessionAfter)).andReturn();
        String menuTreeResponseAfter = menuTreeAfter.getResponse().getContentAsString();
        int menuTreeCodeAfter = getResponseCode(menuTreeResponseAfter);
        logger.info("【步骤8】解绑后用户查看菜单树响应: code={}", menuTreeCodeAfter);

        JsonNode menuTreeData = objectMapper.readTree(menuTreeResponseAfter).get("data");
        boolean hasMenus = menuTreeData != null && menuTreeData.isArray() && menuTreeData.size() > 0;
        logger.info("【步骤8】解绑后用户是否有菜单: {}", hasMenus);
        Assertions.assertFalse(hasMenus, "解绑角色后用户不应该看到菜单");

        logger.info("【测试2】通过 - 解绑用户-角色后权限失效验证成功");
    }

    @Test
    @Order(3)
    @DisplayName("测试3: 解绑验证 - 解绑角色-API后API权限失效")
    public void testUnbindRoleApiPermissionInvalid() throws Exception {
        logger.info("========================================");
        logger.info("【测试3】解绑验证 - 角色-API解绑后权限失效");
        logger.info("========================================");

        String timestamp = String.valueOf(System.currentTimeMillis());

        logger.info("【步骤1】创建测试角色...");
        String roleJson = "{\"name\":\"测试角色-解绑API-" + timestamp + "\",\"code\":\"TEST_UNBIND_API_" + timestamp + "\",\"description\":\"用于测试解绑API的角色\"}";
        mockMvc.perform(post("/api/role/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(roleJson)
                .session(adminSession))
                .andExpect(status().isOk());

        MvcResult roleListResult = mockMvc.perform(get("/api/role/list").session(adminSession)).andReturn();
        JsonNode roles = objectMapper.readTree(roleListResult.getResponse().getContentAsString()).get("data");
        Long roleId = null;
        for (JsonNode role : roles) {
            if (role.get("code").asText().equals("TEST_UNBIND_API_" + timestamp)) {
                roleId = role.get("id").asLong();
                break;
            }
        }
        Assertions.assertNotNull(roleId, "角色ID不应为空");

        logger.info("【步骤2】创建测试API权限...");
        String apiCode = "test:unbind:role:api:" + timestamp;
        String apiJson = "{\"name\":\"测试API-角色解绑-" + timestamp + "\",\"code\":\"" + apiCode + "\",\"url\":\"/api/test/unbind/role\",\"method\":\"GET\"}";
        mockMvc.perform(post("/api/permission/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(apiJson)
                .session(adminSession))
                .andExpect(status().isOk());

        MvcResult apiListResult = mockMvc.perform(get("/api/permission/list").session(adminSession)).andReturn();
        JsonNode apis = objectMapper.readTree(apiListResult.getResponse().getContentAsString()).get("data");
        Long apiId = null;
        for (JsonNode api : apis) {
            if (api.get("code").asText().equals(apiCode)) {
                apiId = api.get("id").asLong();
                break;
            }
        }
        Assertions.assertNotNull(apiId, "API ID不应为空");
        logger.info("【步骤2】API创建成功 - ID: {}, 编码: {}", apiId, apiCode);

        logger.info("【步骤3】绑定角色与API...");
        mockMvc.perform(post("/api/role/" + roleId + "/apis")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[" + apiId + "]")
                .session(adminSession)).andExpect(status().isOk());

        logger.info("【步骤4】创建测试用户...");
        String userJson = "{\"username\":\"testuser_unbind_role_api_" + timestamp + "\",\"password\":\"password123\",\"realName\":\"测试用户-解绑角色API\",\"userType\":0,\"roleId\":" + roleId + "}";
        mockMvc.perform(post("/api/user/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson)
                .session(adminSession))
                .andExpect(status().isOk());

        logger.info("【步骤5】验证用户有API权限时可以访问受保护API...");
        MockHttpSession userSession = loginAsUser("testuser_unbind_role_api_" + timestamp, "password123");
        MvcResult accessBefore = mockMvc.perform(get("/api/user/list").session(userSession)).andReturn();
        int accessCodeBefore = getResponseCode(accessBefore.getResponse().getContentAsString());
        logger.info("【步骤5】解绑前用户访问API响应: code={}", accessCodeBefore);

        logger.info("【步骤6】解绑角色-API关系...");
        MvcResult roleApisResult = mockMvc.perform(get("/api/role/" + roleId + "/apis").session(adminSession)).andReturn();
        JsonNode roleApis = objectMapper.readTree(roleApisResult.getResponse().getContentAsString()).get("data");
        logger.info("【步骤6】角色当前绑定的API: {}", roleApis);

        mockMvc.perform(post("/api/role/" + roleId + "/apis")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[]")
                .session(adminSession)).andExpect(status().isOk());
        logger.info("【步骤6】已清空角色绑定的API");

        logger.info("【步骤7】验证解绑后用户无法访问受保护API...");
        MockHttpSession userSessionAfter = loginAsUser("testuser_unbind_role_api_" + timestamp, "password123");
        MvcResult accessAfter = mockMvc.perform(get("/api/user/list").session(userSessionAfter)).andReturn();
        int accessCodeAfter = getResponseCode(accessAfter.getResponse().getContentAsString());
        logger.info("【步骤7】解绑后用户访问API响应: code={}", accessCodeAfter);
        Assertions.assertEquals(403, accessCodeAfter, "解绑API后用户应该无权限访问");

        logger.info("【测试3】通过 - 解绑角色-API后权限失效验证成功");
    }

    @Test
    @Order(4)
    @DisplayName("测试4: 多角色合并权限验证 - 权限是角色的并集")
    public void testMultiRolePermissionUnion() throws Exception {
        logger.info("========================================");
        logger.info("【测试4】多角色合并权限验证 - 权限并集");
        logger.info("========================================");

        String timestamp = String.valueOf(System.currentTimeMillis());

        logger.info("【步骤1】创建两个测试角色...");
        String role1Json = "{\"name\":\"测试角色-权限1-" + timestamp + "\",\"code\":\"TEST_ROLE_PERM1_" + timestamp + "\",\"description\":\"权限1\"}";
        String role2Json = "{\"name\":\"测试角色-权限2-" + timestamp + "\",\"code\":\"TEST_ROLE_PERM2_" + timestamp + "\",\"description\":\"权限2\"}";
        
        mockMvc.perform(post("/api/role/save").contentType(MediaType.APPLICATION_JSON).content(role1Json).session(adminSession)).andExpect(status().isOk());
        mockMvc.perform(post("/api/role/save").contentType(MediaType.APPLICATION_JSON).content(role2Json).session(adminSession)).andExpect(status().isOk());

        MvcResult roleListResult = mockMvc.perform(get("/api/role/list").session(adminSession)).andReturn();
        JsonNode roles = objectMapper.readTree(roleListResult.getResponse().getContentAsString()).get("data");
        Long role1Id = null, role2Id = null;
        for (JsonNode role : roles) {
            if (role.get("code").asText().equals("TEST_ROLE_PERM1_" + timestamp)) {
                role1Id = role.get("id").asLong();
            }
            if (role.get("code").asText().equals("TEST_ROLE_PERM2_" + timestamp)) {
                role2Id = role.get("id").asLong();
            }
        }
        Assertions.assertNotNull(role1Id, "角色1 ID不应为空");
        Assertions.assertNotNull(role2Id, "角色2 ID不应为空");
        logger.info("【步骤1】角色创建成功 - 角色1 ID: {}, 角色2 ID: {}", role1Id, role2Id);

        logger.info("【步骤2】为角色1创建菜单...");
        String menu1Json = "{\"name\":\"测试菜单-角色1-" + timestamp + "\",\"code\":\"test_menu_role1_" + timestamp + "\",\"path\":\"/test/role1\",\"parentId\":0,\"sort\":1,\"menuType\":1,\"status\":1}";
        mockMvc.perform(post("/api/menu/save").contentType(MediaType.APPLICATION_JSON).content(menu1Json).session(adminSession)).andExpect(status().isOk());

        MvcResult menuListResult = mockMvc.perform(get("/api/menu/list").session(adminSession)).andReturn();
        JsonNode menus = objectMapper.readTree(menuListResult.getResponse().getContentAsString()).get("data");
        Long menu1Id = null;
        for (JsonNode menu : menus) {
            if (menu.get("code").asText().equals("test_menu_role1_" + timestamp)) {
                menu1Id = menu.get("id").asLong();
                break;
            }
        }
        Assertions.assertNotNull(menu1Id, "菜单1 ID不应为空");

        mockMvc.perform(post("/api/role/" + role1Id + "/menus")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[" + menu1Id + "]")
                .session(adminSession)).andExpect(status().isOk());

        logger.info("【步骤3】为角色2创建菜单...");
        String menu2Json = "{\"name\":\"测试菜单-角色2-" + timestamp + "\",\"code\":\"test_menu_role2_" + timestamp + "\",\"path\":\"/test/role2\",\"parentId\":0,\"sort\":1,\"menuType\":1,\"status\":1}";
        mockMvc.perform(post("/api/menu/save").contentType(MediaType.APPLICATION_JSON).content(menu2Json).session(adminSession)).andExpect(status().isOk());

        menuListResult = mockMvc.perform(get("/api/menu/list").session(adminSession)).andReturn();
        menus = objectMapper.readTree(menuListResult.getResponse().getContentAsString()).get("data");
        Long menu2Id = null;
        for (JsonNode menu : menus) {
            if (menu.get("code").asText().equals("test_menu_role2_" + timestamp)) {
                menu2Id = menu.get("id").asLong();
                break;
            }
        }
        Assertions.assertNotNull(menu2Id, "菜单2 ID不应为空");

        mockMvc.perform(post("/api/role/" + role2Id + "/menus")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[" + menu2Id + "]")
                .session(adminSession)).andExpect(status().isOk());

        logger.info("【步骤4】创建测试用户并绑定角色1...");
        String userJson = "{\"username\":\"testuser_multirole_" + timestamp + "\",\"password\":\"password123\",\"realName\":\"测试用户-多角色\",\"userType\":0,\"roleId\":" + role1Id + "}";
        mockMvc.perform(post("/api/user/save").contentType(MediaType.APPLICATION_JSON).content(userJson).session(adminSession)).andExpect(status().isOk());

        logger.info("【步骤5】验证角色1用户可以看到的菜单...");
        MockHttpSession userSessionRole1 = loginAsUser("testuser_multirole_" + timestamp, "password123");
        MvcResult menuTreeRole1 = mockMvc.perform(get("/api/menu/tree").session(userSessionRole1)).andReturn();
        JsonNode role1Menus = objectMapper.readTree(menuTreeRole1.getResponse().getContentAsString()).get("data");
        int role1MenuCount = role1Menus != null && role1Menus.isArray() ? role1Menus.size() : 0;
        logger.info("【步骤5】角色1用户可访问菜单数: {}", role1MenuCount);

        logger.info("【步骤6】切换用户角色为角色2...");
        MvcResult userListResult = mockMvc.perform(get("/api/user/list").session(adminSession)).andReturn();
        JsonNode users = objectMapper.readTree(userListResult.getResponse().getContentAsString()).get("data");
        Long multiRoleUserId = null;
        for (JsonNode user : users) {
            if (user.get("username").asText().equals("testuser_multirole_" + timestamp)) {
                multiRoleUserId = user.get("id").asLong();
                break;
            }
        }
        mockMvc.perform(post("/api/user/bindRole")
                .param("userId", String.valueOf(multiRoleUserId))
                .param("roleId", String.valueOf(role2Id))
                .session(adminSession)).andExpect(status().isOk());

        logger.info("【步骤7】验证角色2用户可以看到的菜单...");
        MockHttpSession userSessionRole2 = loginAsUser("testuser_multirole_" + timestamp, "password123");
        MvcResult menuTreeRole2 = mockMvc.perform(get("/api/menu/tree").session(userSessionRole2)).andReturn();
        JsonNode role2Menus = objectMapper.readTree(menuTreeRole2.getResponse().getContentAsString()).get("data");
        int role2MenuCount = role2Menus != null && role2Menus.isArray() ? role2Menus.size() : 0;
        logger.info("【步骤7】角色2用户可访问菜单数: {}", role2MenuCount);

        logger.info("【步骤8】验证两个角色的菜单不同...");
        Assertions.assertTrue(role1MenuCount > 0, "角色1应该有菜单");
        Assertions.assertTrue(role2MenuCount > 0, "角色2应该有菜单");

        logger.info("【测试4】通过 - 多角色权限验证成功");
    }

    @Test
    @Order(5)
    @DisplayName("测试5: 无角色用户访问受保护API")
    public void testNoRoleUserAccessProtectedApi() throws Exception {
        logger.info("========================================");
        logger.info("【测试5】无角色用户访问受保护API");
        logger.info("========================================");

        String timestamp = String.valueOf(System.currentTimeMillis());

        logger.info("【步骤1】创建无角色的测试用户...");
        String userJson = "{\"username\":\"testuser_norole_" + timestamp + "\",\"password\":\"password123\",\"realName\":\"测试用户-无角色\",\"userType\":0}";
        MvcResult userResult = mockMvc.perform(post("/api/user/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson)
                .session(adminSession))
                .andExpect(status().isOk())
                .andReturn();
        String userResponse = userResult.getResponse().getContentAsString();
        logger.info("【步骤1】用户创建响应: {}", userResponse);
        Assertions.assertEquals(200, getResponseCode(userResponse), "用户创建应该成功");

        logger.info("【步骤2】模拟无角色用户登录...");
        MockHttpSession userSession = loginAsUser("testuser_norole_" + timestamp, "password123");

        logger.info("【步骤3】验证无角色用户访问受保护API返回403...");
        MvcResult accessResult = mockMvc.perform(get("/api/user/list")
                .session(userSession))
                .andExpect(status().isOk())
                .andReturn();
        String accessResponse = accessResult.getResponse().getContentAsString();
        int accessCode = getResponseCode(accessResponse);
        logger.info("【步骤3】无角色用户访问API响应: code={}", accessCode);
        Assertions.assertEquals(403, accessCode, "无角色用户访问受保护API应该返回403");

        logger.info("【步骤4】验证无角色用户查看菜单树返回空...");
        MvcResult menuTreeResult = mockMvc.perform(get("/api/menu/tree")
                .session(userSession))
                .andExpect(status().isOk())
                .andReturn();
        String menuTreeResponse = menuTreeResult.getResponse().getContentAsString();
        int menuTreeCode = getResponseCode(menuTreeResponse);
        JsonNode menuTreeData = objectMapper.readTree(menuTreeResponse).get("data");
        boolean hasMenus = menuTreeData != null && menuTreeData.isArray() && menuTreeData.size() > 0;
        logger.info("【步骤4】无角色用户查看菜单树响应: code={}, 是否有菜单: {}", menuTreeCode, hasMenus);
        Assertions.assertFalse(hasMenus, "无角色用户不应该看到任何菜单");

        logger.info("【测试5】通过 - 无角色用户访问受保护API验证成功");
    }

    @Test
    @Order(6)
    @DisplayName("测试6: 未登录用户访问受保护API")
    public void testNotLoggedInUserAccessProtectedApi() throws Exception {
        logger.info("========================================");
        logger.info("【测试6】未登录用户访问受保护API");
        logger.info("========================================");

        logger.info("【步骤1】验证未登录用户访问受保护API返回401...");
        MvcResult accessResult = mockMvc.perform(get("/api/user/list"))
                .andExpect(status().isOk())
                .andReturn();
        String accessResponse = accessResult.getResponse().getContentAsString();
        int accessCode = getResponseCode(accessResponse);
        logger.info("【步骤1】未登录用户访问API响应: code={}", accessCode);
        Assertions.assertEquals(401, accessCode, "未登录用户访问受保护API应该返回401");

        logger.info("【步骤2】验证未登录用户查看当前用户信息返回401...");
        MvcResult currentUserResult = mockMvc.perform(get("/current-user"))
                .andExpect(status().isOk())
                .andReturn();
        String currentUserResponse = currentUserResult.getResponse().getContentAsString();
        int currentUserCode = getResponseCode(currentUserResponse);
        logger.info("【步骤2】未登录用户查看当前用户信息响应: code={}", currentUserCode);
        Assertions.assertEquals(401, currentUserCode, "未登录用户查看当前用户信息应该返回401");

        logger.info("【测试6】通过 - 未登录用户访问受保护API验证成功");
    }

    @Test
    @Order(7)
    @DisplayName("测试7: 管理员用户不受权限限制")
    public void testAdminUserNoPermissionLimit() throws Exception {
        logger.info("========================================");
        logger.info("【测试7】管理员用户不受权限限制");
        logger.info("========================================");

        logger.info("【步骤1】验证管理员可以访问所有API...");
        MvcResult userListResult = mockMvc.perform(get("/api/user/list").session(adminSession))
                .andExpect(status().isOk())
                .andReturn();
        int userListCode = getResponseCode(userListResult.getResponse().getContentAsString());
        logger.info("【步骤1】管理员访问用户列表API响应: code={}", userListCode);
        Assertions.assertEquals(200, userListCode, "管理员应该能访问用户列表API");

        MvcResult roleListResult = mockMvc.perform(get("/api/role/list").session(adminSession))
                .andExpect(status().isOk())
                .andReturn();
        int roleListCode = getResponseCode(roleListResult.getResponse().getContentAsString());
        logger.info("【步骤1】管理员访问角色列表API响应: code={}", roleListCode);
        Assertions.assertEquals(200, roleListCode, "管理员应该能访问角色列表API");

        MvcResult menuListResult = mockMvc.perform(get("/api/menu/list").session(adminSession))
                .andExpect(status().isOk())
                .andReturn();
        int menuListCode = getResponseCode(menuListResult.getResponse().getContentAsString());
        logger.info("【步骤1】管理员访问菜单列表API响应: code={}", menuListCode);
        Assertions.assertEquals(200, menuListCode, "管理员应该能访问菜单列表API");

        logger.info("【步骤2】验证管理员可以查看所有菜单...");
        MvcResult menuTreeResult = mockMvc.perform(get("/api/menu/tree").session(adminSession))
                .andExpect(status().isOk())
                .andReturn();
        String menuTreeResponse = menuTreeResult.getResponse().getContentAsString();
        int menuTreeCode = getResponseCode(menuTreeResponse);
        JsonNode menuTreeData = objectMapper.readTree(menuTreeResponse).get("data");
        int menuCount = menuTreeData != null && menuTreeData.isArray() ? menuTreeData.size() : 0;
        logger.info("【步骤2】管理员查看菜单树响应: code={}, 菜单数量: {}", menuTreeCode, menuCount);
        Assertions.assertEquals(200, menuTreeCode, "管理员应该能查看菜单树");
        Assertions.assertTrue(menuCount > 0, "管理员应该能看到所有菜单");

        logger.info("【测试7】通过 - 管理员用户不受权限限制验证成功");
    }

    @AfterAll
    public void cleanup() {
        logger.info("========================================");
        logger.info("Web层权限集成测试执行完毕");
        logger.info("========================================");
    }
}
