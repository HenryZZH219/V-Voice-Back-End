package org.coketom.webRTC;

import org.coketom.service.MessageService;
import org.kurento.client.KurentoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class RoomManager {

    @Autowired
    private KurentoClient kurentoClient;

    @Autowired
    private MessageService messageService;
    private final ConcurrentMap<Integer, Room> rooms = new ConcurrentHashMap<>();

    public Room getRoom(Integer roomId) {
        Room room = rooms.get(roomId);

        if (room == null) {
            System.out.printf("Room {%d} not existent. Will create now!\n", roomId);
            room = new Room(roomId, kurentoClient.createMediaPipeline(), messageService);
            rooms.put(roomId, room);
        }
        return room;
    }

    public void removeRoom(Room room) {
        this.rooms.remove(room.gerRoomId());
        room.close();
        System.out.printf("Room {%d} removed and closed\n", room.gerRoomId());
    }
}
