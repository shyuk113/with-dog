import { useEffect, useState } from 'react';
import { api } from '../api.js';
import { formatDateTime } from '../utils.js';

const TYPE_LABEL = {
  NEW_COMMENT: '💬 새 댓글',
  NEW_REPLY: '↩️ 새 답글',
};

export default function NotificationsPage() {
  const [notifications, setNotifications] = useState([]);
  const [error, setError] = useState('');

  const load = async () => {
    try {
      const page = await api.getNotifications(0);
      setNotifications(page.content);
    } catch (err) {
      setError(err.message);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const markRead = async (id) => {
    try {
      await api.markNotificationRead(id);
      setNotifications((list) => list.map((n) => (n.id === id ? { ...n, read: true } : n)));
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <main>
      {error && <div className="error">{error}</div>}
      <div className="card">
        <h2>알림</h2>
        {notifications.length === 0 && <p className="muted">알림이 없습니다.</p>}
        {notifications.map((n) => (
          <div className="list-item" key={n.id} style={{ opacity: n.read ? 0.55 : 1 }}>
            <div className="row" style={{ justifyContent: 'space-between' }}>
              <div>
                <div>{TYPE_LABEL[n.type] ?? n.type}</div>
                <strong>{n.message}</strong>
                <div className="muted">{formatDateTime(n.createdAt)}</div>
              </div>
              {!n.read && <button className="btn small secondary" onClick={() => markRead(n.id)}>읽음</button>}
            </div>
          </div>
        ))}
      </div>
    </main>
  );
}
