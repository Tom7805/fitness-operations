import { MonitorSmartphone } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import type { CounterDevice } from '../types/auth.types';

/** Dải nhận diện máy quầy (thiết kế S3): lễ tân luôn biết mình đang ở quầy nào, câu lạc bộ nào. */
export function DeviceBanner({ device }: { device: CounterDevice }) {
  const { t } = useTranslation();
  return (
    <div
      className="flex items-center gap-3 rounded-lg bg-primary-soft px-4 py-3 text-primary-soft-foreground"
      data-testid="device-banner"
    >
      <MonitorSmartphone className="size-6 shrink-0" aria-hidden />
      <div className="min-w-0">
        <p className="truncate text-sm font-semibold">
          {t('auth.device.badge')} · {device.name}
        </p>
        {device.branch ? <p className="truncate text-sm">{device.branch.name}</p> : null}
      </div>
    </div>
  );
}
