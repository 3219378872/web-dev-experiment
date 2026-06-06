// @vitest-environment node

import { describe, expect, it } from 'vitest';
import viteConfig from '../../vite.config.js';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const proxy = viteConfig.server.proxy;
const testDir = path.dirname(fileURLToPath(import.meta.url));
const nginxConf = fs.readFileSync(path.resolve(testDir, '../../nginx.conf'), 'utf-8');

describe('frontend upload file routing', () => {
  it('proxies root-relative upload and file URLs through the Vite dev server', () => {
    for (const route of ['/files', '/upload']) {
      expect(proxy[route]).toEqual(
        expect.objectContaining({
          target: 'http://localhost:8080',
          changeOrigin: true,
        })
      );
      expect(proxy[route].rewrite).toBeUndefined();
    }
  });

  it('proxies root-relative upload and file URLs through production nginx', () => {
    expect(nginxConf).toContain('location /files/');
    expect(nginxConf).toContain('proxy_pass http://hm-gateway:8080/files/');
    expect(nginxConf).toContain('location /upload/');
    expect(nginxConf).toContain('proxy_pass http://hm-gateway:8080/upload/');
  });
});
