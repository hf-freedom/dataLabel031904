package com.datalabel.aspect;

import com.datalabel.annotation.RequireApiPermission;
import com.datalabel.common.Result;
import com.datalabel.entity.User;
import com.datalabel.service.ApiPermissionService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Method;
import java.util.Collections;

@Aspect
@Component
public class ApiPermissionAspect {
    
    @Autowired
    private ApiPermissionService apiPermissionService;
    
    @Around("@annotation(com.datalabel.annotation.RequireApiPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequireApiPermission annotation = method.getAnnotation(RequireApiPermission.class);
        String apiCode = annotation.value();
        
        HttpSession session = null;
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof HttpSession) {
                session = (HttpSession) arg;
                break;
            }
        }
        
        if (session == null) {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                session = request.getSession(false);
            }
        }
        
        if (session == null) {
            return Result.error(401, "未登录");
        }
        
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        
        if (currentUser.getUserType() != null && currentUser.getUserType() == 1) {
            return joinPoint.proceed();
        }
        
        Long roleId = currentUser.getRoleId();
        if (roleId == null) {
            return Result.error(403, "无权限访问该接口");
        }
        
        boolean hasPermission = apiPermissionService.hasPermission(Collections.singletonList(roleId), apiCode);
        if (!hasPermission) {
            return Result.error(403, "无权限访问该接口");
        }
        
        return joinPoint.proceed();
    }
}
