package org.coketom.webRTC;

import com.alibaba.fastjson.JSON;
import jakarta.websocket.Session;
import lombok.Getter;
import org.coketom.ServerEndPoint.ChatEndpoint;
import org.coketom.dto.message.MessageDto;
import org.coketom.entity.message.WebRTCMessage;

import org.coketom.service.MessageService;
import org.kurento.client.Continuation;
import org.kurento.client.IceCandidate;
import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class UserSession {

    @Getter
    private final Integer userId;
    private final MediaPipeline pipeline;

    private final Integer roomId;
    private final WebRtcEndpoint outgoingMedia;

    private final Session session;
    private final ConcurrentMap<Integer, WebRtcEndpoint> incomingMedia = new ConcurrentHashMap<>();

    private final MessageService messageService;

    public UserSession(Integer userId, Integer roomId, MediaPipeline pipeline, Session session, MessageService messageService) {
        this.pipeline = pipeline;
        this.userId = userId;
        this.session = session;
        this.roomId = roomId;
        this.outgoingMedia = new WebRtcEndpoint.Builder(pipeline).build();
        this.messageService = messageService;

        this.outgoingMedia.addIceCandidateFoundListener(event -> {
            WebRTCMessage response = new WebRTCMessage();
            response.setType("iceCandidate");
//            response.setCandidate(event.getCandidate().getCandidate());
            IceCandidate candidate = event.getCandidate();
            // 确保 sdpMid 和 sdpMLineIndex 有值
            String sdpMid = candidate.getSdpMid() != null ? candidate.getSdpMid() : "0";
            int sdpMLineIndex = candidate.getSdpMLineIndex() != -1 ? candidate.getSdpMLineIndex() : 0;

            // 构建 ICE 候选者信息 JSON 对象
            String iceCandidateMessage = String.format("{\"type\": \"iceCandidate\", \"candidate\": {\"candidate\": \"%s\", \"sdpMid\": \"%s\", \"sdpMLineIndex\": %d}}",
                    candidate.getCandidate(), sdpMid, sdpMLineIndex);
            response.setCandidate(iceCandidateMessage);
            sendRTCMessage(response);

//            synchronized (lock) {
//                try {
//                    session.getBasicRemote().sendText(JSON.toJSONString(messageDto));
//                }catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }
        });
    }

    public WebRtcEndpoint getOutgoingWebRtcPeer() {
        return outgoingMedia;
    }
    public void receiveVideoFrom(UserSession sender, String sdpOffer) throws IOException {
        System.out.printf("USER {%d}: connecting with {%d} in room {}\n", this.userId, sender.getUserId());
        System.out.printf("USER {%d}: SdpOffer for {%d} is {%s}\n", this.userId, sender.getUserId(), sdpOffer);

        final String sdpAnswer = this.getEndpointForUser(sender).processOffer(sdpOffer);

        System.out.printf("USER {%d}: SdpAnswer for {%d} is {%s}\n", this.userId, sender.getUserId(), sdpAnswer);
        this.sendSdpAnswer(sdpAnswer);
        System.out.print("gather candidates\n");
        this.getEndpointForUser(sender).gatherCandidates();
    }

    private void sendSdpAnswer(String sdpAnswer) {
        //将sdpAnswer转发给offer发送方
        WebRTCMessage response = new WebRTCMessage();
        response.setType("answer");
        response.setSdp(sdpAnswer);
        sendRTCMessage(response);

//        try {
//            session.getBasicRemote().sendText(JSON.toJSONString(messageDto));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private WebRtcEndpoint getEndpointForUser(final UserSession sender) {
        if (sender.getUserId().equals(userId)) {
            System.out.printf("PARTICIPANT {%d}: configuring loopback\n", this.userId);
            return outgoingMedia;
        }

        System.out.printf("PARTICIPANT {%d}: receiving video from {%d}\n", this.userId, sender.getUserId());

        WebRtcEndpoint incoming = incomingMedia.get(sender.getUserId());
        if (incoming == null) {
            System.out.printf("PARTICIPANT {%d}: creating new endpoint for {%d}\n", this.userId, sender.getUserId());
            incoming = new WebRtcEndpoint.Builder(pipeline).build();

            incoming.addIceCandidateFoundListener(event -> {
                WebRTCMessage response = new WebRTCMessage();
                response.setType("iceCandidate");
//                response.setCandidate(event.getCandidate().getCandidate());

                IceCandidate candidate = event.getCandidate();
                // 确保 sdpMid 和 sdpMLineIndex 有值
                String sdpMid = candidate.getSdpMid() != null ? candidate.getSdpMid() : "0";
                int sdpMLineIndex = candidate.getSdpMLineIndex() != -1 ? candidate.getSdpMLineIndex() : 0;

                // 构建 ICE 候选者信息 JSON 对象
                String iceCandidateMessage = String.format("{\"type\": \"iceCandidate\", \"candidate\": {\"candidate\": \"%s\", \"sdpMid\": \"%s\", \"sdpMLineIndex\": %d}}",
                        candidate.getCandidate(), sdpMid, sdpMLineIndex);
                response.setCandidate(iceCandidateMessage);
                sendRTCMessage(response);
            });

            incomingMedia.put(sender.getUserId(), incoming);
        }

        System.out.printf("PARTICIPANT {%d}: obtained endpoint for {%d}\n", this.userId, sender.getUserId());
        sender.getOutgoingWebRtcPeer().connect(incoming);

        return incoming;
    }

    public void addCandidate(IceCandidate candidate, Integer userId) {
        if (this.userId.equals(userId)) {
            outgoingMedia.addIceCandidate(candidate);
        } else {
            WebRtcEndpoint webRtc = incomingMedia.get(userId);
            if (webRtc != null) {
                webRtc.addIceCandidate(candidate);
            }
        }
    }

    public void sendRTCMessage(WebRTCMessage webRTCMessage) {
//        System.out.println(messageDto);
        webRTCMessage.setTo(this.userId);
        MessageDto messageDto = new MessageDto();
        messageDto.setMessageType("RTCMsg");
        messageDto.setContent(JSON.toJSONString(webRTCMessage));
        messageService.sendRTCMessage(messageDto, session);
//        try {
//            session.getBasicRemote().sendText(JSON.toJSONString(messageDto));
//        }catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }

    public void close(){
        System.out.printf("PARTICIPANT {%d}: Releasing resources\n", this.userId);
        for (final Integer remoteParticipantName : incomingMedia.keySet()) {
            final WebRtcEndpoint ep = this.incomingMedia.get(remoteParticipantName);
            ep.release();
        }

        outgoingMedia.release();
    }

    public void cancelVideoFrom(Integer userId) {
        final WebRtcEndpoint incoming = incomingMedia.remove(userId);
        incoming.release();
    }
}
