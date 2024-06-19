package org.coketom.controller;

import org.coketom.entity.system.SysUser;
import org.coketom.service.SysUserService;
import org.coketom.vo.common.Result;
import org.coketom.vo.common.ResultCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/updateUser")
    public Result register(@RequestHeader(name = "token") String token, @RequestBody SysUser sysUser){
        System.out.println(sysUser);
        sysUserService.updateUser(token, sysUser);
        return Result.build(null, ResultCodeEnum.SUCCESS);
    }
}
