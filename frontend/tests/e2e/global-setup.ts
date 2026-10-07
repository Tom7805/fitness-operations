import { resetLoginLocks } from './support/db';

export default function globalSetup(): void {
  resetLoginLocks();
}
