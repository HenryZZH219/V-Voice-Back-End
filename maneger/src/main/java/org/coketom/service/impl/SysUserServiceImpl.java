package org.coketom.service.impl;

import com.alibaba.fastjson.JSON;
import jakarta.servlet.http.HttpSession;
import org.coketom.AuthContextUtil;
import org.coketom.dto.system.LoginDto;
import org.coketom.dto.system.PasswdDto;
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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
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
        sysUser.setAvatar(getFirstSegment(userName));
        sysUserMapper.register(sysUser);
    }

    public static String getFirstSegment(String str) {
        if (str == null || str.isEmpty()) {
            return "";
        }

        // 使用正则表达式匹配第一个单词或第一个非英文字符
        String[] words = str.split("\\s+"); // 以空白字符分割字符串
        for (String word : words) {
            // 检查每个单词
            if (word.matches("^[a-zA-Z0-9]+")) {
                // 如果是英文或数字单词，返回它
                return word;
            } else {
                // 否则，遍历这个单词中的每个字符，找到第一个非英文字符
                for (char c : word.toCharArray()) {
                    if (!Character.isLetterOrDigit(c)) {
                        return String.valueOf(c);
                    }
                }
            }
        }

        // 如果没有匹配到任何非英文字符或单词，则返回空字符串
        return "";
    }

    @Override
    public void logout(String token) {
        redisTemplate.delete("user:login" + token);
        System.out.println();
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

    @Override
    public void updatePasswd(String token, PasswdDto passwdDto) {
        SysUser sysUser = AuthContextUtil.get();
        String oldPasswd = DigestUtils.md5DigestAsHex(passwdDto.getOldPasswd().getBytes());
        if(!oldPasswd.equals(sysUser.getPassword())){
            throw new TomException(ResultCodeEnum.LOGIN_ERROR);
        }
        String newPasswd = DigestUtils.md5DigestAsHex(passwdDto.getNewPasswd().getBytes());
        sysUserMapper.setPasswd(sysUser.getId(), newPasswd);
        String userName = sysUser.getUsername();
        sysUser = sysUserMapper.selectUserInfoByUserName(userName);
        redisTemplate.opsForValue()
                .set("user:login"+token,
                        JSON.toJSONString(sysUser),
                        30,
                        TimeUnit.MINUTES);
    }

    @Override
    public List<SysUser> getUserInfoByIds(List<Integer> ids) {
        return sysUserMapper.selectUsersByIds(ids);
    }
}
