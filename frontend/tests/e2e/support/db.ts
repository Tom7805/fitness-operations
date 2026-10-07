import { execFileSync } from 'node:child_process';

/** Trình khách mysql; trên Windows mặc định là bản đi kèm MySQL Server 8.0. Đặt MYSQL_CLI để dùng đường dẫn khác. */
const mysqlCli =
  process.env.MYSQL_CLI ??
  (process.platform === 'win32'
    ? 'C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysql.exe'
    : 'mysql');

/** Chạy một câu SQL trên database dev (MySQL); trả về các dòng, mỗi dòng là mảng giá trị cột. */
export function sql(statement: string): string[][] {
  const output = execFileSync(
    mysqlCli,
    [
      `--host=${process.env.E2E_DB_HOST ?? '127.0.0.1'}`,
      `--port=${process.env.E2E_DB_PORT ?? '3306'}`,
      `--user=${process.env.E2E_DB_USERNAME ?? 'fitness'}`,
      `--password=${process.env.E2E_DB_PASSWORD ?? 'fitness'}`,
      '--default-character-set=utf8mb4',
      '--batch',
      '--skip-column-names',
      `--database=${process.env.E2E_DB_NAME ?? 'fitness_operations'}`,
      `--execute=${statement}`,
    ],
    { encoding: 'utf8', stdio: ['ignore', 'pipe', 'pipe'] },
  );
  return output
    .split(/\r?\n/)
    .filter((line) => line.length > 0)
    .map((line) => line.split('\t'));
}

/** Gỡ tạm khóa của các tài khoản mẫu để có thể chạy lại kiểm thử ngay. */
export function resetLoginLocks(): void {
  sql('UPDATE users SET failed_login_attempts = 0, locked_until = NULL');
}
