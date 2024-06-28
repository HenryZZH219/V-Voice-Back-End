package org.coketom.entity.message;

import lombok.Data;
import org.coketom.dto.message.MessageDto;


import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
public class UserMessage {
    Integer messageId;
    Integer roomId;
    Integer userId;
    String content;
    String messageType;
    Timestamp createdAt;
    public UserMessage() {
    }
    // 可以有一个接受各个字段的构造函数
    public UserMessage(Integer messageId, Integer roomId, Integer userId, String content, String messageType, Timestamp createdAt) {
        this.messageId = messageId;
        this.roomId = roomId;
        this.userId = userId;
        this.content = content;
        this.messageType = messageType;
        this.createdAt = createdAt;
    }
    public UserMessage(Integer roomId, Integer userID ,String content, String messageType) {
        this.roomId = roomId;
        this.userId = userID;
        this.content = content;
        this.messageType = messageType;
        this.createdAt = Timestamp.valueOf(LocalDateTime.now());
    }

    public UserMessage(MessageDto messageDto, Integer roomId, Integer userID){
        this.roomId = roomId;
        this.userId = userID;
        this.content = messageDto.getContent();
        this.messageType = messageDto.getMessageType();
        this.createdAt = Timestamp.valueOf(LocalDateTime.now());
    }
}
