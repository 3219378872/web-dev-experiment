import { defineConfig, devices } from '@playwright/test';

// Auto-start both Vite dev servers when running e2e locally.
// `reuseExistingServer: true` means if you've already run `npm run dev`
// for either frontend, Playwright reuses your running process instead of
// spawning a new one. Backend gateway (8080) must be up separately —
// either `docker compose up -d` or a local Spring Boot run; see CLAUDE.md.

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
  webServer: [
    {
      command: 'npm --prefix ../hmall-web run dev -- --host --port 5173 --strictPort',
      url: 'http://localhost:5173',
      reuseExistingServer: true,
      timeout: 120_000,
    },
    {
      command: 'npm --prefix ../hmall-admin run dev -- --host --port 5174 --strictPort',
      url: 'http://localhost:5174',
      reuseExistingServer: true,
      timeout: 120_000,
    },
  ],
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
