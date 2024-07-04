package org.coketom.vo.system;

import lombok.Data;
import org.coketom.entity.system.SysUser;

@Data
public class LoginVo {
    private String token;
    private SysUser sysUser;
}
