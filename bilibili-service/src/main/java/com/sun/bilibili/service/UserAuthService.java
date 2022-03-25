package com.sun.bilibili.service;

import com.sun.bilibili.domain.auth.*;
import com.sun.bilibili.domain.constant.AuthRoleConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserAuthService {

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private AuthRoleService authRoleService;

    public UserAuthorities getUserAuthorities(Long userId) {
        List<UserRole> userRoleList=userRoleService.getUserRoleByUserId(userId);
        Set<Long> roleIdSet=userRoleList.stream().map(UserRole::getRoleId).collect(Collectors.toSet());
        List<AuthRoleElementOperation> roleElementOperationList=authRoleService.getRoleElementOperationsByRoleIds(roleIdSet);
        List<AuthRoleMenu> authRoleMenuList =authRoleService.getAuthRoleMenusByRoleIds(roleIdSet);
        UserAuthorities userAuthorities=new UserAuthorities();
        userAuthorities.setRoleMenuList(authRoleMenuList);
        userAuthorities.setRoleElementOperationList(roleElementOperationList);
        return userAuthorities;
    }

    public void addUserDefaultRole(long id) {
        UserRole userRole=new UserRole();
        AuthRole authRole=authRoleService.getRoleByCode(AuthRoleConstant.ROLE_LV0);
        userRole.setUserId(id);
        userRole.setRoleId(authRole.getId());
        userRoleService.addUserRole(userRole);
    }
}
