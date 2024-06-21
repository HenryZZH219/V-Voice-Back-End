package org.coketom.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.coketom.entity.message.UserMessage;

@Mapper
public interface MessageMapper {
    void saveMessage(UserMessage userMessage);
}
