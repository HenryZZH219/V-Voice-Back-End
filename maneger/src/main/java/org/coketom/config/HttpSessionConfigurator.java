package org.coketom.config;

import jakarta.servlet.http.HttpSession;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;

import java.util.List;
import java.util.Map;

public class HttpSessionConfigurator extends ServerEndpointConfig.Configurator {
    @Override
    public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response) {
        Map<String, List<String>> params = request.getParameterMap();
        if (params.containsKey("token")) {
            List<String> tokenList = params.get("token");
            if (!tokenList.isEmpty()) {
                String token = tokenList.get(0);
                config.getUserProperties().put("token", token);
            }
        }
    }
}