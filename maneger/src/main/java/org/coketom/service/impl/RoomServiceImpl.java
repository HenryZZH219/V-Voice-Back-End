package org.coketom.service.impl;

import org.apache.ibatis.annotations.Mapper;
import org.coketom.ServerEndPoint.ChatEndpoint;
import org.coketom.dto.room.RoomDto;
import org.coketom.entity.room.Room;
import org.coketom.mapper.RoomMapper;
import org.coketom.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RoomServiceImpl implements RoomService {

    @Autowired
    private RoomMapper roomMapper;

    private static ChatEndpoint chatEndpoint;
    @Autowired
    public void SomeOtherService(ChatEndpoint chatEndpoint) {
        this.chatEndpoint = chatEndpoint;
    }
    @Override
    public List<RoomDto> getRoomsInfo() {
        List<Room> rooms = roomMapper.getRoomsInfo();
        List<RoomDto> roomDtos = new ArrayList<>();
        Map<Integer, Map<Integer, ChatEndpoint>> roomsConnection = chatEndpoint.getRooms();
        for(Room room:rooms) {
            RoomDto roomDto = new RoomDto(room);
            Integer roomId = roomDto.getRoomId();
            Map<Integer, ChatEndpoint> connections = roomsConnection.get(roomId);
            if(connections!=null) {
                connections.forEach((userId, endPoint)->{
                    roomDto.getOnlineUsers().add(userId);
                });
            }

            roomDtos.add(roomDto);
        }
        return roomDtos;
    }
}
