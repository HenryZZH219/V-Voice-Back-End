package org.coketom.entity.system;

import lombok.Data;

@Data
public class SysUser {
    private Integer id;

    private String username;

    private String password;

    private String name;

    private String email;

    private String phone;

    private String avatar;

    private String description;

    private Integer status;
}
