package org.coketom.controller;

import org.coketom.entity.message.UserMessage;
import org.coketom.service.MessageService;
import org.coketom.vo.common.Result;
import org.coketom.vo.common.ResultCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/getMessage")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping("/room/{roomId}")
    public Result getMessagesByRoomId(@PathVariable Integer roomId) {
        List<UserMessage> messages = messageService.getMessagesByRoomId(roomId);
        return Result.build(messages, ResultCodeEnum.SUCCESS);
    }

}
