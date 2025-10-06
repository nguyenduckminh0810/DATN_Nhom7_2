<template>
  <div class="product-card section-item" itemscope itemtype="https://schema.org/Product">
    <!-- Image Section - Separate and larger -->
    <div class="product-image-section">
      <div class="product-image-container" @mouseenter="showSizePopup = true" @mouseleave="showSizePopup = false">
        <img 
          :src="img" 
          :alt="name"
          class="product-image section-item__image"
          itemprop="image"
          loading="lazy"
        />
        <!-- Hover Image -->
        <img 
          v-if="hoverImg"
          :src="hoverImg" 
          :alt="name"
          class="product-image-hover"
        />
        <div v-if="discount" class="discount-badge">
          -{{ discount }}%
        </div>
        <!-- Promotional Badge -->
        <div v-if="promotionalBadge" class="promotional-badge">
          {{ promotionalBadge }}
        </div>
        
        <!-- Size Popup with Blur Effect -->
        <div v-if="showSizePopup" class="size-popup">
          <div class="size-popup-content">
            <div class="size-popup-header">
              <span class="size-popup-title">Thêm nhanh vào giỏ hàng +</span>
            </div>
            <div class="size-options">
              <button 
                v-for="size in sizes" 
                :key="size"
                class="size-option"
                :class="{ 
                  'unavailable': !currentSizes.includes(size),
                  'selected': selectedSize === size
                }"
                @click="selectSize(size)"
                :disabled="!currentSizes.includes(size)"
              >
                {{ size }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Product Info Section - Separate and smaller -->
    <div class="product-info-section">
      <h3 class="product-name" itemprop="name">{{ name }}</h3>
      <div class="product-price">
        <span class="price-current" itemprop="offers" itemscope itemtype="https://schema.org/Offer">
          <span itemprop="price" :content="priceNow">{{ formatPrice(priceNow) }}</span>
          <span itemprop="priceCurrency" content="VND">₫</span>
        </span>
        <span v-if="priceOld" class="price-old">{{ formatPrice(priceOld) }}₫</span>
        <span v-if="discount" class="discount-text">-{{ discount }}%</span>
      </div>
      
      <!-- Color Options -->
      <div v-if="colorOptions && colorOptions.length > 0" class="color-options">
        <div 
          v-for="color in colorOptions" 
          :key="color"
          class="color-swatch"
          :class="{ 'selected': selectedColor === color }"
          :style="{ backgroundColor: color }"
          @click="selectColor(color)"
        ></div>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  name: {
    type: String,
    required: true
  },
  img: {
    type: String,
    required: true
  },
  hoverImg: {
    type: String,
    default: null
  },
  priceNow: {
    type: Number,
    required: true
  },
  priceOld: {
    type: Number,
    default: null
  },
  discount: {
    type: Number,
    default: null
  },
  promotionalBadge: {
    type: String,
    default: null
  },
  colorOptions: {
    type: Array,
    default: () => []
  },
  sizes: {
    type: Array,
    default: () => ['S', 'M', 'L', 'XL', '2XL', '3XL']
  },
  availableSizes: {
    type: Array,
    default: () => ['S', 'M', 'L', 'XL', '2XL', '3XL']
  },
  colorSizeMapping: {
    type: Object,
    default: () => ({})
  }
})

import { ref, computed } from 'vue'

const showSizePopup = ref(false)
const selectedSize = ref(null)
const selectedColor = ref(null)

// Computed để lấy size theo màu được chọn
const currentSizes = computed(() => {
  if (selectedColor.value && props.colorSizeMapping[selectedColor.value]) {
    return props.colorSizeMapping[selectedColor.value]
  }
  return props.availableSizes
})

const formatPrice = (price) => {
  return new Intl.NumberFormat('vi-VN').format(price)
}

const selectSize = (size) => {
  if (currentSizes.value.includes(size)) {
    selectedSize.value = size
  }
}

const selectColor = (color) => {
  selectedColor.value = color
  selectedSize.value = null // Reset size khi chọn màu mới
}
</script>

<style scoped>
.product-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  height: 100%;
  display: flex;
  flex-direction: column;
  flex: 0 0 calc((100vw - 120px) / 5); /* 5 sản phẩm chia đều chiều ngang */
  max-width: 400px;
  min-height: 650px; /* Giảm từ 700px vì product-info nhỏ hơn */
}

.product-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

/* Image Section - Separate and larger */
.product-image-section {
  flex: 1; /* Chiếm phần lớn không gian */
  display: flex;
  flex-direction: column;
}

.product-image-container {
  position: relative;
  height: 580px; /* Tăng từ 550px để ảnh lớn hơn */
  overflow: hidden;
  flex: 1; /* Chiếm toàn bộ không gian của section */
}

.product-image {
  width: 100%;
  height: 580px; /* Tăng từ 550px */
  object-fit: cover;
  transition: opacity 0.3s ease;
}

.product-image-hover {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 580px;
  object-fit: cover;
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: 1;
}

.product-image-container:hover .product-image {
  opacity: 0;
}

.product-image-container:hover .product-image-hover {
  opacity: 1;
}

.discount-badge {
  position: absolute;
  top: 1rem;
  right: 1rem;
  background: #dc3545;
  color: white;
  padding: 0.25rem 0.5rem;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
}

.promotional-badge {
  position: absolute;
  bottom: 1rem;
  left: 1rem;
  background: #007bff;
  color: white;
  padding: 0.5rem 0.75rem;
  border-radius: 8px;
  font-size: 0.75rem;
  font-weight: 600;
  max-width: 80%;
  line-height: 1.2;
}

/* Product Info Section - Separate and smaller */
.product-info-section {
  flex: 0 0 auto; /* Không co giãn, kích thước cố định */
  padding: 1rem; /* Giảm padding */
  display: flex;
  flex-direction: column;
  gap: 0.5rem; /* Giảm gap */
  background: #fff;
  border-top: 1px solid #f0f0f0; /* Đường phân cách */
}

.product-name {
  font-size: 1rem; /* Giảm từ 1.25rem */
  font-weight: 600;
  color: #212529;
  margin-bottom: 0; /* Bỏ margin vì đã có gap */
  line-height: 1.3; /* Giảm line-height */
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 2rem; /* Giảm từ 2.5rem */
}

.product-price {
  display: flex;
  align-items: center;
  gap: 0.5rem; /* Giảm từ 0.75rem */
  margin-bottom: 0; /* Bỏ margin vì đã có gap */
}

.price-current {
  font-size: 1.25rem; /* Giảm từ 1.75rem */
  font-weight: 700;
  color: #dc3545;
  line-height: 1.2;
}

.price-old {
  font-size: 1rem; /* Giảm từ 1.25rem */
  color: #6c757d;
  text-decoration: line-through;
}

.discount-text {
  font-size: 0.875rem; /* Giảm từ 1rem */
  color: #dc3545;
  font-weight: 600;
}

.color-options {
  display: flex;
  gap: 0.5rem; /* Giảm từ 0.75rem */
  margin-top: 0; /* Bỏ margin vì đã có gap */
}

.color-swatch {
  width: 20px; /* Giảm từ 28px */
  height: 20px; /* Giảm từ 28px */
  border-radius: 50%;
  border: 2px solid #e9ecef;
  cursor: pointer;
  transition: all 0.2s ease;
}

.color-swatch:hover {
  border-color: #007bff;
  transform: scale(1.1);
}

.color-swatch.selected {
  border-color: #007bff;
  border-width: 3px;
  transform: scale(1.1);
}

/* Size Popup with Blur Effect */
.size-popup {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.9), rgba(147, 51, 234, 0.9));
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-radius: 0 0 12px 12px;
  padding: 1rem;
  z-index: 10;
  animation: slideUp 0.3s ease;
}

.size-popup-content {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.size-popup-header {
  text-align: center;
}

.size-popup-title {
  color: white;
  font-size: 0.875rem;
  font-weight: 600;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.size-options {
  display: flex;
  gap: 0.5rem;
  justify-content: center;
  flex-wrap: wrap;
}

.size-option {
  background: white;
  border: 2px solid transparent;
  border-radius: 6px;
  padding: 0.5rem 0.75rem;
  font-size: 0.875rem;
  font-weight: 600;
  color: #374151;
  cursor: pointer;
  transition: all 0.2s ease;
  min-width: 40px;
  text-align: center;
}

.size-option:hover:not(.unavailable) {
  border-color: #3b82f6;
  background: #f8fafc;
  transform: translateY(-1px);
}

.size-option.selected {
  border-color: #3b82f6;
  background: #3b82f6;
  color: white;
}

.size-option.unavailable {
  background: #f3f4f6;
  color: #9ca3af;
  cursor: not-allowed;
  opacity: 0.5;
  text-decoration: line-through;
}

@keyframes slideUp {
  from {
    transform: translateY(100%);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

/* Responsive breakpoints for product cards */
@media (max-width: 1400px) {
  .product-card {
    flex: 0 0 calc((100vw - 100px) / 4); /* 4 sản phẩm */
    max-width: 350px; /* Tăng từ 300px */
  }
}

@media (max-width: 1024px) {
  .product-card {
    flex: 0 0 calc((100vw - 80px) / 3); /* 3 sản phẩm */
    max-width: 320px; /* Tăng từ 280px */
  }
}

@media (max-width: 768px) {
  .product-card {
    flex: 0 0 calc((100vw - 60px) / 2); /* 2 sản phẩm */
    max-width: 280px;
    min-height: 550px; /* Giảm từ 600px */
  }
  
  .product-image-container {
    height: 450px; /* Tăng từ 400px để ảnh lớn hơn */
  }
  
  .product-image {
    height: 450px; /* Tăng từ 400px */
  }
  
  .product-info-section {
    padding: 0.75rem; /* Giảm padding cho mobile */
  }
  
  .product-name {
    font-size: 0.875rem; /* Giảm cho mobile */
    min-height: 1.75rem; /* Giảm cho mobile */
  }
  
  .price-current {
    font-size: 1.125rem; /* Giảm cho mobile */
  }
  
  .price-old {
    font-size: 0.875rem; /* Giảm cho mobile */
  }
}

@media (max-width: 576px) {
  .product-card {
    flex: 0 0 calc((100vw - 40px) / 2);
    max-width: 220px;
    min-height: 450px; /* Giảm từ 500px */
  }
  
  .product-image-container {
    height: 350px; /* Giữ nguyên để ảnh lớn */
  }
  
  .product-image {
    height: 350px; /* Giữ nguyên */
  }
  
  .product-info-section {
    padding: 0.5rem; /* Giảm padding cho mobile nhỏ */
  }
  
  .product-name {
    font-size: 0.875rem; /* Giảm cho mobile nhỏ */
    min-height: 1.5rem; /* Giảm cho mobile nhỏ */
  }
  
  .price-current {
    font-size: 1rem; /* Giảm cho mobile nhỏ */
  }
  
  .price-old {
    font-size: 0.75rem; /* Giảm cho mobile nhỏ */
  }
}
</style>
