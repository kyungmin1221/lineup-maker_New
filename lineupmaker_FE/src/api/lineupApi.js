import { http } from './httpClient';

// LineUpController(create)의 CreateLineUpRequest는 deviceId를 body에 포함한다 (헤더 아님)
export async function createLineup(lineupData, ownerId) {
  const body = {
    teamName: lineupData.teamName,
    deviceId: ownerId,
    squad: lineupData.squad,
    quarters: lineupData.quarters,
  };
  const response = await http.post('/api/v1/lineups', body);
  return response.id;
}

export async function getLineup(id) {
  try {
    return await http.get(`/api/v1/lineups/${id}`);
  } catch (err) {
    if (err.status === 404) return null;
    throw err;
  }
}

// LineUpController(update)의 UpdateLineUpRequest도 deviceId/editToken을 body에 포함한다 (헤더 아님)
export async function updateLineup(id, data, { deviceId, editToken } = {}) {
  const body = { ...data, deviceId, editToken };
  await http.patch(`/api/v1/lineups/${id}`, body);
}

export async function deleteLineup(id, deviceId) {
  await http.delete(`/api/v1/lineups/${id}`, { deviceId });
}

// 특정 사용자의 라인업 목록 반환 (BE가 이미 updatedAt 내림차순으로 정렬해서 줌)
export async function findMyLineups(ownerId) {
  const items = await http.get('/api/v1/lineups/me', { deviceId: ownerId });
  return items.map((item) => ({
    id: item.id,
    teamName: item.teamName || '',
    updatedAt: item.updatedAt ? new Date(item.updatedAt).getTime() : 0,
  }));
}

export async function getOrCreateEditToken(id, deviceId) {
  const response = await http.post(`/api/v1/lineups/${id}/edit-token`, undefined, { deviceId });
  return response.editToken;
}

export async function addComment(lineupId, quarterIdx, comment) {
  await http.post(`/api/v1/lineups/${lineupId}/quarters/${quarterIdx}/comments`, comment);
}

export async function deleteComment(lineupId, quarterIdx, commentIdx, deviceId) {
  await http.delete(`/api/v1/lineups/${lineupId}/quarters/${quarterIdx}/comments/${commentIdx}`, {
    deviceId,
  });
}

// Firestore onSnapshot 실시간 구독을 대체 — 서버에 SSE/WebSocket이 아직 없어서 짧은 주기로 폴링한다.
// (docs/api-spec.md "미해결 사항"에 적어둔 1차 방안. unsubscribe 함수를 반환하는 시그니처는 기존과 동일하게 유지)
export function subscribeToLineup(id, callback, intervalMs = 4000) {
  let cancelled = false;

  const tick = async () => {
    try {
      const data = await getLineup(id);
      if (!cancelled) callback(data);
    } catch (err) {
      console.error('라인업 폴링 실패:', err);
    }
  };

  tick();
  const timer = setInterval(tick, intervalMs);

  return () => {
    cancelled = true;
    clearInterval(timer);
  };
}
