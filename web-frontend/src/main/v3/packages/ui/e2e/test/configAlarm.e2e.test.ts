import { test, expect } from '@playwright/test';

test.describe('Configuration Alarm UI', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/config/alarm');
  });

  test('Application selection button is rendered.', async ({ page }) => {
    const selectAppButton = page.locator('button >> text=Select your application.');
    await expect(selectAppButton).toBeVisible();
  });

  test('Add alarm button is rendered and disabled without application selected.', async ({
    page,
  }) => {
    const addButton = page.locator('button', { hasText: 'Add' });
    await expect(addButton).toBeVisible();
    await expect(addButton).toBeDisabled();
  });
});
