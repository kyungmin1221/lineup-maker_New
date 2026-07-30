package org.example.lineupmaker_be.domain.repo;

import org.example.lineupmaker_be.domain.model.lockerroom.LockerRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LockerRoomRepository extends JpaRepository<LockerRoom, String> {
    List<LockerRoom> findByOwnerIdOrderByUpdatedAtDesc(String ownerId);
}
