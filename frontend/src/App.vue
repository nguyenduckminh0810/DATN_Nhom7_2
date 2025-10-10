<script setup>
import { onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useCartStore } from './stores/cart'
import { useUserStore } from './stores/user'
import { useSEO } from './composables/useSEO'
import { useToast } from './composables/useToast'
import ClientLayout from './layouts/ClientLayout.vue'
import Toast from './components/common/Toast.vue'

const route = useRoute()
const cartStore = useCartStore()
const userStore = useUserStore()
const { initializeSEO } = useSEO()
const { toasts, removeToast } = useToast()

// Computed để xác định layout
const isAdminRoute = computed(() => {
  return route.path.startsWith('/admin')
})

// Initialize SEO for current route
onMounted(() => {
  cartStore.loadFromStorage()
  userStore.loadUserFromStorage()
  initializeSEO()
  
  // Listen for toast events
  window.addEventListener('show-toast', (event) => {
    const { type, title, message, duration } = event.detail
    useToast()[type](message, { title, duration })
  })
})

// Helper function to get toast icon
const getToastIcon = (type) => {
  const icons = {
    success: 'bi bi-check-circle text-success',
    error: 'bi bi-x-circle text-danger',
    warning: 'bi bi-exclamation-triangle-fill text-warning',
    info: 'bi bi-info-circle text-info',
    loading: 'bi bi-arrow-clockwise text-primary'
  }
  return icons[type] || icons.info
}
</script>

<template>
  <div id="app">
    <!-- Chỉ hiển thị ClientLayout cho các route không phải admin -->
    <ClientLayout v-if="!isAdminRoute" />
    
    <!-- Admin routes sẽ sử dụng AdminLayout thông qua router-view -->
    <router-view v-if="isAdminRoute" />
    
    <!-- Toast Container -->
    <div class="toast-container position-fixed top-0 end-0 p-3" style="z-index: 10000;">
      <div v-for="toast in toasts" :key="toast.id" class="toast show" role="alert">
        <div class="toast-header">
          <i :class="getToastIcon(toast.type)" class="me-2"></i>
          <strong class="me-auto">{{ toast.title }}</strong>
          <button v-if="toast.closable" type="button" class="btn-close" @click="removeToast(toast.id)"></button>
        </div>
        <div class="toast-body">
          {{ toast.message }}
        </div>
      </div>
    </div>
    
  </div>
</template>

<style>
/* Global styles */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: var(--auro-body-font);
  font-weight: var(--auro-font-normal);
  line-height: 1.6;
  color: var(--auro-text);
  background-color: var(--auro-bg);
  letter-spacing: -0.01em;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* AURO Premium Brand Colors - Black & Bronze Theme */
:root {
  /* Primary Brand Colors - Deep Black & Bronze */
  --auro-primary: #000000;
  --auro-primary-light: #1a1a1a;
  --auro-primary-dark: #000000;
  --auro-secondary: #cd7f32;
  --auro-secondary-light: #daa520;
  --auro-secondary-dark: #b8860b;
  
  /* Accent Colors */
  --auro-accent: #8b4513;
  --auro-accent-light: #a0522d;
  --auro-accent-dark: #654321;
  
  /* Neutral Colors */
  --auro-dark: #0f1419;
  --auro-light: #f8fafc;
  --auro-bg: #ffffff;
  --auro-card: #ffffff;
  --auro-border: #e2e8f0;
  --auro-border-light: #f1f5f9;
  --auro-text: #1e293b;
  --auro-text-light: #64748b;
  --auro-text-muted: #94a3b8;
  --auro-hover: #f8fafc;
  
  /* Shadows */
  --auro-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06);
  --auro-shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  --auro-shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
  --auro-shadow-xl: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  --auro-shadow-hover: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  
  /* Gradients */
  --auro-gradient: linear-gradient(135deg, #000000 0%, #1a1a1a 100%);
  --auro-gradient-accent: linear-gradient(135deg, #cd7f32 0%, #daa520 100%);
  --auro-gradient-subtle: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  
  /* Typography - Modern Premium Fonts */
  --auro-heading-font: "Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  --auro-body-font: "Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  --auro-display-font: "Playfair Display", Georgia, serif;
  
  /* Font Weights */
  --auro-font-light: 300;
  --auro-font-normal: 400;
  --auro-font-medium: 500;
  --auro-font-semibold: 600;
  --auro-font-bold: 700;
  --auro-font-extrabold: 800;
  
  /* Spacing System */
  --auro-space-xs: 0.25rem;
  --auro-space-sm: 0.5rem;
  --auro-space-md: 1rem;
  --auro-space-lg: 1.5rem;
  --auro-space-xl: 2rem;
  --auro-space-2xl: 3rem;
  --auro-space-3xl: 4rem;
  
  /* Border Radius */
  --auro-radius-sm: 0.375rem;
  --auro-radius-md: 0.5rem;
  --auro-radius-lg: 0.75rem;
  --auro-radius-xl: 1rem;
  --auro-radius-2xl: 1.5rem;
  --auro-radius-full: 9999px;
  
  /* Transitions */
  --auro-transition-fast: 150ms cubic-bezier(0.4, 0, 0.2, 1);
  --auro-transition-normal: 250ms cubic-bezier(0.4, 0, 0.2, 1);
  --auro-transition-slow: 350ms cubic-bezier(0.4, 0, 0.2, 1);
}

/* AURO Premium Typography */
h1, h2, h3, h4, h5, h6 {
  font-family: var(--auro-heading-font);
  font-weight: var(--auro-font-bold);
  line-height: 1.2;
  color: var(--auro-text);
  letter-spacing: -0.02em;
}

h1 { font-size: 3rem; font-weight: var(--auro-font-extrabold); }
h2 { font-size: 2.25rem; font-weight: var(--auro-font-bold); }
h3 { font-size: 1.875rem; font-weight: var(--auro-font-semibold); }
h4 { font-size: 1.5rem; font-weight: var(--auro-font-semibold); }
h5 { font-size: 1.25rem; font-weight: var(--auro-font-medium); }
h6 { font-size: 1.125rem; font-weight: var(--auro-font-medium); }

/* AURO Premium Buttons */
.btn-warning {
  background: var(--auro-gradient-accent);
  border: none;
  color: var(--auro-dark);
  font-weight: var(--auro-font-semibold);
  border-radius: var(--auro-radius-lg);
  padding: var(--auro-space-md) var(--auro-space-xl);
  transition: all var(--auro-transition-normal);
  box-shadow: var(--auro-shadow-md);
  text-transform: none;
  letter-spacing: -0.01em;
  font-size: 0.875rem;
  position: relative;
  overflow: hidden;
}

.btn-warning::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left var(--auro-transition-normal);
}

.btn-warning:hover::before {
  left: 100%;
}

.btn-warning:hover {
  transform: translateY(-2px);
  box-shadow: var(--auro-shadow-lg);
  color: var(--auro-dark);
}

.text-warning {
  color: var(--auro-secondary) !important;
}

/* AURO Premium Navbar */
.navbar-brand {
  font-family: var(--auro-display-font);
  font-weight: var(--auro-font-bold);
  letter-spacing: -0.02em;
  font-size: 1.75rem;
  color: var(--auro-primary) !important;
  text-decoration: none;
}

.navbar-brand:hover {
  color: var(--auro-primary-light) !important;
}

/* AURO Premium Product Cards */
.product-card {
  background: var(--auro-card);
  border-radius: var(--auro-radius-2xl);
  box-shadow: var(--auro-shadow);
  transition: all var(--auro-transition-slow);
  border: 1px solid var(--auro-border);
  overflow: hidden;
  position: relative;
}

.product-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: var(--auro-gradient-accent);
  transform: scaleX(0);
  transition: transform var(--auro-transition-normal);
}

.product-card:hover::before {
  transform: scaleX(1);
}

.product-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--auro-shadow-hover);
  border-color: var(--auro-secondary);
}

.product-card .card-img-top {
  transition: transform var(--auro-transition-slow);
  height: 280px;
  object-fit: cover;
}

.product-card:hover .card-img-top {
  transform: scale(1.05);
}

.product-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all 0.3s ease;
  border-radius: 12px;
}

.product-image-container:hover .product-overlay {
  opacity: 1;
}

.product-overlay .btn {
  transform: translateY(20px);
  transition: transform 0.3s ease;
}

.product-image-container:hover .product-overlay .btn {
  transform: translateY(0);
}

/* Enhanced product card hover effects */
.product-card .card-body {
  transition: all 0.3s ease;
}

.product-card:hover .card-body {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

.product-card:hover .card-title {
  color: var(--auro-primary);
}

.product-card:hover .price {
  transform: scale(1.05);
  transition: transform 0.2s ease;
}

/* Category card hover effects */
.category-card {
  transition: all 0.3s ease;
  cursor: pointer;
}

.category-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
  border-color: var(--auro-accent);
}

.category-card:hover .category-icon-container {
  transform: scale(1.1);
  transition: transform 0.3s ease;
}

.category-card:hover .category-icon i {
  color: var(--auro-accent);
  transform: rotate(10deg);
}

.category-card:hover .card-title {
  color: var(--auro-primary);
}

/* Button hover effects */
.btn-auro-primary {
  position: relative;
  overflow: hidden;
}

.btn-auro-primary::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s ease;
}

.btn-auro-primary:hover::before {
  left: 100%;
}

/* AURO Price Display */
.price {
  font-weight: 700;
  color: var(--auro-accent);
  font-size: 18px;
}

/* AURO Hero Section */
.hero-section {
  background: var(--auro-gradient);
  color: white;
  border-radius: 24px;
  padding: 80px 60px;
  margin: 40px 0;
  position: relative;
  overflow: hidden;
}

.hero-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="50" cy="50" r="1" fill="rgba(255,255,255,0.1)"/></pattern></defs><rect width="100" height="100" fill="url(%23grain)"/></svg>');
  opacity: 0.3;
}

/* AURO Feature Icons */
.feature-icon {
  width: 80px;
  height: 80px;
  background: var(--auro-gradient-accent);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--auro-dark);
  font-size: 2rem;
  margin: 0 auto 1.5rem;
  box-shadow: var(--auro-shadow);
  transition: all 0.3s ease;
}

.feature-icon:hover {
  transform: translateY(-4px) rotate(5deg);
  box-shadow: var(--auro-shadow-hover);
}

/* AURO Testimonial Cards */
.testimonial-card {
  background: var(--auro-card);
  border-radius: 20px;
  padding: 2.5rem;
  box-shadow: var(--auro-shadow);
  border-left: 6px solid var(--auro-accent);
  position: relative;
  transition: all 0.3s ease;
}

.testimonial-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--auro-shadow-hover);
}

.testimonial-card::before {
  content: '"';
  position: absolute;
  top: -15px;
  left: 25px;
  font-size: 5rem;
  color: var(--auro-accent);
  opacity: 0.2;
  font-family: serif;
  font-weight: 300;
}

/* AURO Newsletter Section */
.newsletter-section {
  background: var(--auro-gradient);
  border-radius: 24px;
  padding: 4rem 3rem;
  color: white;
  text-align: center;
  position: relative;
  overflow: hidden;
}

.newsletter-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="dots" width="20" height="20" patternUnits="userSpaceOnUse"><circle cx="10" cy="10" r="1" fill="rgba(255,255,255,0.1)"/></pattern></defs><rect width="100" height="100" fill="url(%23dots)"/></svg>');
  opacity: 0.5;
}

.newsletter-section .form-control {
  border: 2px solid rgba(255, 255, 255, 0.3);
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border-radius: 12px;
  backdrop-filter: blur(10px);
  padding: 16px 20px;
}

.newsletter-section .form-control::placeholder {
  color: rgba(255, 255, 255, 0.7);
}

.newsletter-section .form-control:focus {
  border-color: var(--auro-accent);
  box-shadow: 0 0 0 3px rgba(212, 175, 55, 0.3);
  background: rgba(255, 255, 255, 0.15);
}

/* AURO Footer */
.footer-modern {
  background: var(--auro-gradient);
  color: white;
  border-radius: 24px 24px 0 0;
  margin-top: 6rem;
  position: relative;
  overflow: hidden;
}

.footer-modern::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grid" width="10" height="10" patternUnits="userSpaceOnUse"><path d="M 10 0 L 0 0 0 10" fill="none" stroke="rgba(255,255,255,0.05)" stroke-width="0.5"/></pattern></defs><rect width="100" height="100" fill="url(%23grid)"/></svg>');
  opacity: 0.3;
}

.footer-modern .social-links a {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: white;
  transition: all 0.3s ease;
  margin: 0 0.5rem;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.footer-modern .social-links a:hover {
  background: var(--auro-accent);
  color: var(--auro-dark);
  transform: translateY(-4px) scale(1.1);
  box-shadow: var(--auro-shadow-hover);
}

/* Enhanced AURO Loading Animations */
@keyframes auro-pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.7;
    transform: scale(1.05);
  }
}

@keyframes auro-float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-10px);
  }
}

@keyframes auro-shimmer {
  0% {
    background-position: -200px 0;
  }
  100% {
    background-position: calc(200px + 100%) 0;
  }
}

@keyframes auro-bounce {
  0%, 20%, 53%, 80%, 100% {
    transform: translate3d(0, 0, 0);
  }
  40%, 43% {
    transform: translate3d(0, -8px, 0);
  }
  70% {
    transform: translate3d(0, -4px, 0);
  }
  90% {
    transform: translate3d(0, -2px, 0);
  }
}

@keyframes auro-fade-in-up {
  from {
    opacity: 0;
    transform: translate3d(0, 30px, 0);
  }
  to {
    opacity: 1;
    transform: translate3d(0, 0, 0);
  }
}

@keyframes auro-slide-in-right {
  from {
    opacity: 0;
    transform: translate3d(30px, 0, 0);
  }
  to {
    opacity: 1;
    transform: translate3d(0, 0, 0);
  }
}

.auro-loading {
  animation: auro-pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

.auro-float {
  animation: auro-float 3s ease-in-out infinite;
}

.auro-shimmer {
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200px 100%;
  animation: auro-shimmer 1.5s infinite;
}

.auro-bounce {
  animation: auro-bounce 1s ease-in-out;
}

.auro-fade-in-up {
  animation: auro-fade-in-up 0.6s ease-out;
}

.auro-slide-in-right {
  animation: auro-slide-in-right 0.6s ease-out;
}

/* Smooth hover effects */
.smooth-hover {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.smooth-hover:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

/* Smooth focus effects */
.smooth-focus {
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.smooth-focus:focus {
  outline: none;
  box-shadow: 0 0 0 3px rgba(140, 144, 126, 0.2);
  transform: scale(1.02);
}

/* AURO Gradient Text */
.gradient-text {
  background: var(--auro-gradient-accent);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-weight: 800;
}

/* AURO Button Variants */
.btn-auro-primary {
  background: var(--auro-gradient);
  border: none;
  color: white;
  font-weight: 600;
  border-radius: 12px;
  padding: 16px 32px;
  transition: all 0.3s ease;
  box-shadow: var(--auro-shadow);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  font-size: 14px;
  position: relative;
  overflow: hidden;
}

.btn-auro-primary::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
  transition: left 0.5s;
}

.btn-auro-primary:hover::before {
  left: 100%;
}

.btn-auro-primary:hover {
  transform: translateY(-3px);
  box-shadow: var(--auro-shadow-hover);
  color: white;
}

.btn-auro-secondary {
  background: var(--auro-gradient-accent);
  border: none;
  color: var(--auro-dark);
  font-weight: 600;
  border-radius: 12px;
  padding: 16px 32px;
  transition: all 0.3s ease;
  box-shadow: var(--auro-shadow);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  font-size: 14px;
}

.btn-auro-secondary:hover {
  transform: translateY(-3px);
  box-shadow: var(--auro-shadow-hover);
  color: var(--auro-dark);
}

/* AURO Form Enhancements */
.form-control-auro {
  border: 2px solid var(--auro-border);
  border-radius: 12px;
  padding: 16px 20px;
  transition: all 0.3s ease;
  background: var(--auro-card);
  font-size: 16px;
}

.form-control-auro:focus {
  border-color: var(--auro-accent);
  box-shadow: 0 0 0 3px rgba(212, 175, 55, 0.1);
  outline: none;
  background: var(--auro-light);
}

/* AURO Card Enhancements */
.card-auro {
  background: var(--auro-card);
  border-radius: 20px;
  box-shadow: var(--auro-shadow);
  border: 1px solid var(--auro-border);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  position: relative;
}

.card-auro::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: var(--auro-gradient-accent);
  transform: scaleX(0);
  transition: transform 0.3s ease;
}

.card-auro:hover::before {
  transform: scaleX(1);
}

.card-auro:hover {
  transform: translateY(-8px);
  box-shadow: var(--auro-shadow-hover);
  border-color: var(--auro-accent);
}

/* Utility classes */
.min-vh-100 {
  min-height: 100vh;
}

/* Modern Typography - Kaira Inspired */
h1, h2, h3, h4, h5, h6 {
  font-family: var(--auro-heading-font);
  font-weight: 400;
  line-height: 1.24;
  color: var(--auro-text);
}

.display-1, .display-2, .display-3, .display-4, .display-5 {
  font-family: var(--auro-heading-font);
  font-weight: 400;
  letter-spacing: 0.03rem;
}

/* Enhanced Animation classes */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-enter-active,
.slide-leave-active {
  transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-enter-from {
  transform: translateX(-100%);
}

.slide-leave-to {
  transform: translateX(100%);
}

/* Smooth page transitions */
.page-enter-active,
.page-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.page-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

/* Smooth scale animations */
.scale-enter-active,
.scale-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.scale-enter-from,
.scale-leave-to {
  opacity: 0;
  transform: scale(0.95);
}

/* Smooth slide up animations */
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(30px);
}

/* Smooth slide down animations */
.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-down-enter-from,
.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-30px);
}

/* Modern Scrollbar */
::-webkit-scrollbar {
  width: 8px;
}

::-webkit-scrollbar-track {
  background: var(--auro-bg);
}

::-webkit-scrollbar-thumb {
  background: var(--auro-accent);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #b8941f;
}

/* Selection */
::selection {
  background: var(--auro-accent);
  color: var(--auro-dark);
}

/* Focus styles */
*:focus {
  outline: 2px solid var(--auro-accent);
  outline-offset: 2px;
}

/* Smooth scrolling */
html {
  scroll-behavior: smooth;
}

/* Mobile UX Improvements */
@media (max-width: 768px) {
  /* Touch-friendly buttons */
  .btn, .modern-nav-link, .btn-link {
    min-height: 44px;
    min-width: 44px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  /* Improved touch targets */
  .product-card, .category-card, .banner-item {
    margin-bottom: 1rem;
  }
  
  /* Better mobile spacing */
  .container {
    padding-left: 1rem;
    padding-right: 1rem;
  }
  
  /* Mobile-optimized animations */
  .smooth-hover:hover {
    transform: none;
  }
  
  .smooth-hover:active {
    transform: scale(0.98);
  }
  
  /* Mobile-friendly popups */
  .login-popup-container, .register-popup-container {
    margin: 1rem;
    max-height: calc(100vh - 2rem);
  }
  
  /* Mobile navigation improvements */
  .modern-navbar {
    padding: 0.5rem 0;
  }
  
  .modern-nav-link {
    padding: 0.5rem 0.75rem !important;
    font-size: 0.8rem;
  }
  
  /* Mobile form improvements */
  .form-input, .form-control {
    font-size: 16px; /* Prevents zoom on iOS */
    padding: 16px 20px;
  }
  
  /* Mobile-friendly shadows */
  .product-card, .category-card {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }
  
  .product-card:hover, .category-card:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }
}

/* Tablet optimizations */
@media (min-width: 769px) and (max-width: 1024px) {
  .container {
    max-width: 100%;
    padding-left: 2rem;
    padding-right: 2rem;
  }
  
  .product-card .card-img-top {
    height: 220px;
  }
  
  .banner-item .image-holder img {
    height: 250px;
  }
}

/* Large screen optimizations */
@media (min-width: 1200px) {
  .container {
    max-width: 1200px;
  }
  
  .hero-section {
    padding: 100px 80px;
  }
  
  .product-card .card-img-top {
    height: 300px;
  }
}

/* Accessibility improvements */
@media (prefers-reduced-motion: reduce) {
  * {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}

/* High contrast mode support */
@media (prefers-contrast: high) {
  .product-card, .category-card, .banner-item {
    border: 2px solid var(--auro-text);
  }
  
  .modern-nav-link:hover {
    background: var(--auro-text);
    color: var(--auro-bg) !important;
  }
}
</style>
