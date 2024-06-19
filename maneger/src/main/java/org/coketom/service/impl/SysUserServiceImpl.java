package org.coketom.service.impl;

import com.alibaba.fastjson.JSON;
import org.coketom.AuthContextUtil;
import org.coketom.dto.system.LoginDto;
import org.coketom.entity.system.SysUser;
import org.coketom.exception.TomException;
import org.coketom.mapper.SysUserMapper;
import org.coketom.service.SysUserService;
import org.coketom.vo.common.ResultCodeEnum;
import org.coketom.vo.system.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class SysUserServiceImpl implements SysUserService {
    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private SysUserMapper sysUserMapper;
    @Override
    public LoginVo login(LoginDto loginDto) {
//        System.out.println(loginDto);
        String userName = loginDto.getUsername();
        SysUser sysUser = sysUserMapper.selectUserInfoByUserName(userName);
        if(sysUser == null){
            throw new TomException(ResultCodeEnum.LOGIN_ERROR);
        }

        String password = sysUser.getPassword();
        String input_password =
                DigestUtils.md5DigestAsHex(loginDto.getPassword().getBytes());

        if(!password.equals(input_password)){
            throw new TomException(ResultCodeEnum.LOGIN_ERROR);
        }

        String token = UUID.randomUUID().toString().replaceAll("-","");


        redisTemplate.opsForValue()
                .set("user:login"+token,
                        JSON.toJSONString(sysUser),
                        30,
                        TimeUnit.MINUTES);
        LoginVo loginVo = new LoginVo();
        loginVo.setToken(token);
        return loginVo;


    }

    @Override
    public void register(SysUser sysUser) {

        String userName = sysUser.getUsername();
        SysUser _sysUser = sysUserMapper.selectUserInfoByUserName(userName);
        if(_sysUser != null){
            throw new TomException(ResultCodeEnum.USER_NAME_IS_EXISTS);
        }
        String password = DigestUtils.md5DigestAsHex(sysUser.getPassword().getBytes());
        sysUser.setPassword(password);
        sysUserMapper.register(sysUser);
    }

    @Override
    public void logout(String token) {
        redisTemplate.delete("user:login" + token);
    }

    @Override
    public SysUser getuserinfo(String token) {
        return AuthContextUtil.get();
    }

    @Override
    public void updateUser(String token, SysUser sysUser) {
        Integer userId = AuthContextUtil.get().getId();
        if(sysUser.getName() != null){
            sysUserMapper.setName(userId, sysUser.getName());
        }
        if(sysUser.getPhone() != null){
            sysUserMapper.setPhone(userId, sysUser.getPhone());
        }
        if(sysUser.getEmail() != null){
            sysUserMapper.setEmail(userId, sysUser.getEmail());
        }
        if(sysUser.getDescription() != null){
            sysUserMapper.setDescription(userId, sysUser.getDescription());
        }
        String userName = AuthContextUtil.get().getUsername();
        sysUser = sysUserMapper.selectUserInfoByUserName(userName);
        redisTemplate.opsForValue()
                .set("user:login"+token,
                        JSON.toJSONString(sysUser),
                        30,
                        TimeUnit.MINUTES);
    }
}
