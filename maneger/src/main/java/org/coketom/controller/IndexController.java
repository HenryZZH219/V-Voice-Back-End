package org.coketom.controller;



import org.coketom.dto.system.LoginDto;
import org.coketom.entity.system.SysUser;
import org.coketom.exception.TomException;
import org.coketom.service.SysUserService;
import org.coketom.vo.common.ResultCodeEnum;
import org.coketom.vo.system.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.coketom.vo.common.Result;
@RestController
@RequestMapping(value = "/index")
public class IndexController {

    @Autowired
    private SysUserService sysUserService;
    @PostMapping("/login")
    public Result login(@RequestBody LoginDto loginDto){
        LoginVo loginVo = sysUserService.login(loginDto);
        return Result.build(loginVo, ResultCodeEnum.SUCCESS);

    }

    @PostMapping("/register")
    public Result register(@RequestBody SysUser sysUser){

        sysUserService.register(sysUser);

        return Result.build(null, ResultCodeEnum.SUCCESS);
    }

    //退出
    @GetMapping(value = "/logout")
    public Result logout(@RequestHeader(name = "token") String token) {
        sysUserService.logout(token);
        return Result.build(null,ResultCodeEnum.SUCCESS);
    }
}
