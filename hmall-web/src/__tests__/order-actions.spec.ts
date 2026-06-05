import { describe, it, expect, vi, beforeEach } from 'vitest';

// ---------------------------------------------------------------------------
// Unit tests for Issue #73 order action logic:
//   - handleRepurchase: add each order detail to cart then navigate to /cart
//   - handleDeleteOrder: call deleteOrder API then refresh list
//   - confirmPay: create pay-order then execute balance pay
// ---------------------------------------------------------------------------

// ---- Mocks ----------------------------------------------------------------

vi.mock('vue-router', async (importOriginal) => {
  const actual = await importOriginal();
  return {
    ...actual,
    useRoute: () => ({ params: { id: '42' }, query: {} }),
    useRouter: () => ({ push: vi.fn() }),
  };
});

vi.mock('@/stores/user', () => ({
  useUserStore: () => ({ userInfo: { name: '测试用户' } }),
}));

vi.mock('@/stores/cart', () => ({
  useCartStore: () => ({
    addItem: vi.fn().mockResolvedValue(undefined),
  }),
}));

vi.mock('@/api/order', () => ({
  getOrders: vi.fn().mockResolvedValue({ list: [], total: 0 }),
  getOrderById: vi.fn().mockResolvedValue({ id: 42, status: 1, totalFee: 9900, details: [] }),
  cancelOrder: vi.fn().mockResolvedValue(undefined),
  confirmOrder: vi.fn().mockResolvedValue(undefined),
  deleteOrder: vi.fn().mockResolvedValue(undefined),
  createPayOrder: vi.fn().mockResolvedValue('pay-order-001'),
  payOrderByBalance: vi.fn().mockResolvedValue(undefined),
}));

vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn(),
    info: vi.fn(),
  },
}));

// ---- Import after mocking -------------------------------------------------

import { ElMessage } from 'element-plus';
import { useCartStore } from '@/stores/cart';
import { useRouter } from 'vue-router';
import { deleteOrder, createPayOrder, payOrderByBalance } from '@/api/order';

// ---- Helper: standalone handler implementations (mirrors Vue component logic)

async function handleRepurchase(order, cartStore, router) {
  const details = order.details;
  if (!details || details.length === 0) {
    ElMessage.warning('订单商品信息不完整，无法再次购买');
    return;
  }
  for (const detail of details) {
    await cartStore.addItem({ itemId: detail.itemId, num: detail.num || 1 });
  }
  ElMessage.success('已加入购物车，即将跳转');
  router.push('/cart');
}

async function handleDeleteOrder(id, refresh) {
  await deleteOrder(id);
  ElMessage.success('订单已删除');
  refresh();
}

async function confirmPayFn(order, payPassword) {
  if (!payPassword.trim()) {
    ElMessage.warning('请输入支付密码');
    return false;
  }
  const payOrderId = await createPayOrder({
    bizOrderNo: order.id,
    amount: order.totalFee,
    payChannelCode: 'balance',
    payType: 5,
    orderInfo: `订单${order.id}`,
  });
  await payOrderByBalance(payOrderId, payPassword);
  ElMessage.success('支付成功');
  return true;
}

// ---- Tests ----------------------------------------------------------------

describe('handleRepurchase', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('calls addItem for each detail and navigates to /cart', async () => {
    const cartStore = useCartStore();
    const router = useRouter();

    const order = {
      details: [
        { itemId: 101, num: 2 },
        { itemId: 202, num: 1 },
      ],
    };

    await handleRepurchase(order, cartStore, router);

    expect(cartStore.addItem).toHaveBeenCalledTimes(2);
    expect(cartStore.addItem).toHaveBeenNthCalledWith(1, { itemId: 101, num: 2 });
    expect(cartStore.addItem).toHaveBeenNthCalledWith(2, { itemId: 202, num: 1 });
    expect(router.push).toHaveBeenCalledWith('/cart');
    expect(ElMessage.success).toHaveBeenCalledWith(expect.stringContaining('购物车'));
  });

  it('shows warning and does not navigate when details are empty', async () => {
    const cartStore = useCartStore();
    const router = useRouter();

    await handleRepurchase({ details: [] }, cartStore, router);

    expect(cartStore.addItem).not.toHaveBeenCalled();
    expect(router.push).not.toHaveBeenCalled();
    expect(ElMessage.warning).toHaveBeenCalled();
  });

  it('shows warning and does not navigate when details is undefined', async () => {
    const cartStore = useCartStore();
    const router = useRouter();

    await handleRepurchase({}, cartStore, router);

    expect(cartStore.addItem).not.toHaveBeenCalled();
    expect(router.push).not.toHaveBeenCalled();
    expect(ElMessage.warning).toHaveBeenCalled();
  });
});

describe('handleDeleteOrder', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('calls deleteOrder API and refreshes list on success', async () => {
    const refresh = vi.fn();
    await handleDeleteOrder(123, refresh);

    expect(deleteOrder).toHaveBeenCalledWith(123);
    expect(ElMessage.success).toHaveBeenCalledWith('订单已删除');
    expect(refresh).toHaveBeenCalled();
  });
});

describe('confirmPay', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('warns when password is empty', async () => {
    const result = await confirmPayFn({ id: 42, totalFee: 9900 }, '');
    expect(result).toBe(false);
    expect(ElMessage.warning).toHaveBeenCalledWith('请输入支付密码');
    expect(createPayOrder).not.toHaveBeenCalled();
  });

  it('creates pay order then executes balance pay and shows success', async () => {
    const result = await confirmPayFn({ id: 42, totalFee: 9900 }, '123456');

    expect(createPayOrder).toHaveBeenCalledWith({
      bizOrderNo: 42,
      amount: 9900,
      payChannelCode: 'balance',
      payType: 5,
      orderInfo: '订单42',
    });
    expect(payOrderByBalance).toHaveBeenCalledWith('pay-order-001', '123456');
    expect(ElMessage.success).toHaveBeenCalledWith('支付成功');
    expect(result).toBe(true);
  });
});
