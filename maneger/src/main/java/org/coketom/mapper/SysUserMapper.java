package org.coketom.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.coketom.entity.system.SysUser;

import java.util.List;

@Mapper
public interface SysUserMapper {
    SysUser selectUserInfoByUserName(String userName);

    List<SysUser> selectUsersByIds(List<Integer> ids);
    void register(SysUser sysUser);

    void setEmail(Integer userId, String email);

    void setPhone(Integer userId, String phone);

    void setName(Integer userId, String name);

    void setDescription(Integer userId, String description);

    void setPasswd(Integer userId, String newPasswd);

    void setAvatar(Integer userId, String url);
}
