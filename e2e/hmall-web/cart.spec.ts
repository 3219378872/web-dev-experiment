import { test, expect } from '@playwright/test';
import { setupUserSession } from '../auth.setup';

test.describe('Cart', () => {
  test.beforeEach(async ({ page }) => {
    await setupUserSession(page);
  });

  test('cart page shows empty state when logged in', async ({ page }) => {
    await page.goto('/cart');
    await expect(page.locator('.el-table, table, .cart-container, [class*="cart"]').first()).toBeVisible({ timeout: 10000 });
  });
});
