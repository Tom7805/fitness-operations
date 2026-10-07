export const queryKeys = {
  auth: {
    me: ['auth', 'me'] as const,
  },
  devices: {
    current: ['devices', 'current'] as const,
    registrableBranches: ['devices', 'registrable-branches'] as const,
  },
} as const;
