package org.coketom.service;

import com.github.pagehelper.PageInfo;
import jakarta.websocket.Session;
import org.coketom.ServerEndPoint.ChatEndpoint;
import org.coketom.dto.message.MessageDto;
import org.coketom.entity.message.UserMessage;
import org.coketom.entity.system.SysUser;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface MessageService {
    SysUser getUserInfo(String token);
    void broadcast(UserMessage Msg, Map<Integer, ChatEndpoint> connections);

    void sendRTCMessage(MessageDto Msg, Session session);
//    void sendMsgToSingleUser(UserMessage Msg, Integer userId);
    void saveMessage(UserMessage userMessage);

    List<UserMessage> getMessagesByRoomId(Integer roomId);

    PageInfo<UserMessage> getMessagesByRoomIdByPage(Integer roomId, int page, int size);
}
