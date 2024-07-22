package org.coketom.webRTC;

import com.alibaba.fastjson.JSON;
import com.google.gson.JsonObject;
import jakarta.websocket.Session;
import org.coketom.dto.message.MessageDto;
import org.coketom.entity.message.WebRTCMessage;
import org.coketom.service.MessageService;
import org.kurento.client.Continuation;
import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Room {
    private final ConcurrentMap<Integer, UserSession> participants = new ConcurrentHashMap<>();

    private final MediaPipeline pipeline;

    private final Integer roomId;
    public Integer gerRoomId() {
        return this.roomId;
    }

    public Collection<UserSession> getParticipants() {
        return participants.values();
    }

    private final MessageService messageService;
    public Room(Integer roomId, MediaPipeline pipeline, MessageService messageService) {
        this.roomId = roomId;
        this.pipeline = pipeline;
        this.messageService = messageService;
        System.out.printf("ROOM {%d} has been created\n", roomId);
    }

    public UserSession join(Integer userId, Session session) throws IOException {
        System.out.printf("ROOM {%d}: adding participant {%d}\n", this.roomId, userId);
        final UserSession participant = new UserSession(userId, this.roomId, this.pipeline, session, this.messageService);
        joinRoom(participant);
        participants.put(participant.getUserId(), participant);
//        sendParticipantNames(participant);
        //这个时候应该发个消息通知可以连接其它客户端了 暂时还没写 想合并了再说

        return participant;
    }

    private Collection<Integer> joinRoom(UserSession newParticipant) {

        WebRTCMessage response = new WebRTCMessage();
        response.setType("newParticipantArrived");
        response.setFrom(newParticipant.getUserId());

        final List<Integer> participantsList = new ArrayList<>(participants.values().size());
        System.out.printf("ROOM {%d}: notifying other participants of new participant {%d}\n", roomId,
                newParticipant.getUserId());

        for (final UserSession participant : participants.values()) {
            participant.sendRTCMessage(response);
            participantsList.add(participant.getUserId());
        }

        return participantsList;
    }

    public void close() {
        for (final UserSession user : participants.values()) {
            user.close();
        }

        participants.clear();

        pipeline.release();

        System.out.printf("webrtc Room {%d} closed\n", this.roomId);
    }

    public void leave(UserSession user) {
        this.removeParticipant(user.getUserId());
        user.close();
    }

    private void removeParticipant(Integer userId){
        participants.remove(userId);

        System.out.printf("ROOM {%d}: notifying all users that {%d} is leaving the room\n", this.roomId, userId);

        WebRTCMessage response = new WebRTCMessage();
        response.setType("participantLeft");
        response.setFrom(userId);


        for (final UserSession participant : participants.values()) {
            participant.cancelVideoFrom(userId);
            participant.sendRTCMessage(response);
        }


    }


}
