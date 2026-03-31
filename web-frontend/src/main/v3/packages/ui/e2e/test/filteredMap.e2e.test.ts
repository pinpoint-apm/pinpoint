import { test, expect } from '@playwright/test';

test.describe('Filtered Map UI', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/filteredMap');
  });

  test('The header is rendered.', async ({ page }) => {
    const mainHeader = page.locator('[data-testid="MainHeader"]');
    await expect(mainHeader).toBeVisible();
    await expect(mainHeader).toContainText('Servermap');
    await expect(mainHeader).toContainText('Filtered');
  });

  test('Application selection button is rendered and disabled.', async ({ page }) => {
    const selectAppButton = page.locator('button >> text=Select your application.');
    await expect(selectAppButton).toBeVisible();
    await expect(selectAppButton).toBeDisabled();
  });
});
