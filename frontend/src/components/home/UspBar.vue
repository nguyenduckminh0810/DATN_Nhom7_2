<template>
  <div class="usp-bar">
    <div class="container">
      <div class="usp-content">
        <div class="usp-item">
          <i class="ph ph-truck"></i>
          <span>Freeship từ 499k</span>
        </div>
        <div class="usp-item">
          <i class="ph ph-arrow-clockwise"></i>
          <span>Đổi trả 7 ngày</span>
        </div>
        <div class="usp-item">
          <i class="ph ph-credit-card"></i>
          <span>Thanh toán COD</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
// No props needed for this component
</script>

<style scoped>
.usp-bar {
  background: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
  padding: 0.75rem 0;
}

.usp-content {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 3rem;
}

.usp-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1rem;
  font-weight: 600;
  color: #495057;
}

.usp-item i {
  font-size: 1.25rem;
  color: #28a745;
}

@media (max-width: 768px) {
  .usp-content {
    gap: 1.5rem;
  }
  
  .usp-item {
    font-size: 0.8rem;
  }
  
  .usp-item i {
    font-size: 0.9rem;
  }
}

@media (max-width: 576px) {
  .usp-content {
    flex-direction: column;
    gap: 0.75rem;
  }
  
  .usp-item {
    font-size: 0.75rem;
  }
}
</style>
