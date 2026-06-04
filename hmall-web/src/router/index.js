import { createRouter, createWebHistory } from 'vue-router';

const routes = [
  { path: '/', name: 'Home', component: () => import('@/views/Home.vue') },
  { path: '/category', name: 'Category', component: () => import('@/views/Category.vue') },
  { path: '/item/:id', name: 'ItemDetail', component: () => import('@/views/ItemDetail.vue') },
  { path: '/search', name: 'Search', component: () => import('@/views/Search.vue') },
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('@/views/Login.vue') },
  { path: '/forgot', name: 'Forgot', component: () => import('@/views/Login.vue') },
  {
    path: '/notifications',
    name: 'Notifications',
    component: () => import('@/views/Notifications.vue'),
  },
  {
    path: '/cart',
    name: 'Cart',
    component: () => import('@/views/Cart.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/order/confirm',
    name: 'OrderConfirm',
    component: () => import('@/views/OrderConfirm.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/orders',
    name: 'OrderList',
    component: () => import('@/views/OrderList.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/order/:id',
    name: 'OrderDetail',
    component: () => import('@/views/OrderDetail.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/favorites',
    name: 'FavoriteList',
    component: () => import('@/views/FavoriteList.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/addresses',
    name: 'AddressList',
    component: () => import('@/views/AddressList.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/feedback',
    name: 'Feedback',
    component: () => import('@/views/Feedback.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/coupons',
    name: 'Coupons',
    component: () => import('@/views/Coupons.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/flashsale',
    name: 'FlashSale',
    component: () => import('@/views/FlashSale.vue'),
  },
  {
    path: '/service',
    name: 'Service',
    component: () => import('@/views/Service.vue'),
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 };
  },
});

router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth && !localStorage.getItem('token')) {
    next('/login');
  } else {
    next();
  }
});

export default router;
