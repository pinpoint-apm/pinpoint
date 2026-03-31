import { test, expect } from '@playwright/test';

test.describe('Thread Dump UI', () => {
  test.beforeEach(async ({ page }) => {
    // Page requires both application and agentId; without them the loader redirects to serverMap.
    await page.goto('/threadDump/TestApp@SPRING_BOOT?agentId=testAgent');
  });

  test('The header is rendered.', async ({ page }) => {
    const mainHeader = page.locator('[data-testid="MainHeader"]');
    await expect(mainHeader).toBeVisible();
    await expect(mainHeader).toContainText('Thread Dump');
  });

  test('Application selection button is rendered and disabled.', async ({ page }) => {
    // With application in URL the button shows app name and is disabled.
    const mainHeader = page.locator('[data-testid="MainHeader"]');
    const appButton = mainHeader.locator('button').first();
    await expect(appButton).toBeVisible();
    await expect(appButton).toBeDisabled();
  });
});
