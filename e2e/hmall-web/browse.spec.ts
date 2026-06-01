import { test, expect } from '@playwright/test';

test.describe('Browse Items', () => {
  test('home page loads with banner and items', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('.el-carousel, .banner, img[alt]').first()).toBeVisible({
      timeout: 15000,
    });
    await expect(page.locator('.product-card, .el-card, [class*="item"]').first()).toBeVisible({
      timeout: 15000,
    });
  });

  test('search returns results', async ({ page }) => {
    await page.goto('/');
    const searchInput = page
      .locator('input[placeholder*="搜索"], input[placeholder*="search"], .search-input input')
      .first();
    await searchInput.fill('测试');
    await searchInput.press('Enter');
    await page.waitForURL(/search/);
    await expect(page.locator('.product-card, .el-card, [class*="item"]').first()).toBeVisible({
      timeout: 15000,
    });
  });

  test('category page loads', async ({ page }) => {
    await page.goto('/category');
    await expect(page.locator('.category-card, .el-card').first()).toBeVisible({ timeout: 15000 });
  });
});
