import { useEffect, useRef, useState } from 'react';
import { api } from '../api.js';
import { totalDistanceKm, formatDateTime } from '../utils.js';
import RouteMap from '../components/RouteMap.jsx';
import WalkResultCard from '../components/WalkResultCard.jsx';

const SEOUL_CITY_HALL = { lat: 37.5665, lon: 126.978 };
const POINTS_KEY = (walkId) => `with-dog:walk-points:${walkId}`;

function loadStoredPoints(walkId) {
  try {
    const raw = localStorage.getItem(POINTS_KEY(walkId));
    return raw ? JSON.parse(raw) : [];
  } catch {
    return [];
  }
}

function storePoints(walkId, points) {
  localStorage.setItem(POINTS_KEY(walkId), JSON.stringify(points));
}

export default function WalkPage() {
  const [dogs, setDogs] = useState([]);
  const [selectedDogId, setSelectedDogId] = useState('');
  const [ongoingWalk, setOngoingWalk] = useState(null);
  const [points, setPoints] = useState([]);
  const [justEnded, setJustEnded] = useState(null);
  const [history, setHistory] = useState([]);
  const [historyDetail, setHistoryDetail] = useState(null);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  const [, forceTick] = useState(0);

  useEffect(() => {
    (async () => {
      try {
        const dogList = await api.getDogs();
        setDogs(dogList);
        if (dogList.length === 1) setSelectedDogId(String(dogList[0].id));

        const ongoing = await api.getOngoingWalk();
        if (ongoing) {
          setOngoingWalk(ongoing);
          setPoints(loadStoredPoints(ongoing.id));
        }
        await loadHistory();
      } catch (err) {
        setError(err.message);
      }
    })();
  }, []);

  useEffect(() => {
    if (!ongoingWalk) return;
    const timer = setInterval(() => forceTick((n) => n + 1), 5000);
    return () => clearInterval(timer);
  }, [ongoingWalk]);

  const loadHistory = async () => {
    try {
      const page = await api.getWalks(0);
      setHistory(page.content);
    } catch (err) {
      setError(err.message);
    }
  };

  const startWalk = async () => {
    if (!selectedDogId) {
      setError('강아지를 선택해주세요.');
      return;
    }
    setError('');
    setBusy(true);
    try {
      const walk = await api.startWalk({ dogId: Number(selectedDogId), startedAt: new Date().toISOString() });
      setOngoingWalk(walk);
      setJustEnded(null);
      const initial = [SEOUL_CITY_HALL].map((p) => ({ ...p, capturedAt: new Date().toISOString() }));
      setPoints(initial);
      storePoints(walk.id, initial);
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  const addPoint = () => {
    const last = points[points.length - 1] || SEOUL_CITY_HALL;
    const next = {
      lat: last.lat + (Math.random() - 0.5) * 0.002,
      lon: last.lon + (Math.random() - 0.5) * 0.002,
      capturedAt: new Date().toISOString(),
    };
    const updated = [...points, next];
    setPoints(updated);
    storePoints(ongoingWalk.id, updated);
  };

  const endWalk = async () => {
    setError('');
    setBusy(true);
    try {
      const distanceKm = totalDistanceKm(points);
      const detail = await api.endWalk(ongoingWalk.id, {
        distanceKm,
        routePointRequest: points.map((p) => ({ lat: p.lat, lon: p.lon, capturedAt: p.capturedAt })),
      });
      localStorage.removeItem(POINTS_KEY(ongoingWalk.id));
      setJustEnded(detail);
      setOngoingWalk(null);
      setPoints([]);
      setHistoryDetail(null);
      await loadHistory();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  const openHistoryDetail = async (id) => {
    try {
      setHistoryDetail(await api.getWalk(id));
      setJustEnded(null);
    } catch (err) {
      setError(err.message);
    }
  };

  const elapsedMinutes = ongoingWalk ? Math.floor((Date.now() - new Date(ongoingWalk.startedAt).getTime()) / 60000) : 0;
  const liveDistance = totalDistanceKm(points);

  return (
    <main>
      {error && <div className="error">{error}</div>}

      {!ongoingWalk && (
        <div className="card">
          <h2>산책 시작</h2>
          {dogs.length === 0 && <p className="muted">먼저 강아지 프로필을 등록해주세요.</p>}
          {dogs.length > 1 && (
            <>
              <label>산책할 강아지 선택</label>
              <select value={selectedDogId} onChange={(e) => setSelectedDogId(e.target.value)}>
                <option value="">선택</option>
                {dogs.map((d) => (
                  <option key={d.id} value={d.id}>{d.name}</option>
                ))}
              </select>
            </>
          )}
          {dogs.length === 1 && <p className="muted">{dogs[0].name}와(과) 산책을 시작합니다.</p>}
          <button className="btn" disabled={busy || dogs.length === 0} onClick={startWalk}>
            {busy ? '시작하는 중...' : '산책 시작'}
          </button>
        </div>
      )}

      {ongoingWalk && (
        <div className="card">
          <div className="row" style={{ justifyContent: 'space-between' }}>
            <h2>산책 진행 중</h2>
            <span className="badge">LIVE</span>
          </div>
          <RouteMap points={points} />
          <div className="row wrap" style={{ marginTop: 10, gap: 16 }}>
            <div>
              <div className="muted">경과 시간</div>
              <strong>{elapsedMinutes}분</strong>
            </div>
            <div>
              <div className="muted">누적 거리(추정)</div>
              <strong>{liveDistance} km</strong>
            </div>
            <div>
              <div className="muted">기록된 좌표</div>
              <strong>{points.length}개</strong>
            </div>
          </div>
          <p className="muted">
            실제 GPS 대신, "현재 위치 갱신" 버튼을 눌러 위치 이동을 시뮬레이션합니다.
          </p>
          <div className="row" style={{ marginTop: 8 }}>
            <button className="btn secondary" onClick={addPoint}>📍 현재 위치 갱신</button>
            <button className="btn danger" disabled={busy} onClick={endWalk}>{busy ? '종료하는 중...' : '산책 종료'}</button>
          </div>
        </div>
      )}

      {justEnded && <WalkResultCard walk={justEnded} />}

      <div className="card">
        <h2>산책 히스토리</h2>
        {history.length === 0 && <p className="muted">아직 완료된 산책이 없습니다.</p>}
        {history.map((w) => (
          <div className="list-item" key={w.id} style={{ cursor: 'pointer' }} onClick={() => openHistoryDetail(w.id)}>
            <div className="row" style={{ justifyContent: 'space-between' }}>
              <div>
                <strong>{formatDateTime(w.startedAt)}</strong>
                <div className="muted">{w.distanceKm} km · {w.durationMinute}분</div>
              </div>
              <span className={`badge ${w.endedAt ? 'done' : ''}`}>{w.endedAt ? '완료' : '진행중'}</span>
            </div>
          </div>
        ))}
      </div>

      {historyDetail && <WalkResultCard walk={historyDetail} />}
    </main>
  );
}
