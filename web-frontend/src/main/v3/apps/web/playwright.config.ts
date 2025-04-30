import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: '../../',
  testMatch: ['*.e2e.test.ts'],
  timeout: 30 * 1000,
  reporter: 'list',
  use: {
    baseURL: 'http://localhost:3000',
    browserName: 'chromium',
    headless: true,
    viewport: { width: 1280, height: 720 },
    ignoreHTTPSErrors: true,
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },
});
