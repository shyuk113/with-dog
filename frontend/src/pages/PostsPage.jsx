import { useEffect, useState } from 'react';
import { api } from '../api.js';
import PostDetail from '../components/PostDetail.jsx';

export default function PostsPage() {
  const [view, setView] = useState('all'); // 'all' | 'liked'
  const [posts, setPosts] = useState([]);
  const [error, setError] = useState('');
  const [selectedId, setSelectedId] = useState(null);
  const [showCreate, setShowCreate] = useState(false);

  const load = async (v = view) => {
    try {
      const page = v === 'liked' ? await api.getLikedPosts(0) : await api.getPosts(0);
      setPosts(page.content);
    } catch (err) {
      setError(err.message);
    }
  };

  useEffect(() => {
    load(view);
  }, [view]);

  if (selectedId) {
    return (
      <PostDetail
        postId={selectedId}
        onBack={() => {
          setSelectedId(null);
          load(view);
        }}
      />
    );
  }

  return (
    <main>
      {error && <div className="error">{error}</div>}

      <div className="row" style={{ justifyContent: 'space-between', marginBottom: 12 }}>
        <div className="row">
          <button className={`btn small ${view === 'all' ? '' : 'secondary'}`} onClick={() => setView('all')}>전체 글</button>
          <button className={`btn small ${view === 'liked' ? '' : 'secondary'}`} onClick={() => setView('liked')}>내가 좋아요한 글</button>
        </div>
        <button className="btn small secondary" onClick={() => setShowCreate((v) => !v)}>
          {showCreate ? '취소' : '글쓰기'}
        </button>
      </div>

      {showCreate && (
        <CreatePostForm
          onCreated={() => {
            setShowCreate(false);
            load(view);
          }}
        />
      )}

      <div className="card">
        {posts.length === 0 && <p className="muted">게시글이 없습니다.</p>}
        {posts.map((p) => (
          <div className="list-item" key={p.id} style={{ cursor: 'pointer' }} onClick={() => setSelectedId(p.id)}>
            {p.imageUrl && <img className="thumb" src={`http://localhost:8080${p.imageUrl}`} alt="" style={{ maxHeight: 120 }} />}
            <div className="row" style={{ justifyContent: 'space-between' }}>
              <strong>{p.title}</strong>
              {p.hasRoute && <span className="badge">코스 공유</span>}
            </div>
            <div className="muted">❤ {p.likeCount}{p.likedByMe ? ' (좋아요함)' : ''}</div>
          </div>
        ))}
      </div>
    </main>
  );
}

function CreatePostForm({ onCreated }) {
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [walks, setWalks] = useState([]);
  const [selectedWalkId, setSelectedWalkId] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    (async () => {
      try {
        const page = await api.getWalks(0);
        setWalks(page.content.filter((w) => w.endedAt));
      } catch {
        // 산책 목록 로딩 실패는 조용히 무시 (코스 공유 없이도 글쓰기는 가능해야 함)
      }
    })();
  }, []);

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      let route = null;
      if (selectedWalkId) {
        const detail = await api.getWalk(Number(selectedWalkId));
        if (!detail.routePoints || detail.routePoints.length === 0) {
          throw new Error('선택한 산책에는 저장된 좌표가 없어 코스를 첨부할 수 없습니다.');
        }
        route = {
          courseName: `${detail.dog?.name ?? '강아지'}와의 산책 코스`,
          distanceKm: detail.distanceKm,
          durationMinutes: detail.durationMinute,
          route: detail.routePoints.map((p) => ({ lat: p.lat, lon: p.lon })),
        };
      }
      await api.createPost({ title, content, route });
      setTitle('');
      setContent('');
      setSelectedWalkId('');
      onCreated();
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="card">
      <h2>새 게시글</h2>
      <form onSubmit={submit}>
        <label>제목</label>
        <input value={title} onChange={(e) => setTitle(e.target.value)} required />
        <label>내용</label>
        <textarea value={content} onChange={(e) => setContent(e.target.value)} required />
        <label>산책 코스 첨부 (선택)</label>
        <select value={selectedWalkId} onChange={(e) => setSelectedWalkId(e.target.value)}>
          <option value="">첨부 안 함</option>
          {walks.map((w) => (
            <option key={w.id} value={w.id}>{new Date(w.startedAt).toLocaleString('ko-KR')} · {w.distanceKm}km</option>
          ))}
        </select>
        {error && <div className="error">{error}</div>}
        <button className="btn" type="submit" disabled={submitting}>{submitting ? '작성 중...' : '작성 완료'}</button>
      </form>
    </div>
  );
}
