const BASE_URL = import.meta.env.VITE_API_BASE_URL;

async function request(path, { method = 'GET', body, deviceId, editToken } = {}) {
  const headers = {};
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  if (deviceId) headers['X-Device-Id'] = deviceId;
  if (editToken) headers['X-Edit-Token'] = editToken;

  const res = await fetch(`${BASE_URL}${path}`, {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  if (!res.ok) {
    let errorBody = null;
    try {
      errorBody = await res.json();
    } catch {
      // 에러 응답이 JSON이 아닐 수도 있음 (GlobalExceptionHandler 붙기 전에는 스프링 기본 에러 포맷)
    }
    const err = new Error(errorBody?.message || `요청 실패 (${res.status})`);
    err.status = res.status;
    err.code = errorBody?.code;
    throw err;
  }

  if (res.status === 204) return null;
  return res.json();
}

export const http = {
  get: (path, opts) => request(path, { ...opts, method: 'GET' }),
  post: (path, body, opts) => request(path, { ...opts, method: 'POST', body }),
  patch: (path, body, opts) => request(path, { ...opts, method: 'PATCH', body }),
  delete: (path, opts) => request(path, { ...opts, method: 'DELETE' }),
};
