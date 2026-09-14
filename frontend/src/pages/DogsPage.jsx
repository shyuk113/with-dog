import { useEffect, useState } from 'react';
import { api } from '../api.js';

const BREEDS = [
  'GOLDEN_RETRIEVER', 'GERMAN_SHEPHERD', 'POODLE', 'BULLDOG', 'BEAGLE',
  'POMERANIAN', 'CHIHUAHUA', 'MALTESE', 'DACHSHUND', 'SIBERIAN_HUSKY',
];

export default function DogsPage() {
  const [dogs, setDogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [form, setForm] = useState({ name: '', breed: BREEDS[0], birthDate: '', weight: '' });
  const [submitting, setSubmitting] = useState(false);

  const load = async () => {
    setLoading(true);
    try {
      setDogs(await api.getDogs());
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const update = (key) => (e) => setForm((f) => ({ ...f, [key]: e.target.value }));

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      await api.createDog({ ...form, weight: Number(form.weight) });
      setForm({ name: '', breed: BREEDS[0], birthDate: '', weight: '' });
      await load();
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  const remove = async (id) => {
    if (!confirm('이 강아지 프로필을 삭제할까요?')) return;
    try {
      await api.deleteDog(id);
      await load();
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <main>
      <div className="card">
        <h2>강아지 프로필 등록</h2>
        <form onSubmit={submit}>
          <label>이름</label>
          <input value={form.name} onChange={update('name')} required />
          <label>품종</label>
          <select value={form.breed} onChange={update('breed')}>
            {BREEDS.map((b) => (
              <option key={b} value={b}>{b}</option>
            ))}
          </select>
          <label>생년월일</label>
          <input type="date" value={form.birthDate} onChange={update('birthDate')} required />
          <label>몸무게(kg)</label>
          <input type="number" step="0.1" min="0" max="50" value={form.weight} onChange={update('weight')} required />
          {error && <div className="error">{error}</div>}
          <button className="btn" type="submit" disabled={submitting}>{submitting ? '등록 중...' : '등록'}</button>
        </form>
      </div>

      <div className="card">
        <h2>내 강아지 목록</h2>
        {loading && <p className="spinner">불러오는 중...</p>}
        {!loading && dogs.length === 0 && <p className="muted">등록된 강아지가 없습니다.</p>}
        {dogs.map((dog) => (
          <div className="list-item" key={dog.id}>
            <div className="row" style={{ justifyContent: 'space-between' }}>
              <div>
                <strong>{dog.name}</strong> <span className="muted">({dog.breed})</span>
                <div className="muted">생일 {dog.birthDate} · {dog.weight}kg</div>
              </div>
              <button className="btn small danger" onClick={() => remove(dog.id)}>삭제</button>
            </div>
          </div>
        ))}
      </div>
    </main>
  );
}
