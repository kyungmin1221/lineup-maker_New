package org.example.lineupmaker_be.domain.model.lineup;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.lineupmaker_be.domain.common.PlayerJson;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "lineups",
        indexes = @Index(name = "idx_lineups_owner_id", columnList = "owner_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LineUpEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private String id;

    @Column(name = "team_name", nullable = false)
    private String teamName;

    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    @Column(name = "edit_token")
    private String editToken;

    @Column(name = "show_opponents", nullable = false)
    private boolean showOpponents = true;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<PlayerJson> squad = new ArrayList<>();

    // 쿼터 여러개 (1쿼터,2쿼터 ...n )
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<QuarterJson> quarters = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;


    public static LineUpEntity create(String teamName, String ownerId,
                                       List<PlayerJson> squad, List<QuarterJson> quarters){

        LineUpEntity lineUpEntity = new LineUpEntity();
        lineUpEntity.teamName = teamName;
        lineUpEntity.ownerId = ownerId;
        lineUpEntity.squad = squad;
        lineUpEntity.quarters = quarters;
        return lineUpEntity;
    }

    public void updateTeamName(String teamName) {
        this.teamName = teamName;
    }

    public void updateSquad(List<PlayerJson> squad) {
        this.squad = squad;
    }

    public void updateQuarters(List<QuarterJson> quarters) {
        this.quarters = quarters;
    }

    public void updateShowOpponents(boolean showOpponents) {
        this.showOpponents = showOpponents;
    }

    public void updateEditToken(String editToken) {
        this.editToken = editToken;
    }
}
