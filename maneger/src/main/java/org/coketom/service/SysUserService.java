package org.coketom.service;

import org.coketom.dto.system.LoginDto;
import org.coketom.vo.system.LoginVo;

public interface SysUserService {
    LoginVo login(LoginDto loginDto);
}
