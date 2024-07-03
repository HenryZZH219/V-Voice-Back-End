package org.coketom.service.impl;

import com.alibaba.fastjson.JSON;
import org.coketom.AuthContextUtil;
import org.coketom.entity.system.SysUser;
import org.coketom.mapper.SysUserMapper;
import org.coketom.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisServiceImpl implements RedisService {
    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    @Autowired
    private SysUserMapper sysUserMapper;
    @Override
    public void updateToken(String token) {
        String userName = AuthContextUtil.get().getUsername();
        SysUser sysUser = sysUserMapper.selectUserInfoByUserName(userName);
        redisTemplate.opsForValue()
                .set("user:login"+token,
                        JSON.toJSONString(sysUser),
                        30,
                        TimeUnit.MINUTES);
    }

    @Override
    public void addToken(String token, SysUser user) {
        redisTemplate.opsForValue()
                .set("user:login"+token,
                        JSON.toJSONString(user),
                        30,
                        TimeUnit.MINUTES);
    }

    @Override
    public void deleteToken(String token) {
        redisTemplate.delete("user:login" + token);
    }
}
