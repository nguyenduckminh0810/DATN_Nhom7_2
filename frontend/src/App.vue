<script setup>
import { onMounted } from 'vue'
import { useCartStore } from './stores/cart'
import ClientLayout from './layouts/ClientLayout.vue'
import Toast from './components/ui/Toast.vue'

const cartStore = useCartStore()

onMounted(() => {
  cartStore.loadFromStorage()
})
</script>

<template>
  <div id="app">
    <ClientLayout />
    <Toast />
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
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  line-height: 1.6;
  color: #1a1a1a;
  background-color: #fafafa;
  padding-top: 80px;
}

/* Custom AURO Brand Colors - Modern Theme */
:root {
  --auro-primary: #1a1a1a;
  --auro-secondary: #2c2c2c;
  --auro-accent: #d4af37;
  --auro-dark: #0f0f0f;
  --auro-light: #ffffff;
  --auro-bg: #fafafa;
  --auro-card: #ffffff;
  --auro-border: #e5e5e5;
  --auro-text: #1a1a1a;
  --auro-text-light: #666666;
  --auro-hover: #f5f5f5;
  --auro-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  --auro-shadow-hover: 0 8px 30px rgba(0, 0, 0, 0.12);
  --auro-gradient: linear-gradient(135deg, #1a1a1a 0%, #2c2c2c 100%);
  --auro-gradient-accent: linear-gradient(135deg, #d4af37 0%, #f4d03f 100%);
}

/* AURO Brand Overrides */
.btn-warning {
  background: var(--auro-gradient-accent);
  border: none;
  color: var(--auro-dark);
  font-weight: 600;
  border-radius: 8px;
  padding: 12px 24px;
  transition: all 0.3s ease;
  box-shadow: var(--auro-shadow);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  font-size: 14px;
}

.btn-warning:hover {
  transform: translateY(-2px);
  box-shadow: var(--auro-shadow-hover);
  color: var(--auro-dark);
}

.text-warning {
  color: var(--auro-accent) !important;
}

/* AURO Brand Navbar */
.navbar-brand {
  font-weight: 800;
  letter-spacing: 2px;
  font-size: 24px;
  color: var(--auro-primary) !important;
}

/* AURO Product Cards */
.product-card {
  background: var(--auro-card);
  border-radius: 16px;
  box-shadow: var(--auro-shadow);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
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
  height: 4px;
  background: var(--auro-gradient-accent);
  transform: scaleX(0);
  transition: transform 0.3s ease;
}

.product-card:hover::before {
  transform: scaleX(1);
}

.product-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--auro-shadow-hover);
  border-color: var(--auro-accent);
}

.product-card .card-img-top {
  transition: transform 0.4s ease;
  height: 280px;
  object-fit: cover;
}

.product-card:hover .card-img-top {
  transform: scale(1.08);
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

/* AURO Loading Animation */
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

.auro-loading {
  animation: auro-pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

.auro-float {
  animation: auro-float 3s ease-in-out infinite;
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

/* Modern Typography */
h1, h2, h3, h4, h5, h6 {
  font-weight: 700;
  line-height: 1.2;
  color: var(--auro-text);
}

.display-1, .display-2, .display-3, .display-4, .display-5 {
  font-weight: 800;
  letter-spacing: -0.02em;
}

/* Animation classes */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.4s ease;
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
</style>
