import { test, expect } from '@playwright/test';

test.describe('Configuration Help UI', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/config/help');
  });

  test('The Help heading is rendered.', async ({ page }) => {
    const heading = page.locator('h3', { hasText: 'Help' });
    await expect(heading).toBeVisible();
  });

  test('Document & Guide section is rendered.', async ({ page }) => {
    const docHeading = page.locator('h4', { hasText: 'Document & Guide' });
    await expect(docHeading).toBeVisible();
  });

  test('Community section is rendered.', async ({ page }) => {
    const communityHeading = page.locator('h4', { hasText: 'Community' });
    await expect(communityHeading).toBeVisible();
  });
});
