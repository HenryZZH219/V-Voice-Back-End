package org.coketom.entity.system;

import lombok.Data;

@Data
public class SysUser {
    private String userName;

    private String password;

    private String name;

    private String mail;

    private String phone;

    private String avatar;

    private String description;

    private Integer status;
}
