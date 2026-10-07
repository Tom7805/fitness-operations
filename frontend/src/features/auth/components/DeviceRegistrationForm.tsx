import { zodResolver } from '@hookform/resolvers/zod';
import { CircleAlert } from 'lucide-react';
import { useEffect } from 'react';
import { useForm, useWatch } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import { Alert } from '@/components/ui/Alert';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { Spinner } from '@/components/ui/Spinner';
import { cn } from '@/lib/cn';
import { useRegisterDevice } from '../hooks/useDeviceRegistration';
import {
  deviceRegistrationSchema,
  type DeviceRegistrationFormValues,
} from '../schemas/auth.schema';
import type { Branch, DeviceRegistrationResult } from '../types/auth.types';
import { translateMessage } from '../utils/translateMessage';

interface DeviceRegistrationFormProps {
  branches: Branch[];
  onRegistered: (result: DeviceRegistrationResult) => void;
  onCancel: () => void;
  cancelling?: boolean;
}

/** Thiết kế S5 bước 2: chọn câu lạc bộ đặt máy và đặt tên máy quầy. */
export function DeviceRegistrationForm({
  branches,
  onRegistered,
  onCancel,
  cancelling = false,
}: DeviceRegistrationFormProps) {
  const { t } = useTranslation();
  const registerDevice = useRegisterDevice();
  const onlyBranch = branches.length === 1 ? branches[0] : undefined;

  const form = useForm<DeviceRegistrationFormValues>({
    resolver: zodResolver(deviceRegistrationSchema),
    defaultValues: {
      branchId: onlyBranch ? String(onlyBranch.id) : '',
      name: t('auth.register.deviceNameDefault'),
    },
  });
  const {
    register,
    handleSubmit,
    setValue,
    formState: { errors },
  } = form;

  useEffect(() => {
    if (onlyBranch) {
      setValue('branchId', String(onlyBranch.id));
    }
  }, [onlyBranch, setValue]);

  const selectedBranchId = useWatch({ control: form.control, name: 'branchId' });
  const pending = registerDevice.isPending;

  const onSubmit = handleSubmit((values) => {
    registerDevice.mutate(
      { branchId: Number(values.branchId), name: values.name },
      { onSuccess: onRegistered },
    );
  });

  const branchError = translateMessage(t, errors.branchId?.message);
  const nameError = translateMessage(t, errors.name?.message);

  return (
    <form onSubmit={onSubmit} noValidate className="flex flex-col gap-5">
      <fieldset
        className="flex flex-col gap-2"
        aria-describedby={branchError ? 'branch-error' : undefined}
      >
        <legend className="mb-2 text-sm font-medium">{t('auth.register.branch')}</legend>
        {branches.map((branch) => (
          <label
            key={branch.id}
            className={cn(
              'flex min-h-12 cursor-pointer items-center gap-3 rounded-md border px-3 py-2 text-base transition-colors hover:bg-muted',
              selectedBranchId === String(branch.id) && 'border-primary bg-primary-soft',
            )}
          >
            <input
              type="radio"
              value={String(branch.id)}
              className="size-4 accent-primary"
              disabled={pending}
              {...register('branchId')}
            />
            <span>{branch.name}</span>
          </label>
        ))}
        {branchError ? (
          <p id="branch-error" className="text-sm text-destructive">
            {branchError}
          </p>
        ) : null}
      </fieldset>

      <div className="flex flex-col gap-2">
        <Label htmlFor="device-name">{t('auth.register.deviceName')}</Label>
        <Input
          id="device-name"
          maxLength={100}
          disabled={pending}
          aria-invalid={nameError ? true : undefined}
          aria-describedby={nameError ? 'device-name-error' : 'device-name-hint'}
          {...register('name')}
        />
        {nameError ? (
          <p id="device-name-error" className="text-sm text-destructive">
            {nameError}
          </p>
        ) : (
          <p id="device-name-hint" className="text-sm text-muted-foreground">
            {t('auth.register.deviceNameHint')}
          </p>
        )}
      </div>

      {registerDevice.error ? (
        <Alert
          variant="error"
          icon={<CircleAlert aria-hidden />}
          title={t('auth.errors.genericTitle')}
        >
          {registerDevice.error.message}
        </Alert>
      ) : null}

      <Button type="submit" size="lg" className="w-full" disabled={pending || cancelling}>
        {pending ? (
          <>
            <Spinner />
            {t('auth.register.submitting')}
          </>
        ) : (
          t('auth.register.submit')
        )}
      </Button>
      <Button type="button" variant="ghost" onClick={onCancel} disabled={pending || cancelling}>
        {t('auth.register.cancel')}
      </Button>
    </form>
  );
}
