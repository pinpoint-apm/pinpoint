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

  test('Host group selection button shows placeholder when no group is selected.', async ({
    page,
  }) => {
    const triggerButton = page.locator('button >> text=Select your host-group');
    await expect(triggerButton).toBeVisible();
    await expect(triggerButton).toBeEnabled();
  });

  test('Host group popover auto-opens with search input when no group is selected.', async ({
    page,
  }) => {
    const popover = page.locator('role=dialog');
    await expect(popover).toBeVisible();

    const input = popover.locator('input[placeholder="Input host-group name"]');
    await expect(input).toBeVisible();
    await expect(input).toBeEnabled();
  });
});
