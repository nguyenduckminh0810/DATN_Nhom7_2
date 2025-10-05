import { createRouter, createWebHistory } from 'vue-router'

// Import views
import Home from '../views/Home.vue'
import Category from '../views/Category.vue'
import ProductDetail from '../views/ProductDetail.vue'
import Cart from '../views/Cart.vue'
import Checkout from '../views/Checkout.vue'
import Profile from '../views/Profile.vue'
import Orders from '../views/Orders.vue'
import SearchResults from '../views/SearchResults.vue'
import Wishlist from '../views/Wishlist.vue'
import AdminDashboard from '../views/admin/Dashboard.vue'
import AdminProducts from '../views/admin/Products.vue'
import AdminCategories from '../views/admin/Categories.vue'
import AdminOrders from '../views/admin/Orders.vue'
import AdminUsers from '../views/admin/Users.vue'
import AdminAnalytics from '../views/admin/AnalyticsNew.vue'
import NotFound from '../views/NotFound.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: Home,
      meta: { title: 'Trang chủ - AURO' }
    },
    {
      path: '/category/:slug?',
      name: 'category',
      component: Category,
      meta: { title: 'Danh mục sản phẩm - AURO' }
    },
    {
      path: '/product/:id',
      name: 'product-detail',
      component: ProductDetail,
      meta: { title: 'Chi tiết sản phẩm - AURO' }
    },
    {
      path: '/cart',
      name: 'cart',
      component: Cart,
      meta: { title: 'Giỏ hàng - AURO' }
    },
    {
      path: '/checkout',
      name: 'checkout',
      component: Checkout,
      meta: { title: 'Thanh toán - AURO' }
    },
    {
      path: '/login',
      name: 'login',
      redirect: '/',
      meta: { title: 'Đăng nhập - AURO' }
    },
    {
      path: '/register',
      name: 'register',
      redirect: '/',
      meta: { title: 'Đăng ký - AURO' }
    },
    {
      path: '/profile',
      name: 'profile',
      component: Profile,
      meta: { title: 'Thông tin cá nhân - AURO', requiresAuth: true }
    },
    {
      path: '/orders',
      name: 'orders',
      component: Orders,
      meta: { title: 'Đơn hàng của tôi - AURO', requiresAuth: true }
    },
    {
      path: '/search',
      name: 'search',
      component: SearchResults,
      meta: { title: 'Tìm kiếm - AURO' }
    },
    {
      path: '/wishlist',
      name: 'wishlist',
      component: Wishlist,
      meta: { title: 'Danh sách yêu thích - AURO' }
    },
    {
      path: '/admin',
      name: 'admin',
      component: () => import('../layouts/AdminLayout.vue'),
      children: [
        {
          path: '',
          name: 'admin-dashboard',
          component: AdminDashboard,
          meta: { title: 'Admin Dashboard - AURO' }
        },
        {
          path: 'products',
          name: 'admin-products',
          component: AdminProducts,
          meta: { title: 'Quản lý sản phẩm - AURO' }
        },
        {
          path: 'categories',
          name: 'admin-categories',
          component: AdminCategories,
          meta: { title: 'Quản lý danh mục - AURO' }
        },
        {
          path: 'orders',
          name: 'admin-orders',
          component: AdminOrders,
          meta: { title: 'Quản lý đơn hàng - AURO' }
        },
        {
          path: 'users',
          name: 'admin-users',
          component: AdminUsers,
          meta: { title: 'Quản lý người dùng - AURO' }
        },
        {
          path: 'analytics',
          name: 'admin-analytics',
          component: AdminAnalytics,
          meta: { title: 'Thống kê & Báo cáo - AURO' }
        }
      ]
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: NotFound,
      meta: { title: 'Không tìm thấy trang - AURO' }
    }
  ],
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
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
