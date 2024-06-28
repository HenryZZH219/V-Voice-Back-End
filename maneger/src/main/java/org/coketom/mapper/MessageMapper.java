package org.coketom.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.coketom.entity.message.UserMessage;

import java.util.List;

@Mapper
public interface MessageMapper {
    void saveMessage(UserMessage userMessage);


    List<UserMessage> getMessagesByRoomId(Integer roomId);
}
