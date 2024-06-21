package org.coketom.dto.system;

import lombok.Data;

@Data
public class PasswdDto {
    private String oldPasswd;
    private String newPasswd;
}
