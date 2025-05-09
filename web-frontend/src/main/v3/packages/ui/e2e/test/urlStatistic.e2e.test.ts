import { test, expect } from '@playwright/test';

test.describe('Url Statistic UI', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/urlStatistic');
  });

  test('The header is rendered.', async ({ page }) => {
    // Check header
    const mainHeader = page.locator('[data-testid="MainHeader"]');
    await expect(mainHeader).toBeVisible();
    await expect(mainHeader).toContainText('URL Statistic');
  });

  test('Application selection button is rendered.', async ({ page }) => {
    const selectAppButton = page.locator('button >> text=Select your application.');
    await expect(selectAppButton).toBeVisible();
    await expect(selectAppButton).toBeEnabled();
  });
});
