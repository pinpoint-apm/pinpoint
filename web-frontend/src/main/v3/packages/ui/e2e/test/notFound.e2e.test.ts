import { test, expect } from '@playwright/test';

test.describe('Not Found (404) UI', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/this-page-does-not-exist');
  });

  test('The 404 alert is rendered.', async ({ page }) => {
    const alert = page.locator('role=alert');
    await expect(alert).toBeVisible();
    await expect(alert).toContainText('404 Page Not Found');
  });

  test('Back to main page link is rendered.', async ({ page }) => {
    const backLink = page.locator('a', { hasText: 'Back to main page.' });
    await expect(backLink).toBeVisible();
    await expect(backLink).toHaveAttribute('href', '/');
  });
});
