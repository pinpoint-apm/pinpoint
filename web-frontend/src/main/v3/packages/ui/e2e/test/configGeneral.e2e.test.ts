import { test, expect } from '@playwright/test';

test.describe('Configuration General UI', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/config/general');
  });

  test('The General heading is rendered.', async ({ page }) => {
    const heading = page.locator('h3', { hasText: 'General' });
    await expect(heading).toBeVisible();
  });

  test('Language select is rendered.', async ({ page }) => {
    const languageSelect = page.locator('button[role="combobox"]').first();
    await expect(languageSelect).toBeVisible();
    await expect(languageSelect).toBeEnabled();
  });

  test('Three setting selects are rendered (language, date format, timezone).', async ({ page }) => {
    const selects = page.locator('button[role="combobox"]');
    await expect(selects).toHaveCount(3);
  });
});
