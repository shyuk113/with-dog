import RouteMap from './RouteMap.jsx';
import { formatDateTime } from '../utils.js';

// 지도 API/외부 라이브러리 없이 Canvas API만으로 결과 카드를 그려서 이미지(png)로 저장
function saveAsImage(walk) {
  const canvas = document.createElement('canvas');
  canvas.width = 480;
  canvas.height = 520;
  const ctx = canvas.getContext('2d');

  ctx.fillStyle = '#fffaf3';
  ctx.fillRect(0, 0, canvas.width, canvas.height);

  ctx.fillStyle = '#2b2a27';
  ctx.font = 'bold 22px sans-serif';
  ctx.fillText(`${walk.dog?.name ?? '강아지'}와의 산책 기록`, 24, 44);

  ctx.font = '13px sans-serif';
  ctx.fillStyle = '#8a8578';
  ctx.fillText(formatDateTime(walk.startedAt), 24, 68);

  // 경로 그리기
  const points = walk.routePoints || [];
  if (points.length > 0) {
    const lats = points.map((p) => p.lat);
    const lons = points.map((p) => p.lon);
    const minLat = Math.min(...lats), maxLat = Math.max(...lats);
    const minLon = Math.min(...lons), maxLon = Math.max(...lons);
    const spanLat = maxLat - minLat || 0.0001;
    const spanLon = maxLon - minLon || 0.0001;
    const top = 90, left = 24, w = 432, h = 220;

    ctx.fillStyle = '#f0efe9';
    ctx.fillRect(left, top, w, h);

    ctx.strokeStyle = '#e08a3e';
    ctx.lineWidth = 3;
    ctx.beginPath();
    points.forEach((p, i) => {
      const x = left + 20 + ((p.lon - minLon) / spanLon) * (w - 40);
      const y = top + h - 20 - ((p.lat - minLat) / spanLat) * (h - 40);
      if (i === 0) ctx.moveTo(x, y);
      else ctx.lineTo(x, y);
    });
    ctx.stroke();
  }

  ctx.fillStyle = '#2b2a27';
  ctx.font = 'bold 16px sans-serif';
  const statY = 350;
  ctx.fillText(`거리 ${walk.distanceKm?.toFixed?.(2) ?? walk.distanceKm} km`, 24, statY);
  ctx.fillText(`소요 시간 ${walk.durationMinute}분`, 24, statY + 30);
  ctx.font = '13px sans-serif';
  ctx.fillStyle = '#8a8578';
  ctx.fillText(`시작 ${formatDateTime(walk.startedAt)}`, 24, statY + 60);
  ctx.fillText(`종료 ${formatDateTime(walk.endedAt)}`, 24, statY + 82);

  ctx.font = '12px sans-serif';
  ctx.fillStyle = '#bbb';
  ctx.fillText('with-dog', 24, canvas.height - 20);

  const link = document.createElement('a');
  link.download = `walk-${walk.id}.png`;
  link.href = canvas.toDataURL('image/png');
  link.click();
}

export default function WalkResultCard({ walk }) {
  if (!walk) return null;
  return (
    <div className="card">
      <h2>산책 상세 정보</h2>
      <RouteMap points={walk.routePoints} />
      <div className="row wrap" style={{ marginTop: 10, gap: 16 }}>
        <div>
          <div className="muted">강아지</div>
          <strong>{walk.dog?.name ?? '-'} ({walk.dog?.breed})</strong>
        </div>
        <div>
          <div className="muted">거리</div>
          <strong>{walk.distanceKm} km</strong>
        </div>
        <div>
          <div className="muted">소요 시간</div>
          <strong>{walk.durationMinute}분</strong>
        </div>
      </div>
      <p className="muted" style={{ marginTop: 8 }}>
        {formatDateTime(walk.startedAt)} ~ {formatDateTime(walk.endedAt)}
      </p>
      <button className="btn secondary" onClick={() => saveAsImage(walk)}>이미지로 저장하기</button>
    </div>
  );
}
