import { test, expect } from '@playwright/test';

test.describe('Authentication', () => {
  test('login with valid credentials', async ({ page }) => {
    await page.goto('/login');
    await page
      .locator('input[placeholder*="用户名"], input[placeholder*="username"], .username input')
      .first()
      .fill('testuser');
    await page.locator('input[type="password"]').first().fill('admin123');
    await page.locator('button:has-text("登录"), button:has-text("Login")').first().click();
    await page.waitForURL('**/');
    const token = await page.evaluate(() => localStorage.getItem('token'));
    expect(token).toBeTruthy();
  });

  test('login with wrong password shows error', async ({ page }) => {
    await page.goto('/login');
    await page
      .locator('input[placeholder*="用户名"], input[placeholder*="username"], .username input')
      .first()
      .fill('testuser');
    await page.locator('input[type="password"]').first().fill('wrongpassword');
    await page.locator('button:has-text("登录"), button:has-text("Login")').first().click();
    await expect(
      page.locator('.el-message--error, .el-notification, [class*="error"]').first()
    ).toBeVisible({ timeout: 5000 });
  });
});
