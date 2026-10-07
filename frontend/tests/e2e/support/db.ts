import { execFileSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';

const composeFile = fileURLToPath(new URL('../../../../docker-compose.dev.yml', import.meta.url));

/** Chạy một câu SQL trên PostgreSQL của docker-compose.dev.yml; trả về các dòng, cột phân tách bằng "|". */
export function sql(statement: string): string[][] {
  const output = execFileSync(
    'docker',
    [
      'compose',
      '-f',
      composeFile,
      'exec',
      '-T',
      'postgres',
      'psql',
      '-U',
      process.env.DB_USERNAME ?? 'fitness',
      '-d',
      process.env.DB_NAME ?? 'fitness_operations',
      '-At',
      '-F',
      '|',
      '-c',
      statement,
    ],
    { encoding: 'utf8', stdio: ['ignore', 'pipe', 'pipe'] },
  );
  return output
    .split('\n')
    .map((line) => line.trim())
    .filter((line) => line.length > 0)
    .map((line) => line.split('|'));
}

/** Gỡ tạm khóa của các tài khoản mẫu để có thể chạy lại kiểm thử ngay. */
export function resetLoginLocks(): void {
  sql('UPDATE users SET failed_login_attempts = 0, locked_until = NULL');
}
