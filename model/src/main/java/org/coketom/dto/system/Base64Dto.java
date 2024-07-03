package org.coketom.dto.system;

import lombok.Data;

@Data
public class Base64Dto {
    private String base64;

    public Base64Dto(String base64) {
        this.base64 = base64;
    }
}
