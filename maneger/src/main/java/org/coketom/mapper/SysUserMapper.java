package org.coketom.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.coketom.entity.system.SysUser;

@Mapper
public interface SysUserMapper {
    SysUser selectUserInfoByUserName(String userName);

    void register(SysUser sysUser);

    void setEmail(Integer userId, String email);

    void setPhone(Integer userId, String phone);

    void setName(Integer userId, String name);

    void setDescription(Integer userId, String description);

    void setPasswd(Integer userId, String newPasswd);
}
