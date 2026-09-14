import { useEffect, useState } from 'react';
import { api } from '../api.js';

export default function MissionsPage() {
  const [summary, setSummary] = useState(null);
  const [error, setError] = useState('');
  const [claimingType, setClaimingType] = useState(null);

  const load = async () => {
    try {
      setSummary(await api.getWeeklyMissions());
    } catch (err) {
      setError(err.message);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const claim = async (missionType) => {
    setError('');
    setClaimingType(missionType);
    try {
      setSummary(await api.claimMission(missionType));
    } catch (err) {
      setError(err.message);
    } finally {
      setClaimingType(null);
    }
  };

  if (!summary) return <main>{error && <div className="error">{error}</div>}<p className="spinner">불러오는 중...</p></main>;

  const progressPct = Math.round((summary.gaugePoints / summary.totalPoints) * 100);

  return (
    <main>
      {error && <div className="error">{error}</div>}

      <div className="card">
        <h2>이번 주 미션 ({summary.weekStartDate} 시작)</h2>
        <div className="row" style={{ justifyContent: 'space-between' }}>
          <span>주간 게이지</span>
          <strong>{summary.gaugePoints} / {summary.totalPoints}</strong>
        </div>
        <div className="mission-bar">
          <div className="mission-bar-fill" style={{ width: `${progressPct}%` }} />
        </div>
        <div className="row" style={{ justifyContent: 'space-between', marginTop: 10 }}>
          <span>내 레벨</span>
          <strong>Lv.{summary.level} ({summary.experience} EXP)</strong>
        </div>
      </div>

      <div className="card">
        <h2>미션 목록</h2>
        {summary.missions.map((m) => (
          <div className="list-item" key={m.missionType}>
            <div className="row" style={{ justifyContent: 'space-between' }}>
              <div>
                <strong>{m.description}</strong>
                <div className="muted">{m.points}포인트</div>
              </div>
              {m.claimed ? (
                <span className="badge done">수령 완료</span>
              ) : m.achieved ? (
                <button className="btn small" disabled={claimingType === m.missionType} onClick={() => claim(m.missionType)}>
                  {claimingType === m.missionType ? '받는 중...' : '보상 받기'}
                </button>
              ) : (
                <span className="badge">진행 중</span>
              )}
            </div>
          </div>
        ))}
      </div>
    </main>
  );
}
