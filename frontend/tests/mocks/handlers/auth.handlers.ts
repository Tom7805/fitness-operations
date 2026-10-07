import { http, HttpResponse } from 'msw';
import { managerSession } from '../data/auth';

const api = (path: string) => `*/api/v1${path}`;

/** Hành vi mặc định; từng kiểm thử ghi đè bằng server.use(...) theo kịch bản của mình. */
export const authHandlers = [
  http.get(api('/devices/current'), () => HttpResponse.json({ registered: false })),
  http.get(api('/auth/me'), () => HttpResponse.json(managerSession)),
  http.post(api('/auth/logout'), () => new HttpResponse(null, { status: 204 })),
];

export { api };
