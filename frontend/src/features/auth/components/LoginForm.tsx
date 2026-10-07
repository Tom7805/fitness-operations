import { zodResolver } from '@hookform/resolvers/zod';
import { useState, type ReactNode } from 'react';
import { useForm, useWatch } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { Spinner } from '@/components/ui/Spinner';
import type { ApiError } from '@/services/http/apiError';
import { useCountdown } from '../hooks/useCountdown';
import { useLogin } from '../hooks/useLogin';
import { loginSchema, type LoginFormValues } from '../schemas/auth.schema';
import type { AuthResponse } from '../types/auth.types';
import { detectClientType } from '../utils/clientType';
import { formatCountdown } from '../utils/formatDuration';
import { translateMessage } from '../utils/translateMessage';
import { LoginErrorAlert } from './LoginErrorAlert';
import { PasswordInput } from './PasswordInput';

const LOCKED = 'ACCOUNT_TEMPORARILY_LOCKED';

interface LoginFormProps {
  onSuccess: (response: AuthResponse) => void;
  submitLabel?: string;
  /** Nội dung dưới nút đăng nhập (liên kết chuyển chế độ). */
  footer?: ReactNode;
}

interface LockState {
  username: string;
  until: number;
}

const normalizeUsername = (value: string) => value.trim().toLowerCase();

/**
 * Form đăng nhập dùng chung cho máy tính, điện thoại, máy quầy và bước xác nhận của quản lý khi đăng ký máy.
 * Tạm khóa hiển thị đếm ngược và khóa nút gửi tới khi hết thời gian chờ (TC-02).
 */
export function LoginForm({ onSuccess, submitLabel, footer }: LoginFormProps) {
  const { t } = useTranslation();
  const login = useLogin();
  const [failure, setFailure] = useState<ApiError | null>(null);
  const [lock, setLock] = useState<LockState | null>(null);

  const form = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: { username: '', password: '' },
  });
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = form;

  const username = useWatch({ control: form.control, name: 'username' });
  const lockApplies = lock !== null && normalizeUsername(username) === lock.username;
  const lockSecondsLeft = useCountdown(lockApplies ? lock.until : null);
  const locked = lockApplies && lockSecondsLeft > 0;
  // Khóa là của tài khoản: đổi sang tên đăng nhập khác thì không hiển thị khóa của tài khoản trước.
  const visibleFailure = failure && (failure.code !== LOCKED || lockApplies) ? failure : null;

  const onSubmit = handleSubmit((values) => {
    setFailure(null);
    login.mutate(
      { username: values.username, password: values.password, clientType: detectClientType() },
      {
        onSuccess,
        onError: (error) => {
          if (error.code === LOCKED) {
            const seconds = Number(error.details.retryAfterSeconds ?? 0);
            setLock({
              username: normalizeUsername(values.username),
              until: Date.now() + seconds * 1000,
            });
          }
          if (error.code === 'INVALID_CREDENTIALS') {
            form.resetField('password');
            form.setFocus('password');
          }
          if (error.code === 'VALIDATION_ERROR') {
            error.fieldErrors.forEach((fieldError) => {
              if (fieldError.field === 'username' || fieldError.field === 'password') {
                form.setError(fieldError.field, { message: fieldError.message });
              }
            });
          }
          setFailure(error);
        },
      },
    );
  });

  const pending = login.isPending;
  const usernameError = translateMessage(t, errors.username?.message);
  const passwordError = translateMessage(t, errors.password?.message);

  return (
    <form onSubmit={onSubmit} noValidate className="flex flex-col gap-5" aria-busy={pending}>
      <div className="flex flex-col gap-2">
        <Label htmlFor="login-username">{t('auth.login.username')}</Label>
        <Input
          id="login-username"
          autoComplete="username"
          autoCapitalize="none"
          autoCorrect="off"
          spellCheck={false}
          autoFocus
          placeholder={t('auth.login.usernamePlaceholder')}
          disabled={pending}
          aria-invalid={usernameError ? true : undefined}
          aria-describedby={usernameError ? 'login-username-error' : undefined}
          {...register('username')}
        />
        {usernameError ? (
          <p id="login-username-error" className="text-sm text-destructive">
            {usernameError}
          </p>
        ) : null}
      </div>

      <div className="flex flex-col gap-2">
        <Label htmlFor="login-password">{t('auth.login.password')}</Label>
        <PasswordInput
          id="login-password"
          autoComplete="current-password"
          disabled={pending}
          aria-invalid={passwordError ? true : undefined}
          aria-describedby={passwordError ? 'login-password-error' : undefined}
          {...register('password')}
        />
        {passwordError ? (
          <p id="login-password-error" className="text-sm text-destructive">
            {passwordError}
          </p>
        ) : null}
      </div>

      {visibleFailure ? (
        <LoginErrorAlert error={visibleFailure} lockSecondsLeft={lockSecondsLeft} />
      ) : null}

      <Button type="submit" size="lg" className="w-full" disabled={pending || locked}>
        {pending ? (
          <>
            <Spinner />
            {t('auth.login.submitting')}
          </>
        ) : locked ? (
          t('auth.login.retryIn', { time: formatCountdown(lockSecondsLeft) })
        ) : (
          (submitLabel ?? t('auth.login.submit'))
        )}
      </Button>

      {footer}
    </form>
  );
}
