import { Page } from '@playwright/test';

const BASE_API = 'http://localhost:8080';

export async function loginAs(
  page: Page,
  username = 'testuser',
  password = 'admin123'
): Promise<string> {
  const resp = await page.request.post(`${BASE_API}/users/login`, {
    data: { username, password },
  });
  const body = await resp.json();
  return body.data?.token || body.token || '';
}

export async function loginAsAdmin(page: Page): Promise<string> {
  const resp = await page.request.post(`${BASE_API}/users/login`, {
    data: { username: 'admin', password: 'admin123' },
  });
  const body = await resp.json();
  return body.data?.token || body.token || '';
}

export async function injectToken(page: Page, token: string, key = 'token'): Promise<void> {
  await page.goto('/');
  await page.evaluate(
    ({ k, v }) => {
      localStorage.setItem(k, v);
    },
    { k: key, v: token }
  );
}

export async function setupUserSession(page: Page): Promise<void> {
  const token = await loginAs(page);
  await injectToken(page, token, 'token');
}

export async function setupAdminSession(page: Page): Promise<void> {
  const token = await loginAsAdmin(page);
  await injectToken(page, token, 'adminToken');
}
