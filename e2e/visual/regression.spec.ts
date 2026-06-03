import { test, expect, Page } from '@playwright/test';
import * as fs from 'fs';
import * as path from 'path';
import { compareScreenshots, RegressionResult } from './utils';
// Fixtures available for per-page mocking; import as needed.
// import { ... } from './fixtures';

const RESULTS_DIR = path.join(__dirname, 'results');
const BASELINE_DIR = path.join(RESULTS_DIR, 'baselines');
const ACTUAL_DIR = path.join(RESULTS_DIR, 'actual');
const DIFF_DIR = path.join(RESULTS_DIR, 'diff');

for (const dir of [RESULTS_DIR, BASELINE_DIR, ACTUAL_DIR, DIFF_DIR]) {
  if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true });
}

const PROTOTYPE_BASE = 'http://localhost:8888';
const WEB_BASE = 'http://localhost:5173';
const ADMIN_BASE = 'http://localhost:5174';

interface PageCase {
  name: string;
  prototypePath: string;
  appUrl: string;
  appSetup?: (page: Page) => Promise<void>;
  /** 某些页面不需要严格对比（如包含地图、实时数据），可单独放宽容差 */
  tolerance?: number;
}

/* ─────────── 客户端页面 ─────────── */
const webCases: PageCase[] = [
  { name: 'web-home', prototypePath: '/c/index.html', appUrl: `${WEB_BASE}/` },
  { name: 'web-category', prototypePath: '/c/category.html', appUrl: `${WEB_BASE}/category` },
  { name: 'web-search', prototypePath: '/c/search.html', appUrl: `${WEB_BASE}/search?q=测试` },
  { name: 'web-product', prototypePath: '/c/product.html', appUrl: `${WEB_BASE}/item/1` },
  { name: 'web-login', prototypePath: '/c/login.html', appUrl: `${WEB_BASE}/login` },
  { name: 'web-register', prototypePath: '/c/register.html', appUrl: `${WEB_BASE}/register` },
  { name: 'web-forgot', prototypePath: '/c/forgot.html', appUrl: `${WEB_BASE}/forgot` },
  {
    name: 'web-cart',
    prototypePath: '/c/cart.html',
    appUrl: `${WEB_BASE}/cart`,
    appSetup: injectWebAuth,
  },
  {
    name: 'web-checkout',
    prototypePath: '/c/checkout.html',
    appUrl: `${WEB_BASE}/order/confirm`,
    appSetup: injectWebAuth,
  },
  {
    name: 'web-orders',
    prototypePath: '/c/orders.html',
    appUrl: `${WEB_BASE}/orders`,
    appSetup: injectWebAuth,
  },
  {
    name: 'web-order-detail',
    prototypePath: '/c/order-detail.html',
    appUrl: `${WEB_BASE}/order/1`,
    appSetup: injectWebAuth,
  },
  {
    name: 'web-profile',
    prototypePath: '/c/profile.html',
    appUrl: `${WEB_BASE}/profile`,
    appSetup: injectWebAuth,
  },
  {
    name: 'web-address',
    prototypePath: '/c/address.html',
    appUrl: `${WEB_BASE}/addresses`,
    appSetup: injectWebAuth,
  },
  {
    name: 'web-favorites',
    prototypePath: '/c/favorites.html',
    appUrl: `${WEB_BASE}/favorites`,
    appSetup: injectWebAuth,
  },
  {
    name: 'web-feedback',
    prototypePath: '/c/feedback.html',
    appUrl: `${WEB_BASE}/feedback`,
    appSetup: injectWebAuth,
  },
  {
    name: 'web-notifications',
    prototypePath: '/c/announcements.html',
    appUrl: `${WEB_BASE}/notifications`,
    appSetup: injectWebAuth,
  },
  {
    name: 'web-coupons',
    prototypePath: '/c/coupons.html',
    appUrl: `${WEB_BASE}/coupons`,
    appSetup: injectWebAuth,
  },
  { name: 'web-flashsale', prototypePath: '/c/flashsale.html', appUrl: `${WEB_BASE}/flashsale` },
  { name: 'web-service', prototypePath: '/c/service.html', appUrl: `${WEB_BASE}/service` },
];

/* ─────────── 管理后台页面 ─────────── */
const adminCases: PageCase[] = [
  { name: 'admin-login', prototypePath: '/admin/login.html', appUrl: `${ADMIN_BASE}/login` },
  {
    name: 'admin-dashboard',
    prototypePath: '/admin/dashboard.html',
    appUrl: `${ADMIN_BASE}/dashboard`,
    appSetup: injectAdminAuth,
  },
  {
    name: 'admin-products',
    prototypePath: '/admin/products.html',
    appUrl: `${ADMIN_BASE}/items`,
    appSetup: injectAdminAuth,
  },
  {
    name: 'admin-product-edit',
    prototypePath: '/admin/product-edit.html',
    appUrl: `${ADMIN_BASE}/items/1/edit`,
    appSetup: injectAdminAuth,
  },
  {
    name: 'admin-categories',
    prototypePath: '/admin/categories.html',
    appUrl: `${ADMIN_BASE}/categories`,
    appSetup: injectAdminAuth,
  },
  {
    name: 'admin-orders',
    prototypePath: '/admin/orders.html',
    appUrl: `${ADMIN_BASE}/orders`,
    appSetup: injectAdminAuth,
  },
  {
    name: 'admin-order-detail',
    prototypePath: '/admin/order-detail.html',
    appUrl: `${ADMIN_BASE}/orders/1`,
    appSetup: injectAdminAuth,
  },
  {
    name: 'admin-users',
    prototypePath: '/admin/users.html',
    appUrl: `${ADMIN_BASE}/users`,
    appSetup: injectAdminAuth,
  },
  {
    name: 'admin-reviews',
    prototypePath: '/admin/reviews.html',
    appUrl: `${ADMIN_BASE}/reviews`,
    appSetup: injectAdminAuth,
  },
  {
    name: 'admin-banners',
    prototypePath: '/admin/banners.html',
    appUrl: `${ADMIN_BASE}/banners`,
    appSetup: injectAdminAuth,
  },
  {
    name: 'admin-announcements',
    prototypePath: '/admin/announcements.html',
    appUrl: `${ADMIN_BASE}/notifications`,
    appSetup: injectAdminAuth,
  },
  {
    name: 'admin-feedback',
    prototypePath: '/admin/feedback.html',
    appUrl: `${ADMIN_BASE}/feedbacks`,
    appSetup: injectAdminAuth,
  },
  {
    name: 'admin-settings',
    prototypePath: '/admin/settings.html',
    appUrl: `${ADMIN_BASE}/profile`,
    appSetup: injectAdminAuth,
  },
];

const allCases = [...webCases, ...adminCases];

function saveResult(result: RegressionResult): void {
  const file = path.join(RESULTS_DIR, `${result.name}.result.json`);
  fs.writeFileSync(file, JSON.stringify(result, null, 2));
}

function loadAllResults(): RegressionResult[] {
  const files = fs.readdirSync(RESULTS_DIR).filter((f) => f.endsWith('.result.json'));
  return files.map((f) => JSON.parse(fs.readFileSync(path.join(RESULTS_DIR, f), 'utf-8')));
}

async function injectWebAuth(page: Page): Promise<void> {
  await page.goto(`${WEB_BASE}/login`);
  await page.evaluate(() => localStorage.setItem('token', 'mock-token-visual'));
}

async function injectAdminAuth(page: Page): Promise<void> {
  await page.goto(`${ADMIN_BASE}/login`);
  await page.evaluate(() => localStorage.setItem('adminToken', 'mock-admin-token-visual'));
}

async function waitForStable(page: Page): Promise<void> {
  // 等待网络空闲 + 字体加载 + 给图片/动画充分时间
  await page.waitForLoadState('networkidle', { timeout: 15000 }).catch(() => {
    /* 部分页面有轮询，networkidle 可能永不触发，忽略 */
  });
  await page
    .waitForFunction(() => document.fonts.ready)
    .catch(() => {
      /* 部分环境不支持 document.fonts，忽略 */
    });
  await page.waitForTimeout(1500);
}

/** 认证页统一预处理：填入与原型一致的占位值，使截图可比 */
async function prefillAuthForm(page: Page, type: 'web' | 'admin'): Promise<void> {
  if (type === 'web') {
    // 根据当前 mode 判断是登录还是注册/找回
    const mode = await page.evaluate(() => {
      const h2 = document.querySelector('.auth-card h2');
      if (h2) return h2.textContent;
      const activeTab = document.querySelector('.auth-card .tabs .active');
      if (activeTab) return activeTab.textContent;
      return 'login';
    });
    const text = String(mode || '');
    if (text.includes('登录') || text.includes('账号登录')) {
      await page.locator('.auth-card input').first().fill('linxiao@haoji.com');
      const pw = page.locator('.auth-card input[type="password"]').first();
      if (await pw.isVisible().catch(() => false)) await pw.fill('password123');
    } else if (text.includes('注册') || text.includes('创建账号')) {
      const inputs = page.locator('.auth-card input');
      if (
        await inputs
          .nth(0)
          .isVisible()
          .catch(() => false)
      )
        await inputs.nth(0).fill('newuser');
      if (
        await inputs
          .nth(1)
          .isVisible()
          .catch(() => false)
      )
        await inputs.nth(1).fill('new@haoji.com');
    } else if (text.includes('找回') || text.includes('重置')) {
      const inputs = page.locator('.auth-card input');
      if (
        await inputs
          .nth(0)
          .isVisible()
          .catch(() => false)
      )
        await inputs.nth(0).fill('new@haoji.com');
    }
  } else {
    // admin login
    await page.locator('.al-card input').first().fill('admin@haoji.com');
    const pw = page.locator('.al-card input[type="password"]').first();
    if (await pw.isVisible().catch(() => false)) await pw.fill('admin123456');
    const cap = page.locator('.al-card .cap-row input').first();
    if (await cap.isVisible().catch(() => false)) await cap.fill('7Hq3');
  }
}

/* ─────────── 测试主体 ─────────── */

test.describe('Visual Regression — Prototype vs Implementation', () => {
  for (const tc of allCases) {
    test(tc.name, async ({ page }) => {
      // 固定视口，消除滚动条差异对截图的影响
      await page.setViewportSize({ width: 1440, height: 900 });

      // 1️⃣ 原型截图（baseline）
      const protoUrl = `${PROTOTYPE_BASE}${tc.prototypePath}`;
      await page.goto(protoUrl, { waitUntil: 'domcontentloaded' });
      await waitForStable(page);
      const baselinePath = path.join(BASELINE_DIR, `${tc.name}.png`);
      await page.screenshot({ path: baselinePath, fullPage: false });

      // 2️⃣ 实现截图（actual）
      if (tc.appSetup) {
        await tc.appSetup(page);
      }
      await page.goto(tc.appUrl, { waitUntil: 'domcontentloaded' });
      await waitForStable(page);

      // 隐藏滚动条避免布局偏移差异
      await page.addStyleTag({
        content: '::-webkit-scrollbar{display:none} body{scrollbar-width:none}',
      });

      // 认证页预填表单，使截图与原型状态可比
      if (tc.name === 'web-login' || tc.name === 'web-register' || tc.name === 'web-forgot') {
        await prefillAuthForm(page, 'web');
      } else if (tc.name === 'admin-login') {
        await prefillAuthForm(page, 'admin');
      }

      const actualPath = path.join(ACTUAL_DIR, `${tc.name}.png`);
      await page.screenshot({ path: actualPath, fullPage: false });

      // 3️⃣ 像素级比对
      const diffPath = path.join(DIFF_DIR, `${tc.name}.png`);
      const tolerance = tc.tolerance ?? 0.05; // 默认 5% 门控容差
      const compare = compareScreenshots(baselinePath, actualPath, diffPath, 0.1);
      const result: RegressionResult = {
        name: tc.name,
        ...compare,
        baselinePath,
        actualPath,
        diffPath,
      };
      saveResult(result);

      // 断言：差异像素占比必须小于 tolerance
      expect(
        result.diffRatio,
        `${tc.name} 视觉差异 ${(result.diffRatio * 100).toFixed(2)}%（阈值 ${(tolerance * 100).toFixed(1)}%）`
      ).toBeLessThan(tolerance);
    });
  }

  test.afterAll(async () => {
    // Note: Playwright may restart workers between failing tests,
    // so this hook can run multiple times. Report generation is
    // done via `npm run test:visual:report` after the test run.
    const allResults = loadAllResults();
    // eslint-disable-next-line no-console
    console.log(`\n📸 ${allResults.length} visual result(s) written to ${RESULTS_DIR}`);
  });
});
