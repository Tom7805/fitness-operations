import { readdirSync, statSync } from 'node:fs';
import { join, relative } from 'node:path';
import { defineConfig, mergeConfig } from 'vitest/config';
import viteConfig from './vite.config.ts';

const TEST_FILE = /\.test\.(ts|tsx)$/;

/**
 * Skeleton của dự án có sẵn nhiều file test rỗng cho các tính năng chưa làm. Vitest coi file test rỗng là lỗi,
 * nên chỉ nạp các file test đã có nội dung; file rỗng tự được nạp khi tính năng tương ứng được viết test.
 */
function implementedTestFiles(dir: string): string[] {
  return readdirSync(dir, { withFileTypes: true }).flatMap((entry) => {
    const path = join(dir, entry.name);
    if (entry.isDirectory()) {
      return entry.name === 'node_modules' ? [] : implementedTestFiles(path);
    }
    return TEST_FILE.test(entry.name) && statSync(path).size > 0
      ? [relative(process.cwd(), path).replaceAll('\\', '/')]
      : [];
  });
}

export default mergeConfig(
  viteConfig,
  defineConfig({
    test: {
      environment: 'jsdom',
      setupFiles: ['./tests/setup.ts'],
      include: implementedTestFiles('src'),
      css: false,
      restoreMocks: true,
      unstubGlobals: true,
    },
  }),
);
