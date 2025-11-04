import { createRouter, createWebHistory } from 'vue-router'

// Lazy load views for better performance with code splitting
const Home = () => import(/* webpackChunkName: "home" */ '../views/Home.vue')
const Category = () => import(/* webpackChunkName: "category" */ '../views/Category.vue')
const ProductDetail = () => import(/* webpackChunkName: "product" */ '../views/ProductDetail.vue')
const Cart = () => import(/* webpackChunkName: "cart" */ '../views/Cart.vue')
const Profile = () => import(/* webpackChunkName: "profile" */ '../views/Profile.vue')
const Orders = () => import(/* webpackChunkName: "orders" */ '../views/Orders.vue')
const SearchResults = () => import(/* webpackChunkName: "search" */ '../views/SearchResults.vue')
const NotFound = () => import(/* webpackChunkName: "common" */ '../views/NotFound.vue')
const ShippingDemo = () => import(/* webpackChunkName: "demo" */ '../views/ShippingDemo.vue')
const PaymentReturn = () => import(/* webpackChunkName: "payment" */ '../views/PaymentReturn.vue')

// Admin routes - lazy loaded separately with admin chunk
const AdminDashboard = () => import(/* webpackChunkName: "admin" */ '../views/admin/Dashboard.vue')
const AdminProducts = () =>
  import(/* webpackChunkName: "admin" */ '../views/admin/AdminProducts.vue')
const AdminCategories = () =>
  import(/* webpackChunkName: "admin" */ '../views/admin/Categories.vue')
const AdminOrders = () => import(/* webpackChunkName: "admin" */ '../views/admin/Orders.vue')
const AdminUsers = () => import(/* webpackChunkName: "admin" */ '../views/admin/Users.vue')
const AdminAnalytics = () => import(/* webpackChunkName: "admin" */ '../views/admin/Analytics.vue')
const AdminSettings = () => import(/* webpackChunkName: "admin" */ '../views/admin/Settings.vue')
const AdminInventory = () => import(/* webpackChunkName: "admin" */ '../views/admin/Inventory.vue')
const AdminPromotions = () =>
  import(/* webpackChunkName: "admin" */ '../views/admin/Promotions.vue')

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: Home,
      meta: { title: 'Trang chủ - AURO' },
    },
    {
      path: '/category/:slug?',
      name: 'category',
      component: Category,
      meta: { title: 'Danh mục sản phẩm - AURO' },
    },
    {
      path: '/product/:id',
      name: 'product-detail',
      component: ProductDetail,
      meta: { title: 'Chi tiết sản phẩm - AURO' },
    },
    {
      path: '/cart',
      name: 'cart',
      component: Cart,
      meta: { title: 'Giỏ hàng - AURO' },
    },
    {
      path: '/login',
      name: 'login',
      redirect: '/',
      meta: { title: 'Đăng nhập - AURO' },
    },
    {
      path: '/register',
      name: 'register',
      redirect: '/',
      meta: { title: 'Đăng ký - AURO' },
    },
    {
      path: '/profile',
      name: 'profile',
      component: Profile,
      meta: { title: 'Thông tin cá nhân - AURO', requiresAuth: true },
    },
    {
      path: '/orders',
      name: 'orders',
      component: Orders,
      meta: { title: 'Đơn hàng của tôi - AURO', requiresAuth: true },
    },
    {
      path: '/search',
      name: 'search',
      component: SearchResults,
      meta: { title: 'Tìm kiếm - AURO' },
    },
    {
      path: '/shipping-demo',
      name: 'shipping-demo',
      component: ShippingDemo,
      meta: { title: 'Demo GHN Shipping API - AURO' },
    },
    {
      path: '/payment/vnpay-return',
      name: 'payment-return',
      component: PaymentReturn,
      meta: { title: 'Kết quả thanh toán - AURO' },
    },
    {
      path: '/admin',
      name: 'admin',
      component: () => import('../layouts/AdminLayout.vue'),
      meta: {
        title: 'Admin Dashboard - AURO',
        requiresAuth: true,
        requiresAdmin: true,
      },
      children: [
        {
          path: '',
          name: 'admin-dashboard',
          component: AdminDashboard,
          meta: { title: 'Admin Dashboard - AURO', requiresAuth: true, requiresAdmin: true },
        },
        {
          path: 'products',
          name: 'admin-products',
          component: () => import('@/views/admin/AdminProducts.vue'),
          meta: { title: 'Quản lý sản phẩm - AURO', requiresAuth: true, requiresAdmin: true },
        },
        {
          path: 'categories',
          name: 'admin-categories',
          component: AdminCategories,
          meta: { title: 'Quản lý danh mục - AURO', requiresAuth: true, requiresAdmin: true },
        },
        {
          path: 'orders',
          name: 'admin-orders',
          component: AdminOrders,
          meta: { title: 'Quản lý đơn hàng - AURO', requiresAuth: true, requiresAdmin: true },
        },
        {
          path: 'users',
          name: 'admin-users',
          component: AdminUsers,
          meta: { title: 'Quản lý người dùng - AURO', requiresAuth: true, requiresAdmin: true },
        },
        {
          path: 'analytics',
          name: 'admin-analytics',
          component: AdminAnalytics,
          meta: { title: 'Thống kê & Báo cáo - AURO', requiresAuth: true, requiresAdmin: true },
        },
        {
          path: 'settings',
          name: 'admin-settings',
          component: AdminSettings,
          meta: { title: 'Cài đặt - AURO', requiresAuth: true, requiresAdmin: true },
        },
        {
          path: 'inventory',
          name: 'admin-inventory',
          component: AdminInventory,
          meta: { title: 'Quản lý tồn kho - AURO', requiresAuth: true, requiresAdmin: true },
        },
        {
          path: 'promotions',
          name: 'admin-promotions',
          component: AdminPromotions,
          meta: { title: 'Khuyến mãi & Voucher - AURO', requiresAuth: true, requiresAdmin: true },
        },
      ],
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: NotFound,
      meta: { title: 'Không tìm thấy trang - AURO' },
    },
  ],
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  },
})

// Navigation guards
router.beforeEach((to, from, next) => {
  // Update page title
  if (to.meta.title) {
    document.title = to.meta.title
  }

  // Check authentication
  if (to.meta.requiresAuth) {
    const token = localStorage.getItem('auro_token')
    if (!token) {
      // Store the intended route for redirect after login
      localStorage.setItem('auro_redirect', to.fullPath)
      // For now, just redirect to home - the popup will handle the login
      next('/')
      return
    }
  }
  if (to.meta.requiresAdmin) {
    const user = JSON.parse(localStorage.getItem('auro_user') || '{}')
    console.log('Router check - User from localStorage:', user)

    // Kiểm tra cả vaiTro, vaiTroMa và role
    const userRole = user.vaiTroMa || user.vaiTro || user.role
    console.log('Router check - User role:', userRole)

    if (!userRole || !['ADM', 'STF', 'admin', 'staff'].includes(userRole)) {
      // Redirect to home if not admin/staff
      console.log('Access denied - redirecting to home')
      next('/')
      return
    }
  }

  // Redirect login/register pages to home with popup trigger
  if (to.name === 'login') {
    localStorage.setItem('auro_show_login_popup', 'true')
    next('/')
    return
  }

  if (to.name === 'register') {
    localStorage.setItem('auro_show_register_popup', 'true')
    next('/')
    return
  }

  next()
})

export default router
