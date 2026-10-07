import { z } from 'zod';

/** Thông báo lỗi là khóa i18n, được dịch khi hiển thị. */
export const loginSchema = z.object({
  username: z
    .string()
    .trim()
    .min(1, 'validation:required.username')
    .max(50, 'validation:max.username'),
  password: z.string().min(1, 'validation:required.password').max(128, 'validation:max.password'),
});

export type LoginFormValues = z.infer<typeof loginSchema>;

export const deviceRegistrationSchema = z.object({
  branchId: z.string().min(1, 'validation:required.branch'),
  name: z
    .string()
    .trim()
    .min(1, 'validation:required.deviceName')
    .max(100, 'validation:max.deviceName'),
});

export type DeviceRegistrationFormValues = z.infer<typeof deviceRegistrationSchema>;
