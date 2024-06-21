package org.coketom.service;

import org.coketom.ServerEndPoint.ChatEndpoint;
import org.coketom.entity.message.UserMessage;

import java.util.Set;

public interface MessageService {
    void broadcast(UserMessage Msg, Set<ChatEndpoint> connections);

    void saveMessage(UserMessage userMessage);
}
