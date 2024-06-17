package org.coketom.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.coketom.entity.system.SysUser;

@Mapper
public interface SysUserMapper {
    SysUser selectUserInfoByUserName(String userName);
}
