package org.coketom.service;

import org.coketom.dto.system.LoginDto;
import org.coketom.dto.system.PasswdDto;
import org.coketom.entity.system.SysUser;
import org.coketom.vo.system.LoginVo;

import java.util.List;

public interface SysUserService {
    LoginVo login(LoginDto loginDto);

    void register(SysUser sysUser);

    void logout(String token);

    SysUser getuserinfo(String token);

    void updateUser(String token, SysUser sysUser);

    void updatePasswd(String token, PasswdDto passwdDto);

    List<SysUser> getUserInfoByIds(List<Integer> ids);

    void updateAvatar(String token, String avatarRequest);
}
