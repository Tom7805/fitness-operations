import axios from 'axios';
import { env } from '@/config/env';
import { HTTP_TIMEOUT_MS } from '@/constants/app';
import { attachInterceptors } from './interceptors';

export const httpClient = axios.create({
  baseURL: env.apiBaseUrl,
  timeout: HTTP_TIMEOUT_MS,
  headers: { Accept: 'application/json' },
});

attachInterceptors(httpClient);
