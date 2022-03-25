package com.sun.bilibili.api.aspect;

import com.sun.bilibili.api.support.UserSupport;
import com.sun.bilibili.domain.UserMoment;
import com.sun.bilibili.domain.annotation.ApiLimitedRole;
import com.sun.bilibili.domain.auth.UserRole;
import com.sun.bilibili.domain.constant.AuthRoleConstant;
import com.sun.bilibili.domain.exception.ConditionException;
import com.sun.bilibili.service.UserRoleService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Order(1)
@Component
@Aspect
public class DataLimitedAspect {

    @Autowired
    private UserSupport userSupport;

    @Autowired
    private UserRoleService userRoleService;

    @Pointcut("@annotation(com.sun.bilibili.domain.annotation.DataLimited)")
    public void check(){

    }

    @Before("check()")
    public void doBefore(JoinPoint joinPoint){
        Long userId =userSupport.getCurrentUserId();
        List<UserRole> userRoleList=userRoleService.getUserRoleByUserId(userId);
        Set<String> roleCodeSet=userRoleList.stream().map(UserRole::getRoleCode).collect(Collectors.toSet());
        Object[] args=joinPoint.getArgs();
        for (Object arg:args){
            if(arg instanceof UserMoment){
                UserMoment userMoment=(UserMoment)arg;
                if(roleCodeSet.contains(AuthRoleConstant.ROLE_LV0)&&"0".equals(userMoment.getType())){
                    throw new ConditionException("参数异常");
                }
            }
        }
    }
}
