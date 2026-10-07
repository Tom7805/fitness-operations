import { useEffect, useState } from 'react';

/** Số giây còn lại tới mốc {@code targetMs} (mili giây), cập nhật mỗi giây; 0 khi đã qua hoặc không có mốc. */
export function useCountdown(targetMs: number | null): number {
  const compute = () =>
    targetMs === null ? 0 : Math.max(0, Math.ceil((targetMs - Date.now()) / 1000));
  const [remaining, setRemaining] = useState(compute);

  useEffect(() => {
    const tick = () =>
      setRemaining(targetMs === null ? 0 : Math.max(0, Math.ceil((targetMs - Date.now()) / 1000)));
    tick();
    if (targetMs === null) {
      return undefined;
    }
    const timer = window.setInterval(tick, 1000);
    return () => window.clearInterval(timer);
  }, [targetMs]);

  return remaining;
}
