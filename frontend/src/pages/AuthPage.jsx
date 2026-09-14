import { useState } from 'react';
import { useAuth } from '../AuthContext.jsx';

export default function AuthPage() {
  const { login, signup } = useAuth();
  const [mode, setMode] = useState('login'); // 'login' | 'signup'
  const [form, setForm] = useState({ name: '', email: '', password: '', region: '' });
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);

  const update = (key) => (e) => setForm((f) => ({ ...f, [key]: e.target.value }));

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    setMessage('');
    setLoading(true);
    try {
      if (mode === 'login') {
        await login(form.email, form.password);
      } else {
        await signup(form);
        setMessage('회원가입 완료! 로그인해주세요.');
        setMode('login');
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <main>
      <div className="card" style={{ maxWidth: 360, margin: '40px auto' }}>
        <h2>{mode === 'login' ? '로그인' : '회원가입'}</h2>
        <form onSubmit={submit}>
          {mode === 'signup' && (
            <>
              <label>이름</label>
              <input value={form.name} onChange={update('name')} required />
              <label>지역</label>
              <input value={form.region} onChange={update('region')} placeholder="서울" required />
            </>
          )}
          <label>이메일</label>
          <input type="email" value={form.email} onChange={update('email')} required />
          <label>비밀번호</label>
          <input type="password" value={form.password} onChange={update('password')} required />

          {error && <div className="error">{error}</div>}
          {message && <div className="success">{message}</div>}

          <button className="btn" type="submit" disabled={loading} style={{ width: '100%', marginTop: 4 }}>
            {loading ? '처리 중...' : mode === 'login' ? '로그인' : '회원가입'}
          </button>
        </form>
        <p className="muted" style={{ marginTop: 12, textAlign: 'center' }}>
          {mode === 'login' ? '계정이 없나요? ' : '이미 계정이 있나요? '}
          <a href="#" onClick={(e) => { e.preventDefault(); setMode(mode === 'login' ? 'signup' : 'login'); setError(''); }}>
            {mode === 'login' ? '회원가입' : '로그인'}
          </a>
        </p>
      </div>
    </main>
  );
}
