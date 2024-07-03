package org.coketom.service;

import org.coketom.entity.system.SysUser;
import org.springframework.stereotype.Service;


public interface RedisService {
    void updateToken(String token);

    void addToken(String token, SysUser user);

    void deleteToken(String token);
}
