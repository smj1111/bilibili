package com.sun.bilibili.dao;

import com.sun.bilibili.domain.auth.UserRole;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserRoleDao {
    List<UserRole> getUserRoleByUserId(Long userId);

    void addUserRole(UserRole userRole);
}
