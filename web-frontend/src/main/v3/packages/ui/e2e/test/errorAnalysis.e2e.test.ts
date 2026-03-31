import { test, expect } from '@playwright/test';

test.describe('Error Analysis UI', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/errorAnalysis');
  });

  test('The header is rendered.', async ({ page }) => {
    // Check header
    const mainHeader = page.locator('[data-testid="MainHeader"]');
    await expect(mainHeader).toBeVisible();
    await expect(mainHeader).toContainText('Error Analysis');
  });

  test('Application selection button is rendered.', async ({ page }) => {
    const selectAppButton = page.locator('button >> text=Select your application.');
    await expect(selectAppButton).toBeVisible();
    await expect(selectAppButton).toBeEnabled();
  });

  test('Application list popover auto-opens when no application is selected.', async ({ page }) => {
    const popover = page.locator('role=dialog');
    await expect(popover).toBeVisible();

    const searchInput = popover.locator('input[placeholder="Input application name."]');
    await expect(searchInput).toBeVisible();
    await expect(searchInput).toBeEnabled();
  });
});
