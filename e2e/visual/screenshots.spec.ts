import { test } from '@playwright/test';
import path from 'path';

const SCREENSHOT_DIR = path.join(__dirname, 'screenshots');

test.describe('Visual Regression — hmall-web', () => {
  test('home page', async ({ page }) => {
    await page.goto('/');
    await page.waitForTimeout(2000);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, 'web-home.png'), fullPage: true });
  });

  test('search results', async ({ page }) => {
    await page.goto('/search?q=测试');
    await page.waitForTimeout(2000);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, 'web-search.png'), fullPage: true });
  });

  test('category page', async ({ page }) => {
    await page.goto('/category');
    await page.waitForTimeout(1000);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, 'web-category.png'), fullPage: true });
  });

  test('login page', async ({ page }) => {
    await page.goto('/login');
    await page.waitForTimeout(1000);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, 'web-login.png'), fullPage: true });
  });
});

test.describe('Visual Regression — hmall-admin', () => {
  test('admin login page', async ({ page }) => {
    await page.goto('http://localhost:5174/login');
    await page.waitForTimeout(1000);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, 'admin-login.png'), fullPage: true });
  });
});
