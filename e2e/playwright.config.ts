import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: '.',
  timeout: 30000,
  expect: { timeout: 10000 },
  fullyParallel: false,
  retries: 0,
  workers: 1,
  reporter: 'list',
  use: {
    baseURL: 'http://localhost:5173',
    trace: 'on-first-retry',
    actionTimeout: 10000,
  },
  projects: [
    {
      name: 'hmall-web',
      testMatch: 'hmall-web/*.spec.ts',
      use: {
        baseURL: 'http://localhost:5173',
        ...devices['Desktop Chrome'],
      },
    },
    {
      name: 'hmall-admin',
      testMatch: 'hmall-admin/*.spec.ts',
      use: {
        baseURL: 'http://localhost:5174',
        ...devices['Desktop Chrome'],
      },
    },
  ],
});
