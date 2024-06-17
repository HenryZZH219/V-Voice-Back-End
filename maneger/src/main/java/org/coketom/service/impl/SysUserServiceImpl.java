package org.coketom.service.impl;

import org.coketom.dto.system.LoginDto;
import org.coketom.entity.system.SysUser;
import org.coketom.mapper.SysUserMapper;
import org.coketom.service.SysUserService;
import org.coketom.vo.system.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl implements SysUserService {


    @Autowired
    private SysUserMapper sysUserMapper;
    @Override
    public LoginVo login(LoginDto loginDto) {
        String userName = loginDto.getUserName();
        SysUser sysUser = sysUserMapper.selectUserInfoByUserName(userName);
        if(sysUser == null){
            throw new RuntimeException("user not found");
            return null;
        }


    }
}
