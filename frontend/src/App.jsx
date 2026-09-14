import { useState } from 'react';
import { useAuth } from './AuthContext.jsx';
import AuthPage from './pages/AuthPage.jsx';
import DogsPage from './pages/DogsPage.jsx';
import WalkPage from './pages/WalkPage.jsx';
import MissionsPage from './pages/MissionsPage.jsx';
import PostsPage from './pages/PostsPage.jsx';
import NotificationsPage from './pages/NotificationsPage.jsx';
import MedicationsPage from './pages/MedicationsPage.jsx';

const TABS = [
  { key: 'walk', label: '🐕 산책', Component: WalkPage },
  { key: 'dogs', label: '🐾 강아지', Component: DogsPage },
  { key: 'mission', label: '🏆 미션', Component: MissionsPage },
  { key: 'posts', label: '📷 게시판', Component: PostsPage },
  { key: 'medication', label: '💊 복용기록', Component: MedicationsPage },
  { key: 'notification', label: '🔔 알림', Component: NotificationsPage },
];

export default function App() {
  const { user, logout } = useAuth();
  const [tab, setTab] = useState('walk');

  if (!user) return <AuthPage />;

  const Active = TABS.find((t) => t.key === tab)?.Component ?? WalkPage;

  return (
    <>
      <header className="app-header">
        <h1>🐶 with-dog</h1>
        <div className="top-links">
          <span>{user.email}</span>
          <a onClick={logout}>로그아웃</a>
        </div>
      </header>
      <nav className="tabs">
        {TABS.map((t) => (
          <button key={t.key} className={tab === t.key ? 'active' : ''} onClick={() => setTab(t.key)}>
            {t.label}
          </button>
        ))}
      </nav>
      <Active />
    </>
  );
}
