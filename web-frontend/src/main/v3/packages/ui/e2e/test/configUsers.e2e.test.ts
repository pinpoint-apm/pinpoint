import { test, expect } from '@playwright/test';

test.describe('Configuration Users UI', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/config/users');
  });

  test('The Users heading is rendered.', async ({ page }) => {
    const heading = page.locator('h3', { hasText: 'Users' });
    await expect(heading).toBeVisible();
  });
});
