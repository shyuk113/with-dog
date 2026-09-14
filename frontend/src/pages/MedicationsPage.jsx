import { useEffect, useState } from 'react';
import { api, API_BASE } from '../api.js';
import { formatDate } from '../utils.js';

export default function MedicationsPage() {
  const [dogs, setDogs] = useState([]);
  const [dogId, setDogId] = useState('');
  const [medications, setMedications] = useState([]);
  const [file, setFile] = useState(null);
  const [error, setError] = useState('');
  const [scanning, setScanning] = useState(false);

  useEffect(() => {
    (async () => {
      try {
        const dogList = await api.getDogs();
        setDogs(dogList);
        if (dogList.length > 0) setDogId(String(dogList[0].id));
      } catch (err) {
        setError(err.message);
      }
    })();
  }, []);

  useEffect(() => {
    if (!dogId) return;
    loadMedications(dogId);
  }, [dogId]);

  const loadMedications = async (id) => {
    try {
      setMedications(await api.getMedications(id));
    } catch (err) {
      setError(err.message);
    }
  };

  const scan = async (e) => {
    e.preventDefault();
    if (!file) return;
    setScanning(true);
    setError('');
    try {
      const formData = new FormData();
      formData.append('image', file);
      await api.scanMedication(dogId, formData);
      setFile(null);
      await loadMedications(dogId);
    } catch (err) {
      setError(err.message);
    } finally {
      setScanning(false);
    }
  };

  const remove = async (id) => {
    if (!confirm('이 복용 기록을 삭제할까요?')) return;
    try {
      await api.deleteMedication(dogId, id);
      await loadMedications(dogId);
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <main>
      {error && <div className="error">{error}</div>}

      <div className="card">
        <h2>약 봉투 스캔</h2>
        <label>강아지 선택</label>
        <select value={dogId} onChange={(e) => setDogId(e.target.value)}>
          {dogs.map((d) => (
            <option key={d.id} value={d.id}>{d.name}</option>
          ))}
        </select>
        <form onSubmit={scan}>
          <label>약 봉투 사진</label>
          <input type="file" accept="image/*" onChange={(e) => setFile(e.target.files?.[0] ?? null)} />
          <button className="btn" type="submit" disabled={!file || !dogId || scanning}>
            {scanning ? '분석 중...' : '스캔해서 저장'}
          </button>
        </form>
      </div>

      <div className="card">
        <h2>복용 기록 (병원 방문 시 공유용)</h2>
        {medications.length === 0 && <p className="muted">기록이 없습니다.</p>}
        {medications.map((m) => (
          <div className="list-item" key={m.id}>
            {m.imageUrl && <img className="thumb" src={`${API_BASE}${m.imageUrl}`} alt="" style={{ maxHeight: 140 }} />}
            <div className="row" style={{ justifyContent: 'space-between' }}>
              <div>
                <strong>{m.drugName}</strong>
                <div className="muted">{m.dosage} · {m.hospitalName}</div>
                <div className="muted">처방일 {formatDate(m.prescribedDate)}</div>
              </div>
              <button className="btn small danger" onClick={() => remove(m.id)}>삭제</button>
            </div>
          </div>
        ))}
      </div>
    </main>
  );
}
