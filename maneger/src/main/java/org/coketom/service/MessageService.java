package org.coketom.service;

import org.coketom.ServerEndPoint.ChatEndpoint;
import org.coketom.entity.message.UserMessage;
import org.coketom.entity.system.SysUser;

import java.util.List;
import java.util.Set;

public interface MessageService {
    SysUser getUserInfo(String token);
    void broadcast(UserMessage Msg, Set<ChatEndpoint> connections);

    void saveMessage(UserMessage userMessage);

    List<UserMessage> getMessagesByRoomId(Integer roomId);
}
