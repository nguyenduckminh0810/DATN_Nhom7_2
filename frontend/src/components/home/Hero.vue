<template>
  <section class="hero-section">
    <div class="container">
      <div class="hero-content">
        <div class="hero-text">
          <h1 class="hero-title">Thời trang nam cao cấp</h1>
          <p class="hero-description">
            Khám phá bộ sưu tập áo quần nam đa dạng, chất lượng cao với giá cả hợp lý
          </p>
          <div class="hero-actions">
            <router-link to="/san-pham?loai=ao" class="btn btn-primary btn-lg">
              Mua Áo
            </router-link>
            <router-link to="/san-pham?loai=quan" class="btn btn-outline-primary btn-lg">
              Mua Quần
            </router-link>
          </div>
        </div>
        <div class="hero-image">
          <img
            src="https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80"
            alt="Thời trang nam cao cấp"
            class="hero-img"
          />
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
// No props needed for this component
</script>

<style scoped>
.hero-section {
  padding: 2.5rem 0;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
}

.hero-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
  align-items: center;
}

.hero-title {
  font-size: 2.5rem;
  font-weight: 800;
  color: #212529;
  margin-bottom: 1rem;
  line-height: 1.2;
}

.hero-description {
  font-size: 1.1rem;
  color: #6c757d;
  margin-bottom: 1.5rem;
  line-height: 1.6;
}

.hero-actions {
  display: flex;
  gap: 1rem;
}

.btn {
  padding: 0.75rem 2rem;
  border-radius: 50px;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.3s ease;
  display: inline-block;
}

.btn-primary {
  background: #000;
  color: white;
  border: 2px solid #000;
}

.btn-primary:hover {
  background: #333;
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}

.btn-outline-primary {
  background: transparent;
  color: #000;
  border: 2px solid #000;
}

.btn-outline-primary:hover {
  background: #000;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}

.hero-image {
  position: relative;
}

.hero-img {
  width: 100%;
  height: 380px;
  object-fit: cover;
  border-radius: 24px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

@media (max-width: 992px) {
  .hero-content {
    grid-template-columns: 1fr;
    gap: 1.5rem;
    text-align: center;
  }

  .hero-title {
    font-size: 2rem;
  }

  .hero-description {
    font-size: 1rem;
  }

  .hero-img {
    height: 320px;
  }
}

@media (max-width: 768px) {
  .hero-section {
    padding: 2rem 0;
  }

  .hero-title {
    font-size: 1.75rem;
  }

  .hero-description {
    font-size: 0.95rem;
  }

  .hero-actions {
    flex-direction: column;
    align-items: center;
  }

  .btn {
    width: 200px;
    text-align: center;
  }

  .hero-img {
    height: 280px;
  }
}
</style>
