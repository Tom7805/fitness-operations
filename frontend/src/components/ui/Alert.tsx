import { cva, type VariantProps } from 'class-variance-authority';
import type { HTMLAttributes, ReactNode } from 'react';
import { cn } from '@/lib/cn';

const alertVariants = cva(
  'flex gap-3 rounded-lg border p-3 text-sm [&>svg]:mt-0.5 [&>svg]:size-5 [&>svg]:shrink-0',
  {
    variants: {
      variant: {
        error: 'border-destructive/30 bg-destructive-soft text-destructive-soft-foreground',
        warning: 'border-amber-500/30 bg-warning-soft text-warning-soft-foreground',
        success: 'border-emerald-500/30 bg-success-soft text-success-soft-foreground',
        info: 'border-sky-500/30 bg-info-soft text-info-soft-foreground',
      },
    },
    defaultVariants: { variant: 'info' },
  },
);

export interface AlertProps
  extends Omit<HTMLAttributes<HTMLDivElement>, 'title'>, VariantProps<typeof alertVariants> {
  icon?: ReactNode;
  title?: ReactNode;
}

/** Khung thông báo. Lỗi và cảnh báo dùng role="alert" để trình đọc màn hình đọc ngay. */
export function Alert({ className, variant, icon, title, children, role, ...props }: AlertProps) {
  const liveRole = role ?? (variant === 'error' || variant === 'warning' ? 'alert' : 'status');
  return (
    <div role={liveRole} className={cn(alertVariants({ variant }), className)} {...props}>
      {icon}
      <div className="flex min-w-0 flex-1 flex-col gap-1">
        {title ? <p className="font-semibold">{title}</p> : null}
        {children ? <div className="leading-relaxed">{children}</div> : null}
      </div>
    </div>
  );
}
