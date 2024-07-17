package org.coketom.dto.message;

import lombok.Data;
import org.coketom.entity.message.WebRTCMessage;


@Data
public class MessageDto {
    String token;
    String content;
    String messageType;

}
