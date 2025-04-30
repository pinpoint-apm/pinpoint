import { test, expect } from '@playwright/test';

test.describe('SystemMetric UI', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/systemMetric');
  });

  test('The header is rendered.', async ({ page }) => {
    // Check header
    const mainHeader = page.locator('[data-testid="MainHeader"]');
    await expect(mainHeader).toBeVisible();
    await expect(mainHeader).toContainText('System Metric');
  });

  test('Host group popover renders with input and list items.', async ({ page }) => {
    const popover = page.locator('role=dialog');
    await expect(popover).toBeVisible();

    const input = popover.locator('input[placeholder="Input host-group name"]');
    await expect(input).toBeVisible();
    await expect(input).toBeEnabled();
  });
});
