package org.coketom.service.impl;

import com.alibaba.fastjson.JSON;
import org.coketom.ServerEndPoint.ChatEndpoint;
import org.coketom.entity.message.UserMessage;
import org.coketom.entity.system.SysUser;
import org.coketom.mapper.MessageMapper;
import org.coketom.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    @Override
    public SysUser getUserInfo(String token) {
        String userJson = redisTemplate.opsForValue().get("user:login" + token);
        return JSON.parseObject(userJson, SysUser.class);
    }


    //websocket的广播
    @Override
    public void broadcast(UserMessage Msg, Set<ChatEndpoint> connections) {

        System.out.println(Msg);
        for (ChatEndpoint endpoint : connections) {
            synchronized (endpoint) {
                try {
                    endpoint.session.getBasicRemote().sendText(JSON.toJSONString(Msg));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void saveMessage(UserMessage userMessage) {
        messageMapper.saveMessage(userMessage);
    }

    @Override
    public List<UserMessage> getMessagesByRoomId(Integer roomId) {
        return messageMapper.getMessagesByRoomId(roomId);
    }
}
