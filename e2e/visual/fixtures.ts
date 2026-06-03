/**
 * 视觉回归测试用 API Mock 数据。
 * 格式需与前端组件消费方式一致（list / data / 直接数组等）。
 */

export const mockItems = {
  list: [
    { id: 1, name: '有机五常大米 5kg', price: 6800, image: '', sold: 1240, tag: '热卖' },
    { id: 2, name: '澳洲和牛牛排 M7', price: 29800, image: '', sold: 856, tag: '热卖' },
    { id: 3, name: '日式陶瓷餐具套装', price: 12900, image: '', sold: 642, tag: '热卖' },
    { id: 4, name: '智能空气炸锅 4.5L', price: 29900, image: '', sold: 510, tag: '热卖' },
    { id: 5, name: '进口洗衣液 3L', price: 4500, image: '', sold: 420, tag: '热卖' },
    { id: 6, name: '坚果礼盒 1.2kg', price: 16800, image: '', sold: 380, tag: '新品' },
    { id: 7, name: '纯棉四件套', price: 25900, image: '', sold: 290, tag: '新品' },
    { id: 8, name: '无线吸尘器', price: 129900, image: '', sold: 210, tag: '新品' },
    { id: 9, name: '防晒霜 SPF50', price: 8900, image: '', sold: 180, tag: '新品' },
    { id: 10, name: '运动跑鞋', price: 39900, image: '', sold: 150, tag: '新品' },
  ],
  total: 10,
  pages: 1,
};

export const mockCategories = [
  { id: 1, name: '粮油调味', icon: '🍚' },
  { id: 2, name: '生鲜果蔬', icon: '🥬' },
  { id: 3, name: '肉禽蛋品', icon: '🥩' },
  { id: 4, name: '休闲零食', icon: '🍪' },
  { id: 5, name: '酒水饮料', icon: '🍷' },
  { id: 6, name: '美妆护肤', icon: '💄' },
  { id: 7, name: '家居日用', icon: '🏠' },
  { id: 8, name: '数码家电', icon: '📱' },
  { id: 9, name: '母婴用品', icon: '🍼' },
  { id: 10, name: '运动户外', icon: '⚽' },
];

export const mockItemDetail = {
  id: 1,
  name: '有机五常大米 5kg',
  price: 6800,
  oldPrice: 8800,
  image: '',
  images: ['', '', '', ''],
  stock: 999,
  sold: 1240,
  desc: '东北五常核心产区，稻花香2号品种，口感软糯香甜。',
  specs: ['5kg', '10kg'],
  categoryId: 1,
  brand: '好集甄选',
};

export const mockOrders = {
  list: [
    {
      id: 'ORD202606030001',
      status: 2,
      totalFee: 16800,
      createTime: '2026-06-03 14:15:00',
      payTime: '2026-06-03 14:16:00',
      payType: '微信支付',
      receiverName: '林晓',
      receiverPhone: '13800138000',
      receiverAddress: '浙江省杭州市西湖区文三路 478号',
      userName: 'linxiao',
      userPhone: '13800138000',
      orderDetails: [
        { name: '有机五常大米 5kg', price: 6800, num: 1, image: '', spec: '5kg' },
        { name: '进口洗衣液 3L', price: 4500, num: 2, image: '', spec: '默认规格' },
      ],
    },
    {
      id: 'ORD202606020002',
      status: 3,
      totalFee: 29800,
      createTime: '2026-06-02 10:20:00',
      payTime: '2026-06-02 10:21:00',
      payType: '支付宝',
      receiverName: '林晓',
      receiverPhone: '13800138000',
      receiverAddress: '浙江省杭州市西湖区文三路 478号',
      userName: 'linxiao',
      userPhone: '13800138000',
      orderDetails: [
        { name: '澳洲和牛牛排 M7', price: 29800, num: 1, image: '', spec: '默认规格' },
      ],
    },
  ],
  total: 2,
};

export const mockAdminOrders = {
  list: [
    {
      id: 'ORD202606030001',
      status: 2,
      totalFee: 16800,
      createTime: '2026-06-03 14:15:00',
      userId: 1,
      userName: '林晓',
      receiverName: '林晓',
      receiverPhone: '13800138000',
      receiverAddress: '浙江省杭州市西湖区文三路 478号',
      payType: '微信支付',
      payTime: '2026-06-03 14:16:00',
      goodsText: '有机五常大米 5kg 等 2 件',
    },
    {
      id: 'ORD202606020002',
      status: 3,
      totalFee: 29800,
      createTime: '2026-06-02 10:20:00',
      userId: 2,
      userName: '张伟',
      receiverName: '张伟',
      receiverPhone: '13900139000',
      receiverAddress: '上海市浦东新区世纪大道 100号',
      payType: '支付宝',
      payTime: '2026-06-02 10:21:00',
      goodsText: '澳洲和牛牛排 M7',
    },
  ],
  total: 2,
};

export const mockCoupons = [
  { id: 1, name: '全场满100减20', threshold: 10000, discount: 2000, status: 1 },
  { id: 2, name: '新用户9折券', threshold: 0, discount: 0, status: 1, rate: 0.9 },
  { id: 3, name: '生鲜满200减50', threshold: 20000, discount: 5000, status: 1 },
];

export const mockMyCoupons = [
  {
    id: 1,
    name: '全场满100减20',
    threshold: 10000,
    discount: 2000,
    status: 1,
    validStart: '2026-06-01',
    validEnd: '2026-06-30',
  },
  {
    id: 2,
    name: '新用户9折券',
    threshold: 0,
    discount: 0,
    status: 1,
    rate: 0.9,
    validStart: '2026-06-01',
    validEnd: '2026-06-30',
  },
];

export const mockCart = {
  items: [
    {
      id: 1,
      itemId: 1,
      name: '有机五常大米 5kg',
      price: 6800,
      num: 1,
      image: '',
      spec: '5kg',
      selected: true,
    },
    {
      id: 2,
      itemId: 2,
      name: '澳洲和牛牛排 M7',
      price: 29800,
      num: 1,
      image: '',
      spec: '默认规格',
      selected: true,
    },
  ],
};

export const mockAddresses = [
  {
    id: 1,
    name: '林晓',
    phone: '13800138000',
    province: '浙江省',
    city: '杭州市',
    district: '西湖区',
    detail: '文三路 478号',
    isDefault: true,
  },
  {
    id: 2,
    name: '林晓',
    phone: '13800138000',
    province: '浙江省',
    city: '杭州市',
    district: '滨江区',
    detail: '物联网街 369号',
    isDefault: false,
  },
];

export const mockUserProfile = {
  id: 1,
  username: 'linxiao',
  nickname: '林晓',
  avatar: '',
  phone: '13800138000',
  email: 'linxiao@haoji.com',
  level: '黄金会员',
  points: 2860,
  balance: 12800,
  couponCount: 3,
  orderCount: 12,
  favoriteCount: 8,
};

export const mockNotifications = [
  {
    id: 1,
    title: '订单发货提醒',
    content: '您的订单 ORD202606030001 已发货，顺丰速运 SF1366028866018',
    time: '2026-06-03 15:02',
    read: false,
  },
  {
    id: 2,
    title: '优惠券到账',
    content: '您领取的「全场满100减20」优惠券已到账',
    time: '2026-06-02 09:30',
    read: true,
  },
  {
    id: 3,
    title: '系统维护通知',
    content: '好集将于 6 月 5 日 02:00-04:00 进行系统维护',
    time: '2026-06-01 18:00',
    read: true,
  },
];

export const mockFeedbacks = [
  {
    id: 1,
    type: '商品相关',
    content: '大米口感很好，回购第三次了',
    time: '2026-06-03 10:20',
    status: '已回复',
  },
  {
    id: 2,
    type: '物流建议',
    content: '希望增加次日达选项',
    time: '2026-06-01 16:45',
    status: '待处理',
  },
];

export const mockFavorites = {
  list: [
    { id: 1, itemId: 1, name: '有机五常大米 5kg', price: 6800, image: '' },
    { id: 2, itemId: 3, name: '日式陶瓷餐具套装', price: 12900, image: '' },
    { id: 3, itemId: 7, name: '纯棉四件套', price: 25900, image: '' },
  ],
  total: 3,
};

export const mockFlashSale = {
  list: [
    {
      id: 1,
      name: '有机五常大米 5kg',
      price: 4900,
      oldPrice: 6800,
      image: '',
      stock: 86,
      total: 200,
    },
    { id: 2, name: '进口洗衣液 3L', price: 2900, oldPrice: 4500, image: '', stock: 42, total: 100 },
    {
      id: 3,
      name: '坚果礼盒 1.2kg',
      price: 9900,
      oldPrice: 16800,
      image: '',
      stock: 15,
      total: 50,
    },
  ],
  total: 3,
};

export const mockAdminItems = {
  list: [
    {
      id: 1,
      name: '有机五常大米 5kg',
      price: 6800,
      stock: 999,
      status: 1,
      categoryName: '粮油调味',
      sold: 1240,
      createTime: '2026-05-01',
    },
    {
      id: 2,
      name: '澳洲和牛牛排 M7',
      price: 29800,
      stock: 120,
      status: 1,
      categoryName: '肉禽蛋品',
      sold: 856,
      createTime: '2026-05-02',
    },
    {
      id: 3,
      name: '日式陶瓷餐具套装',
      price: 12900,
      stock: 300,
      status: 1,
      categoryName: '家居日用',
      sold: 642,
      createTime: '2026-05-03',
    },
  ],
  total: 3,
};

export const mockAdminBanners = [
  { id: 1, title: '618年中大促', image: '', link: '', sort: 1, status: 1 },
  { id: 2, title: '新人专享礼包', image: '', link: '', sort: 2, status: 1 },
  { id: 3, title: '粮油调味节', image: '', link: '', sort: 3, status: 1 },
];

export const mockAdminReviews = {
  list: [
    {
      id: 1,
      itemName: '有机五常大米 5kg',
      userName: '林晓',
      rating: 5,
      content: '口感很好，很香',
      createTime: '2026-06-03',
    },
    {
      id: 2,
      itemName: '澳洲和牛牛排 M7',
      userName: '张伟',
      rating: 4,
      content: '肉质不错，价格稍贵',
      createTime: '2026-06-02',
    },
  ],
  total: 2,
};

export const mockAdminUsers = {
  list: [
    {
      id: 1,
      username: 'linxiao',
      nickname: '林晓',
      phone: '13800138000',
      level: '黄金会员',
      status: 1,
      createTime: '2026-01-15',
    },
    {
      id: 2,
      username: 'zhangwei',
      nickname: '张伟',
      phone: '13900139000',
      level: '普通会员',
      status: 1,
      createTime: '2026-02-20',
    },
  ],
  total: 2,
};
