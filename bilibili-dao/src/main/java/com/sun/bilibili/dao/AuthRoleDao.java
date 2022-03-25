package com.sun.bilibili.dao;

import com.sun.bilibili.domain.auth.AuthRole;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuthRoleDao {
    AuthRole getRoleByCode(String code);
}
