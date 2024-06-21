package org.coketom.entity.message;

import lombok.Data;
import org.coketom.dto.message.MessageDto;


import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
public class UserMessage {
    Integer messageId;
    Integer roomId;
    Integer userID;
    String content;
    String messageType;
    Timestamp createdAt;

    public UserMessage(Integer roomId, Integer userID ,String content, String messageType) {
        this.roomId = roomId;
        this.userID = userID;
        this.content = content;
        this.messageType = messageType;
        this.createdAt = Timestamp.valueOf(LocalDateTime.now());
    }

    public UserMessage(MessageDto messageDto, Integer roomId, Integer userID){
        this.roomId = roomId;
        this.userID = userID;
        this.content = messageDto.getContent();
        this.messageType = messageDto.getMessageType();
        this.createdAt = Timestamp.valueOf(LocalDateTime.now());
    }
}
