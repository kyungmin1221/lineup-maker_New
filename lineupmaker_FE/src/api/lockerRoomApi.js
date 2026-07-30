import { http } from './httpClient';

// LockerRoomController는 전부 X-Device-Id 헤더로 인가한다 (LineUp과 달리 body에 안 넣음)
export async function createLockerRoom(name, ownerId) {
  const response = await http.post('/api/v1/locker-rooms', { name }, { deviceId: ownerId });
  return response.id;
}

export async function getLockerRoom(id) {
  try {
    return await http.get(`/api/v1/locker-rooms/${id}`);
  } catch (err) {
    if (err.status === 404) return null;
    throw err;
  }
}

export async function updateLockerRoom(id, data, deviceId) {
  await http.patch(`/api/v1/locker-rooms/${id}`, data, { deviceId });
}

export async function deleteLockerRoom(id, deviceId) {
  await http.delete(`/api/v1/locker-rooms/${id}`, { deviceId });
}

// 주의: 목록 응답은 LockerRoomSummaryResponse라 players 배열이 아니라 playerCount(숫자)만 들어있다.
// 실제 선수 목록이 필요하면(라인업 만들 때 불러오기 등) getLockerRoom(id)로 단건을 다시 조회해야 한다.
export async function findMyLockerRooms(ownerId) {
  const items = await http.get('/api/v1/locker-rooms/me', { deviceId: ownerId });
  return items.map((item) => ({
    id: item.id,
    name: item.name || '',
    playerCount: item.playerCount ?? 0,
    updatedAt: item.updatedAt ? new Date(item.updatedAt).getTime() : 0,
  }));
}
