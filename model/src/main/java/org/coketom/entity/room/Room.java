package org.coketom.entity.room;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class Room {
    Integer roomId;
    String roomName;
    String avatar;
    String description;
    Timestamp createdAt;
}
