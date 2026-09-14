// 실제 지도 API 없이, 수집된 좌표를 정규화해서 SVG 폴리라인으로 그리는 간단한 경로 시각화
export default function RouteMap({ points }) {
  if (!points || points.length === 0) {
    return (
      <svg className="route-svg" viewBox="0 0 300 180">
        <text x="150" y="90" textAnchor="middle" fill="#aaa" fontSize="12">
          아직 기록된 좌표가 없습니다
        </text>
      </svg>
    );
  }

  const lats = points.map((p) => p.lat);
  const lons = points.map((p) => p.lon);
  const minLat = Math.min(...lats);
  const maxLat = Math.max(...lats);
  const minLon = Math.min(...lons);
  const maxLon = Math.max(...lons);
  const padding = 20;
  const width = 300;
  const height = 180;

  const spanLat = maxLat - minLat || 0.0001;
  const spanLon = maxLon - minLon || 0.0001;

  const project = (p) => {
    const x = padding + ((p.lon - minLon) / spanLon) * (width - padding * 2);
    const y = height - padding - ((p.lat - minLat) / spanLat) * (height - padding * 2);
    return [x, y];
  };

  const pathPoints = points.map(project);
  const polylinePoints = pathPoints.map(([x, y]) => `${x},${y}`).join(' ');
  const [startX, startY] = pathPoints[0];
  const [endX, endY] = pathPoints[pathPoints.length - 1];

  return (
    <svg className="route-svg" viewBox={`0 0 ${width} ${height}`}>
      <polyline points={polylinePoints} fill="none" stroke="#e08a3e" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round" />
      <circle cx={startX} cy={startY} r="5" fill="#5c9c6e" />
      <circle cx={endX} cy={endY} r="5" fill="#c85c5c" />
    </svg>
  );
}
