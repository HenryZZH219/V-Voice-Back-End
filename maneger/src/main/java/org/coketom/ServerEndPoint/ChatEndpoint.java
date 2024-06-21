package org.coketom.ServerEndPoint;

import com.alibaba.fastjson.JSON;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import org.coketom.AuthContextUtil;
import org.coketom.config.HttpSessionConfigurator;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import jakarta.websocket.server.ServerEndpoint;
import org.coketom.dto.message.MessageDto;
import org.coketom.entity.message.UserMessage;
import org.coketom.entity.system.SysUser;
import org.coketom.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@ServerEndpoint(value = "/chat/{roomId}", configurator = HttpSessionConfigurator.class)
@Component
public class ChatEndpoint {


    @Autowired
    private MessageService messageService;
    private static final Set<ChatEndpoint> connections = new CopyOnWriteArraySet<>();
    public Session session;
    private HttpSession httpSession;

    private Integer roomId;
    @OnOpen
    public void onOpen(Session session, @PathParam("roomId") Integer roomId) {
        this.session = session;
        this.roomId = roomId;
        this.httpSession = (HttpSession) session.getUserProperties().get(HttpSession.class.getName());
        connections.add(this);
        // You can now use the httpSession object
        SysUser sysUser = AuthContextUtil.get();
        String message = String.format("【%d加入语音】", sysUser.getName());
        UserMessage Msg = new UserMessage(this.roomId, 0, message, "TEXT");
        messageService.broadcast(Msg, connections);

    }

    @OnClose
    public void onClose() {
        connections.remove(this);

        SysUser sysUser = AuthContextUtil.get();
        String message = String.format("【%d退出语音】", sysUser.getName());
        UserMessage Msg = new UserMessage(this.roomId, 0, message, "TEXT");
        messageService.broadcast(Msg, connections);

    }

    @OnMessage
    public void onMessage(String message) {
        SysUser sysUser = AuthContextUtil.get();
        MessageDto messageDto = JSON.parseObject(message, MessageDto.class);
        UserMessage userMessage = new UserMessage(messageDto, this.roomId, sysUser.getId());
        messageService.broadcast(userMessage, connections);

//        messageService.saveMessage(userMessage);
    }

    private void broadcast(String message, boolean systemMsg) {
        SysUser sysUser = AuthContextUtil.get();
        UserMessage Msg = new UserMessage(this.roomId, sysUser.getId(), message, "TEXT");

    }
}
