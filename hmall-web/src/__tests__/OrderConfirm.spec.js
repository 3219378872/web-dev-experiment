import { describe, it, expect, vi } from 'vitest';
import { mount } from '@vue/test-utils';

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: vi.fn() }),
  RouterLink: {
    props: ['to'],
    template: '<a><slot /></a>',
  },
}));

vi.mock('@/stores/cart', () => ({
  useCartStore: () => ({
    selectedItems: [{ id: 101, name: '测试商品', price: 1000, num: 2, spec: '默认' }],
    totalCount: 2,
    totalAmount: 2000,
    fetchCart: vi.fn().mockResolvedValue(undefined),
  }),
}));

vi.mock('@/api/address', () => ({
  getAddresses: vi.fn().mockResolvedValue([
    {
      id: 1,
      receiverName: '张三',
      phone: '13812345678',
      province: '浙江省',
      city: '杭州市',
      district: '西湖区',
      detail: '文一路 1 号',
      isDefault: true,
    },
  ]),
}));

vi.mock('@/api/order', () => ({
  createOrder: vi.fn().mockResolvedValue(99),
  getFreight: vi.fn().mockResolvedValue({ freight: 0 }),
  getAvailableCoupons: vi.fn().mockResolvedValue([]),
}));

vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn(),
  },
  ElEmpty: {
    props: ['description'],
    template: '<div class="empty">{{ description }}</div>',
  },
}));

import OrderConfirm from '@/views/OrderConfirm.vue';

describe('OrderConfirm supported checkout surface', () => {
  it('does not render unsupported payment channels or unsupported order metadata controls', () => {
    const wrapper = mount(OrderConfirm, {
      global: {
        stubs: {
          RouterLink: true,
          'el-empty': true,
        },
      },
    });

    const text = wrapper.text();
    expect(text).toContain('余额支付');
    expect(text).not.toContain('支付宝');
    expect(text).not.toContain('微信支付');
    expect(text).not.toContain('配送时间');
    expect(text).not.toContain('订单备注');
    expect(text).not.toContain('当日达');
    expect(text).toContain('当前订单暂不支持在线开票');
    expect(wrapper.find('select').exists()).toBe(false);
    expect(wrapper.find('input[placeholder*="商家留言"]').exists()).toBe(false);
  });
});
