package org.coketom.controller;

import org.coketom.dto.room.RoomDto;
import org.coketom.service.RoomService;
import org.coketom.vo.common.Result;
import org.coketom.vo.common.ResultCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/rooms")
public class RoomController {
    @Autowired
    private RoomService roomService;

    @GetMapping("/getRoomsInfo")
    private Result getRoomsInfo() {
        List<RoomDto> rooms = roomService.getRoomsInfo();
        return Result.build(rooms, ResultCodeEnum.SUCCESS);
    }
}
