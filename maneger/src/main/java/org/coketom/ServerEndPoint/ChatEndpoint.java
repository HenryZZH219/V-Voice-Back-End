package org.coketom.ServerEndPoint;

import com.alibaba.fastjson.JSON;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.*;
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

@ServerEndpoint(value = "/chat/{roomId}", configurator = HttpSessionConfigurator.class)//
@Component
public class ChatEndpoint {



    private static MessageService messageService;

    @Autowired
    private void setMessageService(MessageService messageService){
        this.messageService = messageService;
    }

    private static final Set<ChatEndpoint> connections = new CopyOnWriteArraySet<>();
    public Session session;


    private Integer roomId;

    private SysUser user;
    @OnOpen
    public void onOpen(Session session, EndpointConfig config, @PathParam("roomId") Integer roomId ) {

        this.session = session;
        this.roomId = roomId;

        connections.add(this);

        String token = (String) config.getUserProperties().get("token");
        System.out.println("Token: " + token);

        this.user = messageService.getUserInfo(token);
        if(this.user == null) {
            throw new RuntimeException();
        }


        String message = String.format("【%s加入语音】", this.user.getName());
        System.out.println(message);
        UserMessage Msg = new UserMessage(this.roomId, this.user.getId(), message, "TEXT");
        System.out.println(messageService);
        messageService.broadcast(Msg, connections);

    }

    @OnClose
    public void onClose() {
        connections.remove(this);
        String message = String.format("【%s退出语音】",  this.user.getName());
        System.out.println(message);
        UserMessage Msg = new UserMessage(this.roomId, this.user.getId(), message, "TEXT");
        messageService.broadcast(Msg, connections);



    }

    @OnMessage
    public void onMessage(String message) {

        MessageDto messageDto = JSON.parseObject(message, MessageDto.class);
        UserMessage userMessage = new UserMessage(messageDto, this.roomId, this.user.getId());
        messageService.broadcast(userMessage, connections);

        messageService.saveMessage(userMessage);
    }

    private void broadcast(String message, boolean systemMsg) {

        UserMessage Msg = new UserMessage(this.roomId,  this.user.getId(), message, "TEXT");

    }
}
