package org.coketom.controller;

import com.github.pagehelper.PageInfo;
import org.coketom.entity.message.UserMessage;
import org.coketom.service.MessageService;
import org.coketom.vo.common.Result;
import org.coketom.vo.common.ResultCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/roomByPage/{roomId}")
    public Result getMessagesByRoomIdByPage(@PathVariable Integer roomId, @RequestParam int page, @RequestParam int size) {
        PageInfo<UserMessage> messages = messageService.getMessagesByRoomIdByPage(roomId, page, size);
        return Result.build(messages, ResultCodeEnum.SUCCESS);
    }



}
