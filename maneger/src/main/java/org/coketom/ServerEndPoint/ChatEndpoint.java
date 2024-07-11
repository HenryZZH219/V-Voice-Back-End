package org.coketom.ServerEndPoint;

import com.alibaba.fastjson.JSON;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import org.coketom.AuthContextUtil;
import org.coketom.config.HttpSessionConfigurator;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

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

    private static ScheduledExecutorService heartbeatScheduler = Executors.newScheduledThreadPool(1);

    static {
        // 定期执行心跳检测
        heartbeatScheduler.scheduleAtFixedRate(ChatEndpoint::sendHeartbeats, 0, 30, TimeUnit.SECONDS);
    }
    @Autowired
    private void setMessageService(MessageService messageService){
        this.messageService = messageService;
    }

//    private static final Set<ChatEndpoint> connections = new CopyOnWriteArraySet<>();
    private static Map<Integer, Map<Integer, ChatEndpoint>> rooms = new ConcurrentHashMap<>();
    public Session session;
    private Integer roomId;
    private SysUser user;
    @OnOpen
    public void onOpen(Session session, EndpointConfig config, @PathParam("roomId") Integer roomId ) {

        this.session = session;
        this.roomId = roomId;
        String token = (String) config.getUserProperties().get("token");
//        System.out.println("Token: " + token);
        this.user = messageService.getUserInfo(token);
        if(this.user == null) {
            throw new RuntimeException();
        }
        Map<Integer, ChatEndpoint> connections = rooms.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>());
        connections.put(this.user.getId(), this);
        session.getUserProperties().put("lastPongTime", System.currentTimeMillis());






        String message = String.format("【%s加入语音】", this.user.getName());
        UserMessage Msg = new UserMessage(this.roomId, this.user.getId(), message, "SysMsg");
        messageService.broadcast(Msg, connections);

    }

    @OnClose
    public void onClose() {
        Map<Integer, ChatEndpoint> connections = rooms.get(roomId);
//        connections.remove(this);
        if (connections != null) {
            connections.remove(this.user.getId());
            if (connections.isEmpty()) {
                rooms.remove(roomId);
            }
        }
        String message = String.format("【%s退出语音】",  this.user.getName());

        UserMessage Msg = new UserMessage(this.roomId, this.user.getId(), message, "SysMsg");
        messageService.broadcast(Msg, connections);
    }

    @OnMessage
    public void onMessage(String message) {
        Map<Integer, ChatEndpoint> connections = rooms.get(roomId);
        MessageDto messageDto = JSON.parseObject(message, MessageDto.class);
        if(messageDto.getMessageType().equals("PING_PONG")) {
            this.session.getUserProperties().put("lastPongTime", System.currentTimeMillis());
        }else {
            UserMessage userMessage = new UserMessage(messageDto, this.roomId, this.user.getId());
            messageService.broadcast(userMessage, connections);
            messageService.saveMessage(userMessage);
        }

    }



    private static void sendHeartbeats() {
        long now = System.currentTimeMillis();
        rooms.forEach((roomId, connections) -> {
            connections.forEach((userId, connection) -> {
                try {
                    Session session = connection.session;
                    Long lastPongTime = (Long) session.getUserProperties().get("lastPongTime");
                    if (lastPongTime != null && now - lastPongTime > 60000) {
                        // 如果超过60秒没有收到pong，则认为连接失效，关闭连接
//                        connections.remove(connection);
                        connections.remove(connection.user.getId());
                        session.close(new CloseReason(CloseReason.CloseCodes.GOING_AWAY, "Heartbeat failed"));

                    }
//                    System.out.println("ping: "+connection.user.getId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            if (connections.isEmpty()) {
                rooms.remove(roomId);
            }
            UserMessage Msg = new UserMessage(0, 1, "ping", "PING_PONG");
            messageService.broadcast(Msg, connections);
        });

    }


    public static Map<Integer, Map<Integer, ChatEndpoint>> getRooms() {
        return rooms;
    }
}
