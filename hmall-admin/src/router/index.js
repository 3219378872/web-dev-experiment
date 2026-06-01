import { createRouter, createWebHistory } from 'vue-router';

const routes = [
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue') },
  {
    path: '/',
    component: () => import('@/components/AdminLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/Dashboard.vue') },
      { path: 'users', name: 'UserList', component: () => import('@/views/UserList.vue') },
      { path: 'items', name: 'ItemList', component: () => import('@/views/ItemList.vue') },
      { path: 'items/add', name: 'ItemAdd', component: () => import('@/views/ItemEdit.vue') },
      { path: 'items/:id/edit', name: 'ItemEdit', component: () => import('@/views/ItemEdit.vue') },
      {
        path: 'categories',
        name: 'CategoryList',
        component: () => import('@/views/CategoryList.vue'),
      },
      { path: 'orders', name: 'OrderList', component: () => import('@/views/OrderList.vue') },
      {
        path: 'orders/:id',
        name: 'OrderDetail',
        component: () => import('@/views/OrderDetail.vue'),
      },
      { path: 'reviews', name: 'ReviewList', component: () => import('@/views/ReviewList.vue') },
      { path: 'banners', name: 'BannerList', component: () => import('@/views/BannerList.vue') },
      {
        path: 'notifications',
        name: 'NotificationList',
        component: () => import('@/views/NotificationList.vue'),
      },
      {
        path: 'feedbacks',
        name: 'FeedbackList',
        component: () => import('@/views/FeedbackList.vue'),
      },
      { path: 'profile', name: 'Profile', component: () => import('@/views/Profile.vue') },
      { path: '', redirect: '/dashboard' },
    ],
  },
];

const router = createRouter({ history: createWebHistory(), routes });
router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth && !localStorage.getItem('adminToken')) next('/login');
  else next();
});
export default router;
