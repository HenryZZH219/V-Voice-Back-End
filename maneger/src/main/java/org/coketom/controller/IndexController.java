package org.coketom.controller;



import org.coketom.dto.system.LoginDto;
import org.coketom.service.SysUserService;
import org.coketom.vo.common.ResultCodeEnum;
import org.coketom.vo.system.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.coketom.vo.common.Result;
@RestController
@RequestMapping(value = "/index")
public class IndexController {

    @Autowired
    private SysUserService sysUserService;
    public Result login(@RequestBody LoginDto loginDto){
        LoginVo loginVo = sysUserService.login(loginDto);
        return Result.build(loginVo, ResultCodeEnum.SUCCESS);

    }
}
