import { createRouter, createWebHistory } from 'vue-router'

// Import views
import Home from '../views/Home.vue'
import Category from '../views/Category.vue'
import ProductDetail from '../views/ProductDetail.vue'
import Cart from '../views/Cart.vue'
import Checkout from '../views/Checkout.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Profile from '../views/Profile.vue'
import Orders from '../views/Orders.vue'
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
      component: Login,
      meta: { title: 'Đăng nhập - AURO' }
    },
    {
      path: '/register',
      name: 'register',
      component: Register,
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
      next({ name: 'login', query: { redirect: to.fullPath } })
      return
    }
  }

  next()
})

export default router
