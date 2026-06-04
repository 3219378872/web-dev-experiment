import { describe, it, expect, vi } from 'vitest';
import { mount } from '@vue/test-utils';

// App.vue 通过 useRoute() 读取 route.path 决定布局，单测需提供路由上下文；
// 仅覆盖 useRoute，保留 createRouter 等真实导出（router/index.js 在导入链上需要）。
vi.mock('vue-router', async (importOriginal) => {
  const actual = await importOriginal();
  return {
    ...actual,
    useRoute: () => ({ path: '/' }),
  };
});

import App from '@/App.vue';

describe('App.vue', () => {
  it('mounts without crashing', () => {
    const wrapper = mount(App, {
      global: {
        stubs: {
          AppHeader: true,
          AppFooter: true,
          'router-view': true,
        },
      },
    });
    expect(wrapper.exists()).toBe(true);
  });
});
