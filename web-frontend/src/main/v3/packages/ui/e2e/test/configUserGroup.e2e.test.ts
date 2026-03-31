import { test, expect } from '@playwright/test';

test.describe('Configuration User Group UI', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/config/userGroup');
  });

  test('The User Group heading is rendered.', async ({ page }) => {
    const heading = page.locator('h3', { hasText: 'User Group' });
    await expect(heading).toBeVisible();
  });
});
