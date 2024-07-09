package org.coketom.dto.room;

import lombok.Data;
import org.coketom.entity.room.Room;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
@Data
public class RoomDto {
    Integer roomId;
    String roomName;
    String avatar;
    String description;
    Timestamp createdAt;
    List<Integer> onlineUsers;

    public RoomDto(Room room) {
        this.roomId = room.getRoomId();
        this.roomName = room.getRoomName();
        this.avatar = room.getAvatar();
        this.description = room.getDescription();
        this.createdAt = room.getCreatedAt();
        this.onlineUsers = new ArrayList<Integer>();
    }
}
