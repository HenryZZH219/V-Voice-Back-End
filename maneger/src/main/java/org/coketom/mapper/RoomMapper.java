package org.coketom.mapper;
import org.apache.ibatis.annotations.Mapper;
import org.coketom.entity.room.Room;

import java.util.List;

@Mapper
public interface RoomMapper {
    List<Room> getRoomsInfo();
}
