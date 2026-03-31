import { test, expect } from '@playwright/test';

test.describe('Configuration Webhook UI', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/config/webhook');
  });

  test('Application selection button is rendered.', async ({ page }) => {
    const selectAppButton = page.locator('button >> text=Select your application.');
    await expect(selectAppButton).toBeVisible();
  });

  test('Create webhook button is rendered and disabled without application selected.', async ({
    page,
  }) => {
    const createButton = page.locator('button', { hasText: 'Create' });
    await expect(createButton).toBeVisible();
    await expect(createButton).toBeDisabled();
  });
});
