// Firebase Anonymous Auth(ensureSignedIn)를 대체 — 서버 왕복 없이 로컬에서 UUID를 발급/보관한다.
const DEVICE_ID_KEY = 'lineup-maker:device-id';

export function getDeviceId() {
  let id = localStorage.getItem(DEVICE_ID_KEY);
  if (!id) {
    id = crypto.randomUUID();
    localStorage.setItem(DEVICE_ID_KEY, id);
  }
  return id;
}
