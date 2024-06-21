package org.coketom.service.impl;

import com.alibaba.fastjson.JSON;
import org.coketom.ServerEndPoint.ChatEndpoint;
import org.coketom.entity.message.UserMessage;
import org.coketom.mapper.MessageMapper;
import org.coketom.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Set;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageMapper messageMapper;
    @Override
    public void broadcast(UserMessage Msg, Set<ChatEndpoint> connections) {
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
}
