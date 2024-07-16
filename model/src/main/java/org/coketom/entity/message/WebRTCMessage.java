package org.coketom.entity.message;

import lombok.Data;

@Data
public class WebRTCMessage {
    private String type;
    private Integer from;
    private Integer to;
    private String sdp;
    private String candidate;
}
