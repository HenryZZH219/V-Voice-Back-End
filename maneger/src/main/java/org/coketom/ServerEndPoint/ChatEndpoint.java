package org.coketom.ServerEndPoint;

import com.alibaba.fastjson.JSON;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.coketom.config.HttpSessionConfigurator;
import org.coketom.dto.message.MessageDto;
import org.coketom.entity.message.UserMessage;
import org.coketom.entity.message.WebRTCMessage;
import org.coketom.entity.system.SysUser;
import org.coketom.service.MessageService;
import org.coketom.webRTC.KurentoService;
import org.kurento.client.IceCandidate;
import org.kurento.client.WebRtcEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@ServerEndpoint(value = "/chat/{roomId}", configurator = HttpSessionConfigurator.class)//
@Component
public class ChatEndpoint {

    private static MessageService messageService;

    private static ScheduledExecutorService heartbeatScheduler = Executors.newScheduledThreadPool(1);
    //    private static final Set<ChatEndpoint> connections = new CopyOnWriteArraySet<>();
    private static Map<Integer, Map<Integer, ChatEndpoint>> rooms = new ConcurrentHashMap<>();

    static {
        // 定期执行心跳检测
        heartbeatScheduler.scheduleAtFixedRate(ChatEndpoint::sendHeartbeats, 0, 30, TimeUnit.SECONDS);
    }

    public Session session;
    private Integer roomId;
    private SysUser user;
    //webRTC相关
    @Autowired
    private KurentoService kurentoService;

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

    @Autowired
    private void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig config, @PathParam("roomId") Integer roomId) {

        this.session = session;
        this.roomId = roomId;
        String token = (String) config.getUserProperties().get("token");
//        System.out.println("Token: " + token);
        this.user = messageService.getUserInfo(token);
        if (this.user == null) {
            throw new RuntimeException();
        }
        Map<Integer, ChatEndpoint> connections = rooms.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>());
        connections.put(this.user.getId(), this);
        session.getUserProperties().put("lastPongTime", System.currentTimeMillis());


        String message = String.format("【%s加入语音】", this.user.getName());
        UserMessage Msg = new UserMessage(this.roomId, this.user.getId(), message, "SysMsg");
        messageService.broadcast(Msg, connections);


        //webRTC初始化
        kurentoService.createEndpoint(roomId, this.user.getId());

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
        String message = String.format("【%s退出语音】", this.user.getName());

        UserMessage Msg = new UserMessage(this.roomId, this.user.getId(), message, "SysMsg");
        messageService.broadcast(Msg, connections);

        // 移除WebRtcEndpoint
        kurentoService.removeEndpoint(roomId, this.user.getId());
    }

    @OnMessage
    public void onMessage(String message) {
        Map<Integer, ChatEndpoint> connections = rooms.get(roomId);
        MessageDto messageDto = JSON.parseObject(message, MessageDto.class);
//        if(messageDto.getMessageType().equals("PING_PONG")) {
//            this.session.getUserProperties().put("lastPongTime", System.currentTimeMillis());
//        }else {
//            UserMessage userMessage = new UserMessage(messageDto, this.roomId, this.user.getId());
//            messageService.broadcast(userMessage, connections);
//            messageService.saveMessage(userMessage);
//        }
        switch (messageDto.getMessageType()) {
            case "PING_PONG":
                this.session.getUserProperties().put("lastPongTime", System.currentTimeMillis());
                break;
            case "RTCMsg":
                handleWebRTCMessage(messageDto, connections);
                break;
            default:
                UserMessage userMessage = new UserMessage(messageDto, this.roomId, this.user.getId());
                messageService.broadcast(userMessage, connections);
                messageService.saveMessage(userMessage);
                break;
        }

    }

    private void handleWebRTCMessage_oldversion(MessageDto messageDto, Map<Integer, ChatEndpoint> connections) {
        System.out.println(messageDto);
        // 解析 WebRTC 信令消息
        WebRTCMessage webRTCMessage = JSON.parseObject(messageDto.getContent(), WebRTCMessage.class);

        System.out.println("转发RTC消息");
        System.out.println(webRTCMessage);

        // 确定目标用户
        Integer targetUserId = webRTCMessage.getTo();
        ChatEndpoint targetEndpoint = connections.get(targetUserId);
        if (targetEndpoint != null) {
            try {
                // 将消息转发给目标用户
                targetEndpoint.session.getBasicRemote().sendText(JSON.toJSONString(messageDto));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void forwardWebRTCMessage(WebRTCMessage webRTCMessage){
        System.out.println("转发RTC消息");
        System.out.println(webRTCMessage);
        MessageDto messageDto = new MessageDto();
        // 确定目标用户
        Integer targetUserId = webRTCMessage.getTo();

        Map<Integer, ChatEndpoint> connections = rooms.get(roomId);
        ChatEndpoint targetEndpoint = connections.get(targetUserId);

        messageDto.setMessageType("RTCMsg");
        messageDto.setContent(JSON.toJSONString(webRTCMessage));
        if (targetEndpoint != null) {
            try {
                // 将消息转发给目标用户
                targetEndpoint.session.getBasicRemote().sendText(JSON.toJSONString(messageDto));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleWebRTCMessage(MessageDto messageDto) {
        // 解析 WebRTC 信令消息
        WebRTCMessage webRTCMessage = JSON.parseObject(messageDto.getContent(), WebRTCMessage.class);
        String messageType = webRTCMessage.getType();
        //rtc服务器处理
        switch (messageType) {
            case "offer":
                handleOffer(webRTCMessage);
                break;
            case "answer":
                handleAnswer(webRTCMessage);
                break;
            case "candidate":
                handleCandidate(webRTCMessage);
                break;
            default:
                System.out.println("Unknown WebRTC message type: " + messageType);
                break;
        }

    }

    private void handleOffer(WebRTCMessage webRTCMessage) {



        WebRtcEndpoint webRtcEndpoint = kurentoService.getEndpoint(roomId, webRTCMessage.getFrom());
        String sdpOffer = webRTCMessage.getSdp();
        String sdpAnswer = webRtcEndpoint.processOffer(sdpOffer);

        //将sdpAnswer转发给offer发送方
        WebRTCMessage response = new WebRTCMessage();
        response.setType("answer");
        response.setSdp(sdpAnswer);

        MessageDto messageDto = new MessageDto();
        messageDto.setMessageType("RTCMsg");
        messageDto.setContent(JSON.toJSONString(response));
        Map<Integer, ChatEndpoint> connections = rooms.get(roomId);
        Integer targetUserId = webRTCMessage.getTo();
        ChatEndpoint targetEndpoint = connections.get(targetUserId);
        try {
            targetEndpoint.session.getBasicRemote().sendText(JSON.toJSONString(messageDto));
        } catch (IOException e) {
            e.printStackTrace();
        }

        webRtcEndpoint.gatherCandidates();
    }

    private void handleAnswer(WebRTCMessage webRTCMessage) {
        WebRtcEndpoint webRtcEndpoint = kurentoService.getEndpoint(roomId, webRTCMessage.getFrom());
        String sdpAnswer = webRTCMessage.getSdp();
        webRtcEndpoint.processAnswer(sdpAnswer);
    }

    private void handleCandidate(WebRTCMessage webRTCMessage) {
        JsonObject candidate = JSON.parseObject(webRTCMessage.getCandidate());
        IceCandidate iceCandidate = new IceCandidate(
                candidate.getString("candidate"),
                candidate.getString("sdpMid"),
                candidate.getInteger("sdpMLineIndex"));
        webRtcEndpoint.addIceCandidate(iceCandidate);
    }
}
