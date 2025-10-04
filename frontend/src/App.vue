<script setup>
import { onMounted } from 'vue'
import { useCartStore } from './stores/cart'
import ClientLayout from './layouts/ClientLayout.vue'

const cartStore = useCartStore()

onMounted(() => {
  cartStore.loadFromStorage()
})
</script>

<template>
  <div id="app">
    <ClientLayout />
  </div>
</template>

<style>
/* Global styles */
body {
  padding-top: 76px; /* Account for fixed navbar */
}

/* Custom AURO Brand Colors - Light Theme */
:root {
  --auro-primary: #CB3CFF;
  --auro-secondary: #00C2FF;
  --auro-accent: #FFC107;
  --auro-dark: #1E293B;
  --auro-light: #FFFFFF;
  --auro-bg: #F8FAFC;
  --auro-card: #FFFFFF;
}

/* AURO Brand Overrides */
.btn-warning {
  background: linear-gradient(135deg, var(--auro-accent), #FDB52A);
  border: none;
  color: var(--auro-dark);
  font-weight: 600;
  box-shadow: 0 4px 6px -1px rgba(255, 193, 7, 0.3);
}

.btn-warning:hover {
  background: linear-gradient(135deg, #FDB52A, #E0A600);
  transform: translateY(-1px);
  box-shadow: 0 10px 15px -3px rgba(255, 193, 7, 0.4);
}

.text-warning {
  color: var(--auro-accent) !important;
}

/* AURO Brand Navbar */
.navbar-brand {
  background: linear-gradient(135deg, var(--auro-primary), var(--auro-secondary));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-weight: 800;
  letter-spacing: -0.5px;
}

/* AURO Product Cards */
.product-card {
  background: white;
  border-radius: 1rem;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  border: 1px solid rgba(203, 60, 255, 0.1);
  overflow: hidden;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 25px -5px rgba(203, 60, 255, 0.1), 0 10px 10px -5px rgba(0, 194, 255, 0.04);
  border-color: rgba(203, 60, 255, 0.2);
}

.product-card .card-img-top {
  transition: transform 0.3s ease;
}

.product-card:hover .card-img-top {
  transform: scale(1.05);
}

/* AURO Price Display */
.price {
  font-weight: 700;
  background: linear-gradient(135deg, var(--auro-primary), var(--auro-secondary));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* AURO Hero Section */
.hero-section {
  background: linear-gradient(135deg, rgba(203, 60, 255, 0.05), rgba(0, 194, 255, 0.05));
  border-radius: 2rem;
  padding: 3rem;
  margin: 2rem 0;
  border: 1px solid rgba(203, 60, 255, 0.1);
}

/* AURO Feature Icons */
.feature-icon {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, var(--auro-primary), var(--auro-secondary));
  border-radius: 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.5rem;
  margin: 0 auto 1rem;
}

/* AURO Testimonial Cards */
.testimonial-card {
  background: white;
  border-radius: 1.5rem;
  padding: 2rem;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  border-left: 4px solid var(--auro-primary);
  position: relative;
}

.testimonial-card::before {
  content: '"';
  position: absolute;
  top: -10px;
  left: 20px;
  font-size: 4rem;
  color: var(--auro-primary);
  opacity: 0.3;
  font-family: serif;
}

/* AURO Newsletter Section */
.newsletter-section {
  background: linear-gradient(135deg, var(--auro-primary), var(--auro-secondary));
  border-radius: 2rem;
  padding: 3rem;
  color: white;
  text-align: center;
}

.newsletter-section .form-control {
  border: 2px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border-radius: 1rem;
}

.newsletter-section .form-control::placeholder {
  color: rgba(255, 255, 255, 0.7);
}

.newsletter-section .form-control:focus {
  border-color: var(--auro-primary);
  box-shadow: 0 0 0 3px rgba(203, 60, 255, 0.3);
}

/* AURO Footer */
.footer-modern {
  background: linear-gradient(135deg, var(--auro-dark), #334155);
  color: white;
  border-radius: 2rem 2rem 0 0;
  margin-top: 4rem;
}

.footer-modern .social-links a {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: white;
  transition: all 0.3s ease;
  margin: 0 0.5rem;
}

.footer-modern .social-links a:hover {
  background: var(--auro-primary);
  transform: translateY(-2px);
}

/* AURO Loading Animation */
@keyframes auro-pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.auro-loading {
  animation: auro-pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

/* AURO Gradient Text */
.gradient-text {
  background: linear-gradient(135deg, var(--auro-primary), var(--auro-secondary));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* AURO Button Variants */
.btn-auro-primary {
  background: linear-gradient(135deg, var(--auro-primary), #A324CC);
  border: none;
  color: white;
  font-weight: 600;
  border-radius: 0.75rem;
  padding: 0.75rem 2rem;
  transition: all 0.3s ease;
  box-shadow: 0 4px 6px -1px rgba(203, 60, 255, 0.3);
}

.btn-auro-primary:hover {
  background: linear-gradient(135deg, #A324CC, #7D1A99);
  transform: translateY(-2px);
  box-shadow: 0 10px 15px -3px rgba(203, 60, 255, 0.4);
  color: white;
}

.btn-auro-secondary {
  background: linear-gradient(135deg, var(--auro-secondary), #1A9FB3);
  border: none;
  color: white;
  font-weight: 600;
  border-radius: 0.75rem;
  padding: 0.75rem 2rem;
  transition: all 0.3s ease;
  box-shadow: 0 4px 6px -1px rgba(0, 194, 255, 0.3);
}

.btn-auro-secondary:hover {
  background: linear-gradient(135deg, #1A9FB3, #177999);
  transform: translateY(-2px);
  box-shadow: 0 10px 15px -3px rgba(0, 194, 255, 0.4);
  color: white;
}

/* AURO Form Enhancements */
.form-control-auro {
  border: 2px solid rgba(203, 60, 255, 0.2);
  border-radius: 0.75rem;
  padding: 0.75rem 1rem;
  transition: all 0.3s ease;
  background: white;
}

.form-control-auro:focus {
  border-color: var(--auro-primary);
  box-shadow: 0 0 0 3px rgba(203, 60, 255, 0.1);
  outline: none;
}

/* AURO Card Enhancements */
.card-auro {
  background: var(--auro-card);
  border-radius: 1.5rem;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(203, 60, 255, 0.1);
  transition: all 0.3s ease;
  overflow: hidden;
}

.card-auro:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 25px -5px rgba(203, 60, 255, 0.1), 0 10px 10px -5px rgba(0, 194, 255, 0.04);
}

/* Utility classes */
.min-vh-100 {
  min-height: 100vh;
}

/* Animation classes */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-enter-active,
.slide-leave-active {
  transition: transform 0.3s ease;
}

.slide-enter-from {
  transform: translateX(-100%);
}

.slide-leave-to {
  transform: translateX(100%);
}
</style>
