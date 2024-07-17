package org.coketom.webRTC;

import org.kurento.client.KurentoClient;
import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class KurentoService {

    private final Map<Integer, MediaPipeline> roomPipelines = new ConcurrentHashMap<>();
    private final Map<Integer, Map<Integer, WebRtcEndpoint>> roomEndpoints = new ConcurrentHashMap<>();
    @Autowired
    private KurentoClient kurentoClient;

//    @Autowired
//    public KurentoService(KurentoClient kurentoClient) {
//        this.kurentoClient = kurentoClient;
//    }

    public MediaPipeline getOrCreatePipeline(Integer roomId) {
        return roomPipelines.computeIfAbsent(roomId, k -> kurentoClient.createMediaPipeline());
    }

    public WebRtcEndpoint createEndpoint(Integer roomId, Integer userId) {
        MediaPipeline pipeline = getOrCreatePipeline(roomId);
        WebRtcEndpoint webRtcEndpoint = new WebRtcEndpoint.Builder(pipeline).build();
        roomEndpoints.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>()).put(userId, webRtcEndpoint);
        return webRtcEndpoint;
    }

    public WebRtcEndpoint getEndpoint(Integer roomId, Integer userId) {
        Map<Integer, WebRtcEndpoint> endpoints = roomEndpoints.get(roomId);
        if (endpoints != null) {
            return endpoints.get(userId);
        }
        return null;
    }

    public void removeEndpoint(Integer roomId, Integer userId) {
        Map<Integer, WebRtcEndpoint> endpoints = roomEndpoints.get(roomId);
        if (endpoints != null) {
            WebRtcEndpoint endpoint = endpoints.remove(userId);
            if (endpoint != null) {
                endpoint.release();
            }
            if (endpoints.isEmpty()) {
                roomEndpoints.remove(roomId);
                removePipeline(roomId);
            }
        }
    }

    private void removePipeline(Integer roomId) {
        MediaPipeline pipeline = roomPipelines.remove(roomId);
        if (pipeline != null) {
            pipeline.release();
        }
    }
}

