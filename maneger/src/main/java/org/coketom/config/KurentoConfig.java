package org.coketom.config;

import org.kurento.client.KurentoClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KurentoConfig {
    @Bean
    public KurentoClient kurentoClient() {
        return KurentoClient.create("ws://124.70.216.41:8888/kurento"); // 修改为您的Kurento Media Server地址
    }
}
