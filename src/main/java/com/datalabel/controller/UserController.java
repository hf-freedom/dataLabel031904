package com.datalabel.controller;

import com.datalabel.annotation.RequireApiPermission;
import com.datalabel.common.Result;
import com.datalabel.entity.User;
import com.datalabel.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/list")
    @RequireApiPermission("user:list")
    public Result<List<User>> list() {
        return Result.success(userService.findAll());
    }
    
    @GetMapping("/{id}")
    @RequireApiPermission("user:view")
    public Result<User> getById(@PathVariable Long id) {
        User user = userService.findById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(user);
    }
    
    @PostMapping("/save")
    @RequireApiPermission("user:save")
    public Result<String> save(@RequestBody User user) {
        User existUser = userService.findByUsername(user.getUsername());
        if (existUser != null && !existUser.getId().equals(user.getId())) {
            return Result.error("用户名已存在");
        }
        if (userService.save(user)) {
            return Result.success("保存成功", null);
        }
        return Result.error("保存失败");
    }
    
    @PostMapping("/update")
    @RequireApiPermission("user:update")
    public Result<String> update(@RequestBody User user, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        if (currentUser.getUserType() == 0) {
            User updateUser = userService.findById(currentUser.getId());
            updateUser.setRealName(user.getRealName());
            updateUser.setEmail(user.getEmail());
            updateUser.setPhone(user.getPhone());
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                updateUser.setPassword(user.getPassword());
            }
            if (userService.update(updateUser)) {
                session.setAttribute("currentUser", updateUser);
                return Result.success("修改成功", null);
            }
        } else {
            if (userService.update(user)) {
                return Result.success("修改成功", null);
            }
        }
        return Result.error("修改失败");
    }
    
    @DeleteMapping("/{id}")
    @RequireApiPermission("user:delete")
    public Result<String> delete(@PathVariable Long id) {
        if (userService.deleteById(id)) {
            return Result.success("删除成功", null);
        }
        return Result.error("删除失败");
    }
    
    @PostMapping("/bindRole")
    @RequireApiPermission("user:bindRole")
    public Result<String> bindRole(@RequestParam Long userId, @RequestParam Long roleId) {
        if (userService.bindRole(userId, roleId)) {
            return Result.success("绑定成功", null);
        }
        return Result.error("绑定失败");
    }
}
