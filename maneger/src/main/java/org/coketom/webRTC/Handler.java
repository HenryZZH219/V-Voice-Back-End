package org.coketom.webRTC;

import com.alibaba.fastjson.JSON;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.websocket.Session;
import org.coketom.dto.message.MessageDto;
import org.coketom.entity.message.WebRTCMessage;
import org.coketom.exception.TomException;
import org.coketom.vo.common.ResultCodeEnum;
import org.kurento.client.IceCandidate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Service
public class Handler {

    @Autowired
    UserManager userManager;

    @Autowired
    RoomManager roomManager;
    public void handleWebRTCMessage(Session session, MessageDto messageDto) throws Exception {
        // 解析 WebRTC 信令消息
        WebRTCMessage webRTCMessage = JSON.parseObject(messageDto.getContent(), WebRTCMessage.class);
        String messageType = webRTCMessage.getType();


        final UserSession user = userManager.getByUserId(webRTCMessage.getFrom());

        if (user != null) {
            System.out.printf("Incoming message from user '{%d}': {%s}\n", user.getUserId(), webRTCMessage);
        } else {
            System.out.printf("Incoming message from new user: {%s}\n", webRTCMessage);
        }

        switch (messageType) {
            case "joinRoom":
                joinRoom(webRTCMessage, session);
                break;
            case "receiveVideoFrom":
                if (user == null) {
                    throw new TomException(ResultCodeEnum.DATA_ERROR);
                }
                final Integer senderUserId = webRTCMessage.getTo();
                final UserSession sender = userManager.getByUserId(senderUserId);
                final String sdpOffer = webRTCMessage.getSdp();
                user.receiveVideoFrom(sender, sdpOffer);
                break;

                /* 放在websocket里触发
            case "leaveRoom":
                leaveRoom(user.getUserId(), webRTCMessage.getRoomId());
                break;*/
            case "onIceCandidate":
                JsonObject candidate = JsonParser.parseString(webRTCMessage.getCandidate()).getAsJsonObject();
                if (user != null) {
                    IceCandidate cand = new IceCandidate(candidate.get("candidate").getAsString(),
                            candidate.get("sdpMid").getAsString(), candidate.get("sdpMLineIndex").getAsInt());
                    user.addCandidate(cand, webRTCMessage.getTo());
                }
                break;
            default:
                break;
        }
    }

    private void joinRoom(WebRTCMessage webRTCMessage, Session session) throws IOException {
        final Integer roomId = webRTCMessage.getRoomId();
        final Integer userId = webRTCMessage.getFrom();
        System.out.printf("PARTICIPANT {%d}: trying to join room {%d}\n", userId, roomId);

        Room room = roomManager.getRoom(roomId);
        final UserSession user = room.join(userId, session);
        userManager.register(user);
    }

    public void leaveRoom(Integer userId, Integer roomId){
        final UserSession user = userManager.getByUserId(userId);
        final Room room = roomManager.getRoom(roomId);
        room.leave(user);
        if (room.getParticipants().isEmpty()) {
            roomManager.removeRoom(room);
        }
    }
}
