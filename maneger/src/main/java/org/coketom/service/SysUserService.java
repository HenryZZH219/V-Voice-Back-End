package org.coketom.service;

import org.coketom.dto.system.LoginDto;
import org.coketom.entity.system.SysUser;
import org.coketom.vo.system.LoginVo;

public interface SysUserService {
    LoginVo login(LoginDto loginDto);

    void register(SysUser sysUser);

    void logout(String token);

    SysUser getuserinfo(String token);

    void updateUser(String token, SysUser sysUser);
}
