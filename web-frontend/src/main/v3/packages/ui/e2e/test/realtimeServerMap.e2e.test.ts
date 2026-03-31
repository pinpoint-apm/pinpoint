import { test, expect } from '@playwright/test';

test.describe('Realtime Server Map UI', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/serverMap/realtime');
  });

  test('The header is rendered.', async ({ page }) => {
    const mainHeader = page.locator('[data-testid="MainHeader"]');
    await expect(mainHeader).toBeVisible();
    await expect(mainHeader).toContainText('Servermap');
  });

  test('Application selection button is rendered and enabled.', async ({ page }) => {
    const selectAppButton = page.locator('button >> text=Select your application.');
    await expect(selectAppButton).toBeVisible();
    await expect(selectAppButton).toBeEnabled();
  });
});
