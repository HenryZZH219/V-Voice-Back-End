package org.coketom.webRTC;

import com.alibaba.fastjson.JSON;
import jakarta.websocket.Session;
import org.coketom.ServerEndPoint.ChatEndpoint;
import org.coketom.dto.message.MessageDto;
import org.coketom.entity.message.WebRTCMessage;

import org.kurento.client.Continuation;
import org.kurento.client.IceCandidate;
import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;


import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class UserSession {

    private final Integer userId;
    private final MediaPipeline pipeline;

    private final Integer roomId;
    private final WebRtcEndpoint outgoingMedia;

    private final Session session;
    private final ConcurrentMap<Integer, WebRtcEndpoint> incomingMedia = new ConcurrentHashMap<>();


    public UserSession(Integer userId, Integer roomId, MediaPipeline pipeline, Session session) {
        this.pipeline = pipeline;
        this.userId = userId;
        this.session = session;
        this.roomId = roomId;
        this.outgoingMedia = new WebRtcEndpoint.Builder(pipeline).build();

        this.outgoingMedia.addIceCandidateFoundListener(event -> {
            WebRTCMessage response = new WebRTCMessage();
            response.setType("iceCandidate");
            response.setCandidate(event.getCandidate().getCandidate());
            MessageDto messageDto = new MessageDto();
            messageDto.setMessageType("RTCMsg");
            messageDto.setContent(JSON.toJSONString(response));
            try {
                session.getBasicRemote().sendText(JSON.toJSONString(messageDto));
            }catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public Integer getUserId() {
        return this.userId;
    }

    public WebRtcEndpoint getOutgoingWebRtcPeer() {
        return outgoingMedia;
    }
    public void receiveVideoFrom(UserSession sender, String sdpOffer) throws IOException {
        System.out.printf("USER {%d}: connecting with {%d} in room {}", this.userId, sender.getUserId());
        System.out.printf("USER {%d}: SdpOffer for {%d} is {%s}", this.userId, sender.getUserId(), sdpOffer);

        final String sdpAnswer = this.getEndpointForUser(sender).processOffer(sdpOffer);

        System.out.printf("USER {%d}: SdpAnswer for {%d} is {%s}", this.userId, sender.getUserId(), sdpAnswer);
        this.sendSdpAnswer(sdpAnswer);
        System.out.printf("gather candidates");
        this.getEndpointForUser(sender).gatherCandidates();
    }

    private void sendSdpAnswer(String sdpAnswer) {
        //将sdpAnswer转发给offer发送方
        WebRTCMessage response = new WebRTCMessage();
        response.setType("answer");
        response.setSdp(sdpAnswer);
        MessageDto messageDto = new MessageDto();
        messageDto.setMessageType("RTCMsg");
        messageDto.setContent(JSON.toJSONString(response));
        try {
            session.getBasicRemote().sendText(JSON.toJSONString(messageDto));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private WebRtcEndpoint getEndpointForUser(final UserSession sender) {
        if (sender.getUserId().equals(userId)) {
            System.out.printf("PARTICIPANT {%d}: configuring loopback", this.userId);
            return outgoingMedia;
        }

        System.out.printf("PARTICIPANT {%d}: receiving video from {%d}", this.userId, sender.getUserId());

        WebRtcEndpoint incoming = incomingMedia.get(sender.getUserId());
        if (incoming == null) {
            System.out.printf("PARTICIPANT {%d}: creating new endpoint for {%d}", this.userId, sender.getUserId());
            incoming = new WebRtcEndpoint.Builder(pipeline).build();

            incoming.addIceCandidateFoundListener(event -> {
                WebRTCMessage response = new WebRTCMessage();
                response.setType("iceCandidate");
                response.setCandidate(event.getCandidate().getCandidate());
                MessageDto messageDto = new MessageDto();
                messageDto.setMessageType("RTCMsg");
                messageDto.setContent(JSON.toJSONString(response));
                sendMessage(messageDto);
            });

            incomingMedia.put(sender.getUserId(), incoming);
        }

        System.out.printf("PARTICIPANT {%d}: obtained endpoint for {%d}", this.userId, sender.getUserId());
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

    public void sendMessage(MessageDto messageDto) {
        try {
            session.getBasicRemote().sendText(JSON.toJSONString(messageDto));
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void close(){
        System.out.printf("PARTICIPANT {}: Releasing resources", this.userId);
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
