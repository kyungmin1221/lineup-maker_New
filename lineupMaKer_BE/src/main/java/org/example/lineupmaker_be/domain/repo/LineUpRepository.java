package org.example.lineupmaker_be.domain.repo;

import org.example.lineupmaker_be.domain.model.lineup.LineUpEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LineUpRepository extends JpaRepository<LineUpEntity, String> {
    List<LineUpEntity> findByOwnerIdOrderByUpdatedAtDesc(String deviceId);
}
