package org.coketom.controller;

import org.coketom.dto.system.PasswdDto;
import org.coketom.entity.system.SysUser;
import org.coketom.service.SysUserService;
import org.coketom.vo.common.Result;
import org.coketom.vo.common.ResultCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/user")
public class UserController {
    @Autowired
    private SysUserService sysUserService;
    @GetMapping("getuserinfo")
    public Result getuserinfo(@RequestHeader(name = "token") String token){
        SysUser sysUser = sysUserService.getuserinfo(token);
        return Result.build(sysUser, ResultCodeEnum.SUCCESS);
    }

    @PutMapping("/updateUser")
    public Result updateUser(@RequestHeader(name = "token") String token, @RequestBody SysUser sysUser){
        sysUserService.updateUser(token, sysUser);
        return Result.build(null, ResultCodeEnum.SUCCESS);
    }

    @PutMapping("/updatePasswd")
    public Result updatePasswd(@RequestHeader(name = "token") String token, @RequestBody PasswdDto passwdDto){
        sysUserService.updatePasswd(token, passwdDto);
        return Result.build(null, ResultCodeEnum.SUCCESS);
    }

    @PostMapping("/GetUserInfoByIds")
    public Result getUserInfoByIds(@RequestBody List<Integer> ids){
        List<SysUser> sysUsers = sysUserService.getUserInfoByIds(ids);
        return Result.build(sysUsers, ResultCodeEnum.SUCCESS);
    }
}
