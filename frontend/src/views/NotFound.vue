<template>
  <div class="not-found">
    <div class="container">
      <div class="row justify-content-center">
        <div class="col-md-6 text-center">
          <div class="error-content py-5">
            <h1 class="display-1 fw-bold text-muted">404</h1>
            <h2 class="h3 mb-3">Không tìm thấy trang</h2>
            <p class="text-muted mb-4">
              Xin lỗi, trang bạn đang tìm kiếm không tồn tại hoặc đã bị di chuyển.
            </p>
            <div class="d-flex gap-3 justify-content-center">
              <router-link to="/" class="btn btn-primary">
                <i class="bi bi-house me-2"></i>Về trang chủ
              </router-link>
              <button @click="$router.go(-1)" class="btn btn-outline-secondary">
                <i class="bi bi-arrow-left me-2"></i>Quay lại
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
// No additional logic needed for 404 page
</script>

<style scoped>
.not-found {
  min-height: 60vh;
  display: flex;
  align-items: center;
}

.error-content {
  padding: 2rem 0;
}
</style>
