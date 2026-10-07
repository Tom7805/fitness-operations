import { Eye, EyeOff, TriangleAlert } from 'lucide-react';
import { forwardRef, useState, type InputHTMLAttributes, type KeyboardEvent } from 'react';
import { useTranslation } from 'react-i18next';
import { Input } from '@/components/ui/Input';
import { cn } from '@/lib/cn';

type PasswordInputProps = Omit<InputHTMLAttributes<HTMLInputElement>, 'type'>;

/** Ô mật khẩu có nút hiện/ẩn và cảnh báo Caps Lock. */
export const PasswordInput = forwardRef<HTMLInputElement, PasswordInputProps>(
  ({ className, onKeyDown, onKeyUp, id, ...props }, ref) => {
    const { t } = useTranslation();
    const [visible, setVisible] = useState(false);
    const [capsLock, setCapsLock] = useState(false);

    const detectCapsLock = (event: KeyboardEvent<HTMLInputElement>) => {
      setCapsLock(event.getModifierState?.('CapsLock') ?? false);
    };

    return (
      <div className="flex flex-col gap-1.5">
        <div className="relative">
          <Input
            ref={ref}
            id={id}
            type={visible ? 'text' : 'password'}
            className={cn('pr-12', className)}
            onKeyDown={(event) => {
              detectCapsLock(event);
              onKeyDown?.(event);
            }}
            onKeyUp={(event) => {
              detectCapsLock(event);
              onKeyUp?.(event);
            }}
            {...props}
          />
          <button
            type="button"
            onClick={() => setVisible((value) => !value)}
            className="absolute inset-y-0 right-0 flex w-12 items-center justify-center rounded-r-md text-muted-foreground hover:text-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
            aria-label={visible ? t('auth.login.hidePassword') : t('auth.login.showPassword')}
            aria-pressed={visible}
            aria-controls={id}
          >
            {visible ? <EyeOff className="size-5" /> : <Eye className="size-5" />}
          </button>
        </div>
        {capsLock ? (
          <p className="flex items-center gap-1.5 text-xs font-medium text-warning-soft-foreground">
            <TriangleAlert className="size-3.5" aria-hidden />
            {t('auth.login.capsLock')}
          </p>
        ) : null}
      </div>
    );
  },
);
PasswordInput.displayName = 'PasswordInput';
