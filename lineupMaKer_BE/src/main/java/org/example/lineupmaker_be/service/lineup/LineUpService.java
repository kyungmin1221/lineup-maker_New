package org.example.lineupmaker_be.service.lineup;

import lombok.RequiredArgsConstructor;
import org.example.lineupmaker_be.domain.common.exception.ForbiddenException;
import org.example.lineupmaker_be.domain.common.exception.NotFoundException;
import org.example.lineupmaker_be.domain.model.lineup.LineUpEntity;
import org.example.lineupmaker_be.domain.model.lineup.QuarterJson;
import org.example.lineupmaker_be.domain.repo.LineUpRepository;
import org.example.lineupmaker_be.web.dto.lineup.*;
import org.springframework.stereotype.Service;

import java.util.List;

// 규칙: domain에만 의존한다. web(Controller)을 참조하지 않는다.
// 이 클래스가 하는 일: 트랜잭션 경계 안에서 리포지토리를 호출하고, 소유권 검증(OwnershipValidator)을 거쳐
// 엔티티 <-> 응답 DTO 변환까지 책임진다. 컨트롤러는 여기 있는 메서드를 호출만 한다.
@Service
@RequiredArgsConstructor
public class LineUpService {

    private final LineUpRepository lineUpRepository;
    // TODO: 라인업 생성
    // 1. LineUpEntity를 새로 만든다 (teamName/squad/quarters는 request 값으로 초기화, ownerId는 deviceId로 설정)
    //    - 엔티티에 세터가 없으므로 정적 팩토리 메서드(예: LineUpEntity.create(...))를 엔티티에 추가하는 걸 고려
    // 2. lineUpRepository.save(entity) 호출 (id는 @GeneratedValue(UUID)가 자동 채움)
    // 3. 저장된 엔티티를 LineUpResponse로 변환해서 반환
    // 4. 쓰기 메서드이므로 @Transactional 고려
    public LineUpResponse create(String deviceId, CreateLineUpRequest request) {
        LineUpEntity lineUpEntity = LineUpEntity.create(deviceId, request.teamName(), request.squad(), request.quarters());
        LineUpEntity savedLineUp = lineUpRepository.save(lineUpEntity);
        return LineUpResponse.from(savedLineUp);
    }

    // TODO: 라인업 단건 조회 (인증 불필요 - 누구나 조회 가능, ViewPage/CreatePage 진입 시 사용)
    // 1. lineUpRepository.findById(id) 호출
    // 2. 없으면 NotFoundException을 던진다 (domain.common.exception에 만들어 사용 - GlobalExceptionHandler가 404로 변환)
    // 3. 있으면 LineUpResponse로 변환해서 반환
    //    - 주의: editToken 필드는 응답에 절대 포함하지 않는다 (api-spec.md 참고, 소유자가 명시적으로 발급 요청했을 때만 노출)
    public LineUpResponse get(String id) {
        LineUpEntity lineUpEntity = lineUpRepository.findById(id)
                .orElseThrow(() -> NotFoundException.lineup(id));
        return LineUpResponse.from(lineUpEntity);
    }

    // TODO: OG 메타태그용 요약 조회 (Vercel의 api/lineup-og.js가 호출할 공개 엔드포인트)
    // 1. lineUpRepository.findById(id), 없으면 NotFoundException
    // 2. teamName만 꺼내서 LineUpOgSummaryResponse로 반환
    public LineUpOgSummaryResponse getSummary(String id) {
        LineUpEntity lineUpEntity = lineUpRepository.findById(id)
                .orElseThrow(() -> NotFoundException.lineup(id));
        return new LineUpOgSummaryResponse(lineUpEntity.getTeamName());
    }

    // TODO: 부분 수정 (FE 자동저장이 1초 debounce 후 호출)
    // 1. lineUpRepository.findById(id), 없으면 NotFoundException
    // 2. 소유권 검증: ownerId==deviceId 이거나, editToken이 entity의 editToken과 일치해야 함
    //    (둘 다 아니면 ForbiddenException) - OwnershipValidator로 분리해서 재사용하는 걸 고려
    // 3. request에서 null이 아닌 필드만 엔티티에 반영 (teamName/squad/quarters/showOpponents 전부 optional)
    //    - 엔티티에 도메인 메서드(예: entity.applyUpdate(request))를 추가해서 처리하는 걸 고려 (세터 노출 지양)
    // 4. updatedAt은 @UpdateTimestamp가 자동 갱신하므로 직접 건드릴 필요 없음
    // 5. 저장 후 LineUpResponse로 변환해서 반환
    // 6. 쓰기 메서드이므로 @Transactional 고려
    public LineUpResponse update(String id, String deviceId, String editToken,
                                 UpdateLineUpRequest request) {
        LineUpEntity lineUpEntity = lineUpRepository.findById(id)
                .orElseThrow(() -> NotFoundException.lineup(id));

        // 소유권 검증
        if (!lineUpEntity.getOwnerId().equals(deviceId)
                && !lineUpEntity.getEditToken().equals(editToken)) {
            throw ForbiddenException.notOwnerOrEditToken();
        }

        // request에서 null이 아닌 필드만 엔티티에 반영
        if (request.teamName() != null) {
            lineUpEntity.updateTeamName(request.teamName());
        }
        if (request.squad() != null) {
            lineUpEntity.updateSquad(request.squad());
        }
        if (request.quarters() != null) {
            lineUpEntity.updateQuarters(request.quarters());
        }
        if (request.showOpponents() != null) {
            lineUpEntity.updateShowOpponents(request.showOpponents());
        }

        LineUpEntity updatedLineUp = lineUpRepository.save(lineUpEntity);
        return LineUpResponse.from(updatedLineUp);
    }

    // TODO: 라인업 삭제 (소유자만 가능)
    // 1. lineUpRepository.findById(id), 없으면 NotFoundException
    // 2. 소유권 검증 (deviceId == entity.ownerId만 허용, editToken으로는 삭제 불가) - 아니면 ForbiddenException
    // 3. lineUpRepository.delete(entity)
    public void delete(String id, String deviceId) {
        LineUpEntity lineUpEntity = lineUpRepository.findById(id)
                .orElseThrow(() -> NotFoundException.lineup(id));

        // 소유권 검증
        if (!lineUpEntity.getOwnerId().equals(deviceId)) {
            throw ForbiddenException.notOwner();
        }

        lineUpRepository.delete(lineUpEntity);
    }

    // TODO: 내 라인업 목록 조회 (최근 수정순)
    // 1. LineUpRepository에 findByOwnerIdOrderByUpdatedAtDesc(String ownerId) 같은 쿼리 메서드를 추가
    //    (Spring Data JPA가 메서드 이름으로 자동 구현 - 직접 쿼리 짤 필요 없음)
    // 2. 결과 리스트를 List<LineUpSummaryResponse>(id, teamName, updatedAt)로 변환해서 반환
    public List<LineUpSummaryResponse> findMine(String deviceId) {
        List<LineUpEntity> lineUps = lineUpRepository.findByOwnerIdOrderByUpdatedAtDesc(deviceId);
        return lineUps.stream()
                .map(LineUpSummaryResponse::from)
                .toList();
    }

    // TODO: 편집 토큰 발급/조회 (소유자만 가능)
    // 1. lineUpRepository.findById(id), 없으면 NotFoundException
    // 2. 소유권 검증 (소유자만 허용)
    // 3. entity.getEditToken()이 이미 있으면 그대로 EditTokenResponse에 담아 반환
    // 4. 없으면 새 토큰 생성 (SecureRandom 기반 랜덤 hex 문자열 - EditTokenGenerator 유틸을 만들어 사용하는 걸 고려)
    // 5. 생성한 토큰을 엔티티에 반영하고 저장한 뒤 EditTokenResponse로 반환
    public EditTokenResponse getOrCreateEditToken(String id, String deviceId) {
        throw new UnsupportedOperationException("TODO");
    }

    // TODO: 댓글 추가 (인증 불필요 - 뷰어도 작성 가능, Comments.jsx와 동일 정책)
    // 1. lineUpRepository.findById(id), 없으면 NotFoundException
    // 2. entity.getQuarters()에서 quarterIdx가 유효 범위인지 확인 (범위 밖이면 예외 - 400 혹은 404 중 선택)
    // 3. 해당 QuarterJson.comments()에 새 CommentJson(name, text, createdAt=현재시각) 추가
    //    - QuarterJson/CommentJson은 record(불변)라 리스트/레코드를 새로 만들어서 quarters 전체를 교체해야 함
    // 4. 저장 후 방금 추가한 댓글을 CommentResponse로 변환해서 반환
    public CommentResponse addComment(String id, int quarterIdx, CommentRequest request) {
        throw new UnsupportedOperationException("TODO");
    }

    // TODO: 댓글 삭제 (소유자만 가능, ViewPage.jsx의 isOwner 정책과 동일)
    // 1. lineUpRepository.findById(id), 없으면 NotFoundException
    // 2. 소유권 검증 (소유자만 허용)
    // 3. quarterIdx/commentIdx가 유효 범위인지 확인
    // 4. 해당 인덱스의 댓글을 제외한 새 리스트로 quarters를 교체하고 저장
    public void deleteComment(String id, int quarterIdx, int commentIdx, String deviceId) {
        LineUpEntity lineUpEntity = lineUpRepository.findById(id)
                .orElseThrow(() -> NotFoundException.lineup(id));

        // 소유권검증
        if (!lineUpEntity.getOwnerId().equals(deviceId)) {
            throw ForbiddenException.notOwner();
        }

        // quarterIdx와 commentIdx 유효성 검증
        if (quarterIdx < 0 || quarterIdx >= lineUpEntity.getQuarters().size()) {
            throw new IllegalArgumentException("Invalid quarter index: " + quarterIdx);
        }

        // 해당 인덱스의 댓글을 제외한 새 리스트로 quarters를 교체하고 저장
        QuarterJson quarter = lineUpEntity.getQuarters().get(quarterIdx);
        if (commentIdx < 0 || commentIdx >= quarter.comments().size()) {
            throw new IllegalArgumentException("Invalid comment index: " + commentIdx);
        }
    }

}
