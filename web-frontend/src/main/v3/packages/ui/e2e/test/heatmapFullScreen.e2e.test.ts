import { test, expect } from '@playwright/test';

test.describe('Heatmap Full Screen UI', () => {
  test.beforeEach(async ({ page }) => {
    // Page requires application param in URL; without it the loader redirects to root.
    await page.goto('/heatmapFullScreenMode/TestApp@SPRING_BOOT');
  });

  test('The header is rendered.', async ({ page }) => {
    const mainHeader = page.locator('[data-testid="MainHeader"]');
    await expect(mainHeader).toBeVisible();
    await expect(mainHeader).toContainText('Servermap');
    await expect(mainHeader).toContainText('Heatmap');
  });

  test('Application selection button is rendered and disabled.', async ({ page }) => {
    // With application in URL the button shows app name and is disabled.
    const mainHeader = page.locator('[data-testid="MainHeader"]');
    const appButton = mainHeader.locator('button').first();
    await expect(appButton).toBeVisible();
    await expect(appButton).toBeDisabled();
  });
});
