package org.example.lineupmaker_be.domain.repo;

import org.example.lineupmaker_be.domain.model.lockerroom.LockerRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public class LockerRoomRepository extends JpaRepository<LockerRoom, String> {
}
