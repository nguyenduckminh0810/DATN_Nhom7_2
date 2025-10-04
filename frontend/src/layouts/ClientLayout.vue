<template>
  <div class="client-layout">
    <!-- Navigation -->
    <Navbar />
    
    <!-- Main Content -->
    <main class="main-content">
      <router-view />
    </main>
    
    <!-- Footer -->
    <Footer />
  </div>
</template>

<script setup>
import Navbar from '../components/client/Navbar.vue'
import Footer from '../components/client/Footer.vue'
</script>

<style scoped>
.client-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
}
</style>
