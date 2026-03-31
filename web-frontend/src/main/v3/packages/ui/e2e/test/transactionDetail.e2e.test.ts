import { test, expect } from '@playwright/test';

test.describe('Transaction Detail UI', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/transactionDetail');
  });

  test('The header is rendered.', async ({ page }) => {
    const mainHeader = page.locator('[data-testid="MainHeader"]');
    await expect(mainHeader).toBeVisible();
    await expect(mainHeader).toContainText('Transaction Detail');
  });

  test('Application selection button is rendered and disabled.', async ({ page }) => {
    const selectAppButton = page.locator('button >> text=Select your application.');
    await expect(selectAppButton).toBeVisible();
    await expect(selectAppButton).toBeDisabled();
  });
});
