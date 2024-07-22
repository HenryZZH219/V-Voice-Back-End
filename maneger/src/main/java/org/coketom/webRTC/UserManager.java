package org.coketom.webRTC;

import jakarta.websocket.Session;
import org.kurento.client.MediaPipeline;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserManager {

    private final ConcurrentHashMap<Integer, UserSession> usersBySessionId = new ConcurrentHashMap<>();
    public UserSession getByUserId(Integer userId) {
        return usersBySessionId.get(userId);
    }

    public void register(UserSession user){
        usersBySessionId.put(user.getUserId(), user);
    }
}
