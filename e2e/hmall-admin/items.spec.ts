import { test, expect } from '@playwright/test';
import { setupAdminSession } from '../auth.setup';

test.describe('Admin Item Management', () => {
  test.beforeEach(async ({ page }) => {
    await setupAdminSession(page);
  });

  test('item list page loads', async ({ page }) => {
    await page.goto('/items');
    await expect(page.locator('.el-table, table, [class*="table"]').first()).toBeVisible({ timeout: 10000 });
  });

  test('user list page loads', async ({ page }) => {
    await page.goto('/users');
    await expect(page.locator('.el-table, table, [class*="table"]').first()).toBeVisible({ timeout: 10000 });
  });

  test('order list page loads', async ({ page }) => {
    await page.goto('/orders');
    await expect(page.locator('.el-table, table, [class*="table"]').first()).toBeVisible({ timeout: 10000 });
  });

  test('dashboard loads with charts', async ({ page }) => {
    await page.goto('/dashboard');
    await expect(page.locator('canvas, .el-card, [class*="chart"], [class*="stat"]').first()).toBeVisible({ timeout: 10000 });
  });
});
