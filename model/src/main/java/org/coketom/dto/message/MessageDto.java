package org.coketom.dto.message;

import lombok.Data;


@Data
public class MessageDto {
    String token;
    String content;
    String messageType;
}
