const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080';

function getTokens() {
  return {
    accessToken: localStorage.getItem('accessToken'),
    refreshToken: localStorage.getItem('refreshToken'),
  };
}

function setTokens(tokens) {
  if (tokens?.accessToken) localStorage.setItem('accessToken', tokens.accessToken);
  if (tokens?.refreshToken) localStorage.setItem('refreshToken', tokens.refreshToken);
}

function clearTokens() {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
}

function decodeJwt(token) {
  try {
    const payload = token.split('.')[1];
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
    const json = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + c.charCodeAt(0).toString(16).padStart(2, '0'))
        .join('')
    );
    return JSON.parse(json);
  } catch {
    return null;
  }
}

function getCurrentUser() {
  const { accessToken } = getTokens();
  if (!accessToken) return null;
  const claims = decodeJwt(accessToken);
  if (!claims) return null;
  return { id: Number(claims.sub), email: claims.email, role: claims.role };
}

let refreshingPromise = null;

async function refreshAccessToken() {
  const { refreshToken } = getTokens();
  if (!refreshToken) throw new Error('리프레시 토큰이 없습니다.');
  if (!refreshingPromise) {
    refreshingPromise = fetch(`${API_BASE}/api/auth/refresh`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken }),
    })
      .then(async (res) => {
        if (!res.ok) throw new Error('토큰 재발급 실패');
        const data = await res.json();
        setTokens(data);
        return data;
      })
      .finally(() => {
        refreshingPromise = null;
      });
  }
  return refreshingPromise;
}

async function request(path, { method = 'GET', body, isForm = false, auth = true, retry = true } = {}) {
  const headers = {};
  if (!isForm) headers['Content-Type'] = 'application/json';

  if (auth) {
    const { accessToken } = getTokens();
    if (accessToken) headers['Authorization'] = `Bearer ${accessToken}`;
  }

  const res = await fetch(`${API_BASE}${path}`, {
    method,
    headers,
    body: body === undefined ? undefined : isForm ? body : JSON.stringify(body),
  });

  if (res.status === 401 && auth && retry) {
    try {
      await refreshAccessToken();
      return request(path, { method, body, isForm, auth, retry: false });
    } catch {
      clearTokens();
      window.dispatchEvent(new Event('with-dog:session-expired'));
      throw new Error('세션이 만료되었습니다. 다시 로그인해주세요.');
    }
  }

  if (res.status === 204) return null;

  const text = await res.text();
  const data = text ? JSON.parse(text) : null;

  if (!res.ok) {
    const message = data?.message || `요청 실패 (${res.status})`;
    throw new Error(message);
  }
  return data;
}

export const api = {
  // auth
  signup: (body) => request('/api/auth/signup', { method: 'POST', body, auth: false }),
  login: (body) => request('/api/auth/login', { method: 'POST', body, auth: false }),
  logout: () => request('/api/auth/logout', { method: 'POST' }),

  // user
  getUser: (id) => request(`/api/users/${id}`),
  updateUser: (id, body) => request(`/api/users/${id}`, { method: 'PUT', body }),

  // dogs
  getDogs: () => request('/api/dogs'),
  createDog: (body) => request('/api/dogs', { method: 'POST', body }),
  updateDog: (id, body) => request(`/api/dogs/${id}`, { method: 'PUT', body }),
  deleteDog: (id) => request(`/api/dogs/${id}`, { method: 'DELETE' }),

  // walks
  getWalks: (page = 0) => request(`/api/walks?page=${page}&size=15`),
  getWalk: (id) => request(`/api/walks/${id}`),
  getOngoingWalk: () => request('/api/walks/ongoing'),
  startWalk: (body) => request('/api/walks', { method: 'POST', body }),
  endWalk: (id, body) => request(`/api/walks/${id}`, { method: 'PUT', body }),

  // missions
  getWeeklyMissions: () => request('/api/missions/weekly'),
  claimMission: (missionType) => request(`/api/missions/weekly/${missionType}/claim`, { method: 'POST' }),

  // posts
  getPosts: (page = 0, keyword) =>
    request(`/api/posts?page=${page}&size=10${keyword ? `&keyword=${encodeURIComponent(keyword)}` : ''}`),
  getPost: (id) => request(`/api/posts/${id}`),
  createPost: (body) => request('/api/posts', { method: 'POST', body }),
  updatePost: (id, body) => request(`/api/posts/${id}`, { method: 'PUT', body }),
  deletePost: (id) => request(`/api/posts/${id}`, { method: 'DELETE' }),
  attachPostImage: (id, formData) => request(`/api/posts/${id}/image`, { method: 'POST', body: formData, isForm: true }),
  likePost: (id) => request(`/api/posts/${id}/like`, { method: 'POST' }),
  unlikePost: (id) => request(`/api/posts/${id}/like`, { method: 'DELETE' }),
  getLikedPosts: (page = 0) => request(`/api/posts/liked?page=${page}&size=10`),

  // comments
  getComments: (postId, page = 0) => request(`/api/posts/${postId}/comments?page=${page}&size=50`),
  createComment: (postId, body) => request(`/api/posts/${postId}/comments`, { method: 'POST', body }),
  updateComment: (postId, id, body) => request(`/api/posts/${postId}/comments/${id}`, { method: 'PUT', body }),
  deleteComment: (postId, id) => request(`/api/posts/${postId}/comments/${id}`, { method: 'DELETE' }),

  // medications
  getMedications: (dogId) => request(`/api/dogs/${dogId}/medications`),
  scanMedication: (dogId, formData) =>
    request(`/api/dogs/${dogId}/medications/scan`, { method: 'POST', body: formData, isForm: true }),
  deleteMedication: (dogId, id) => request(`/api/dogs/${dogId}/medications/${id}`, { method: 'DELETE' }),

  // notifications
  getNotifications: (page = 0) => request(`/api/notifications?page=${page}&size=20`),
  markNotificationRead: (id) => request(`/api/notifications/${id}/read`, { method: 'PATCH' }),
};

export { API_BASE, getTokens, setTokens, clearTokens, getCurrentUser };
