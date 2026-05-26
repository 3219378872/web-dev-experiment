import { test, expect } from '@playwright/test';

test.describe('Admin Login', () => {
  test('admin login page loads', async ({ page }) => {
    await page.goto('/login');
    await expect(page.locator('input[placeholder*="用户名"], input[placeholder*="username"]').first()).toBeVisible({ timeout: 10000 });
  });

  test('admin login with valid credentials redirects to dashboard', async ({ page }) => {
    await page.goto('/login');
    await page.locator('input[placeholder*="用户名"], input[placeholder*="username"]').first().fill('admin');
    await page.locator('input[type="password"]').first().fill('admin123');
    await page.locator('button:has-text("登录"), button:has-text("Login")').first().click();
    await page.waitForURL('**/dashboard', { timeout: 10000 });
    const token = await page.evaluate(() => localStorage.getItem('adminToken'));
    expect(token).toBeTruthy();
  });
});
