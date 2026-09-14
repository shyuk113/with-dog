import { useEffect, useState } from 'react';
import { api, API_BASE } from '../api.js';
import { useAuth } from '../AuthContext.jsx';
import RouteMap from './RouteMap.jsx';

export default function PostDetail({ postId, onBack }) {
  const { user } = useAuth();
  const [post, setPost] = useState(null);
  const [comments, setComments] = useState([]);
  const [error, setError] = useState('');
  const [newComment, setNewComment] = useState('');
  const [replyTo, setReplyTo] = useState(null);
  const [replyText, setReplyText] = useState('');
  const [imageFile, setImageFile] = useState(null);

  const load = async () => {
    try {
      const [p, c] = await Promise.all([api.getPost(postId), api.getComments(postId)]);
      setPost(p);
      setComments(c.content);
    } catch (err) {
      setError(err.message);
    }
  };

  useEffect(() => {
    load();
  }, [postId]);

  const toggleLike = async () => {
    try {
      if (post.likedByMe) await api.unlikePost(postId);
      else await api.likePost(postId);
      setPost(await api.getPost(postId));
    } catch (err) {
      setError(err.message);
    }
  };

  const submitComment = async (e) => {
    e.preventDefault();
    try {
      await api.createComment(postId, { content: newComment, parentId: null });
      setNewComment('');
      const c = await api.getComments(postId);
      setComments(c.content);
    } catch (err) {
      setError(err.message);
    }
  };

  const submitReply = async (parentId) => {
    try {
      await api.createComment(postId, { content: replyText, parentId });
      setReplyText('');
      setReplyTo(null);
      const c = await api.getComments(postId);
      setComments(c.content);
    } catch (err) {
      setError(err.message);
    }
  };

  const deleteComment = async (id) => {
    if (!confirm('댓글을 삭제할까요?')) return;
    try {
      await api.deleteComment(postId, id);
      const c = await api.getComments(postId);
      setComments(c.content);
    } catch (err) {
      setError(err.message);
    }
  };

  const uploadImage = async () => {
    if (!imageFile) return;
    const formData = new FormData();
    formData.append('image', imageFile);
    try {
      setPost(await api.attachPostImage(postId, formData));
      setImageFile(null);
    } catch (err) {
      setError(err.message);
    }
  };

  const deletePost = async () => {
    if (!confirm('게시글을 삭제할까요?')) return;
    try {
      await api.deletePost(postId);
      onBack();
    } catch (err) {
      setError(err.message);
    }
  };

  if (!post) return <main>{error && <div className="error">{error}</div>}<p className="spinner">불러오는 중...</p></main>;

  const topLevel = comments.filter((c) => !c.parentId);
  const repliesOf = (id) => comments.filter((c) => c.parentId === id);

  return (
    <main>
      <button className="btn small secondary" onClick={onBack}>← 목록으로</button>

      <div className="card">
        <div className="row" style={{ justifyContent: 'space-between' }}>
          <h2>{post.title}</h2>
          {post.userId === user?.id && <button className="btn small danger" onClick={deletePost}>삭제</button>}
        </div>
        {post.imageUrl && <img className="thumb" src={`${API_BASE}${post.imageUrl}`} alt="" />}
        <p>{post.content}</p>

        {post.courseName && (
          <div style={{ marginTop: 10 }}>
            <h3>🐾 {post.courseName}</h3>
            <RouteMap points={post.route} />
            <p className="muted">{post.distanceKm}km · {post.durationMinutes}분</p>
          </div>
        )}

        {post.userId === user?.id && !post.imageUrl && (
          <div className="row" style={{ marginTop: 10 }}>
            <input type="file" accept="image/*" onChange={(e) => setImageFile(e.target.files?.[0] ?? null)} />
            <button className="btn small secondary" onClick={uploadImage} disabled={!imageFile}>사진 첨부</button>
          </div>
        )}

        <div className="row" style={{ marginTop: 12 }}>
          <button className={`btn small ${post.likedByMe ? '' : 'secondary'}`} onClick={toggleLike}>
            ❤ 좋아요 {post.likeCount}
          </button>
        </div>
      </div>

      {error && <div className="error">{error}</div>}

      <div className="card">
        <h3>댓글</h3>
        <form onSubmit={submitComment} className="row">
          <input value={newComment} onChange={(e) => setNewComment(e.target.value)} placeholder="댓글을 남겨보세요" required />
          <button className="btn small" type="submit">등록</button>
        </form>

        {topLevel.length === 0 && <p className="muted">첫 댓글을 남겨보세요.</p>}
        {topLevel.map((c) => (
          <div key={c.id}>
            <div className="comment">
              <div className="row" style={{ justifyContent: 'space-between' }}>
                <span>{c.content} <span className="muted">(user #{c.userId})</span></span>
                <div className="row">
                  <button className="btn small secondary" onClick={() => setReplyTo(replyTo === c.id ? null : c.id)}>답글</button>
                  {c.userId === user?.id && <button className="btn small danger" onClick={() => deleteComment(c.id)}>삭제</button>}
                </div>
              </div>
            </div>

            {repliesOf(c.id).map((r) => (
              <div className="comment reply" key={r.id}>
                <div className="row" style={{ justifyContent: 'space-between' }}>
                  <span>↳ {r.content} <span className="muted">(user #{r.userId})</span></span>
                  {r.userId === user?.id && <button className="btn small danger" onClick={() => deleteComment(r.id)}>삭제</button>}
                </div>
              </div>
            ))}

            {replyTo === c.id && (
              <form
                className="row"
                style={{ marginLeft: 20, marginBottom: 8 }}
                onSubmit={(e) => {
                  e.preventDefault();
                  submitReply(c.id);
                }}
              >
                <input value={replyText} onChange={(e) => setReplyText(e.target.value)} placeholder="답글 남기기" required />
                <button className="btn small" type="submit">등록</button>
              </form>
            )}
          </div>
        ))}
      </div>
    </main>
  );
}
