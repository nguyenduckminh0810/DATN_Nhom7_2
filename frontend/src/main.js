import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap-icons/font/bootstrap-icons.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'
import './assets/main.css'
import './styles/sections.css'
import './utils/animations.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import { setupGlobalErrorHandler } from './composables/useErrorHandler'
import { validateConfig } from './config/env'
import { useUserStore } from './stores/user'

// Validate environment configuration
validateConfig()

const app = createApp(App)

// Setup global error handling
setupGlobalErrorHandler(app)

// Setup Pinia store
const pinia = createPinia()
app.use(pinia)

// Load user from localStorage after Pinia is initialized
const userStore = useUserStore()
userStore.loadUserFromStorage()
console.log('🔐 Loaded user from storage:', userStore.user)
console.log('✅ User authenticated:', userStore.isAuthenticated)
console.log('👤 User role:', userStore.userRole)

// Setup router
app.use(router)

// Global error handler for Vue
app.config.errorHandler = (err, vm, info) => {
  console.error('Vue error:', err, info)
  
  // Show user-friendly error message
  window.dispatchEvent(new CustomEvent('show-toast', {
    detail: {
      type: 'error',
      title: 'Lỗi ứng dụng',
      message: 'Có lỗi xảy ra trong ứng dụng. Vui lòng tải lại trang.',
      duration: 5000
    }
  }))
}

// Global warning handler for Vue
app.config.warnHandler = (msg, vm, trace) => {
  if (import.meta.env.DEV) {
    console.warn('Vue warning:', msg, trace)
  }
}

app.mount('#app')
