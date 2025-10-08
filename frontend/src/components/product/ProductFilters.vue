<template>
  <div class="product-filters">
    <!-- Filter Header -->
    <div class="filter-header mb-4">
      <div class="d-flex justify-content-between align-items-center">
        <h5 class="mb-0">
          <i class="ph-funnel me-2"></i>Bộ lọc
        </h5>
        <button 
          v-if="hasActiveFilters" 
          class="btn btn-sm btn-outline-secondary"
          @click="clearAllFilters"
        >
          <i class="ph-x me-1"></i>Xóa tất cả
        </button>
      </div>
      <div v-if="activeFiltersCount > 0" class="mt-2">
        <small class="text-muted">
          {{ activeFiltersCount }} bộ lọc đang áp dụng
        </small>
      </div>
    </div>

    <!-- Price Filter -->
    <div class="filter-section mb-4">
      <h6 class="filter-title">
        <i class="ph-currency-circle-dollar me-2"></i>Khoảng giá
      </h6>
      
      <!-- Price Range Slider -->
      <div class="price-range-container">
        <div class="price-inputs d-flex gap-2 mb-3">
          <input 
            type="number" 
            class="form-control form-control-sm" 
            placeholder="Từ"
            :value="priceRange.min"
            @input="updateMinPrice"
          >
          <span class="align-self-center">-</span>
          <input 
            type="number" 
            class="form-control form-control-sm" 
            placeholder="Đến"
            :value="priceRange.max"
            @input="updateMaxPrice"
          >
        </div>
        
        <!-- Range Slider -->
        <div class="range-slider-container">
          <input 
            type="range" 
            class="form-range price-slider" 
            min="0" 
            max="5000000" 
            step="100000"
            :value="priceRange.min"
            @input="updateMinPriceFromSlider"
          >
          <input 
            type="range" 
            class="form-range price-slider" 
            min="0" 
            max="5000000" 
            step="100000"
            :value="priceRange.max"
            @input="updateMaxPriceFromSlider"
          >
        </div>
        
        <div class="price-display mt-2">
          <small class="text-muted">
            {{ formatPrice(priceRange.min) }} - {{ formatPrice(priceRange.max) }}
          </small>
        </div>
      </div>

      <!-- Quick Price Ranges -->
      <div class="quick-price-ranges mt-3">
        <div class="d-flex flex-wrap gap-2">
          <button 
            v-for="range in priceRanges" 
            :key="range.label"
            class="btn btn-sm btn-outline-primary quick-range-btn"
            :class="{ 'active': isPriceRangeActive(range) }"
            @click="selectPriceRange(range)"
          >
            {{ range.label }}
          </button>
        </div>
      </div>
    </div>

    <!-- Size Filter -->
    <div class="filter-section mb-4">
      <h6 class="filter-title">
        <i class="ph-ruler me-2"></i>Kích thước
        <button 
          v-if="activeFilters.sizes.length > 0" 
          class="btn btn-sm btn-link p-0 ms-2"
          @click="clearFilter('sizes')"
        >
          <i class="ph-x"></i>
        </button>
      </h6>
      <div class="size-grid">
        <button 
          v-for="size in availableSizes" 
          :key="size"
          class="btn btn-sm size-btn"
          :class="{ 'active': activeFilters.sizes.includes(size) }"
          @click="toggleSize(size)"
        >
          {{ size }}
        </button>
      </div>
    </div>

    <!-- Color Filter -->
    <div class="filter-section mb-4">
      <h6 class="filter-title">
        <i class="ph-palette me-2"></i>Màu sắc
        <button 
          v-if="activeFilters.colors.length > 0" 
          class="btn btn-sm btn-link p-0 ms-2"
          @click="clearFilter('colors')"
        >
          <i class="ph-x"></i>
        </button>
      </h6>
      <div class="color-grid">
        <button 
          v-for="color in availableColors" 
          :key="color.value"
          class="btn color-btn"
          :class="{ 'active': activeFilters.colors.includes(color.value) }"
          :style="{ backgroundColor: color.hex }"
          :title="color.name"
          @click="toggleColor(color.value)"
        >
          <span class="visually-hidden">{{ color.name }}</span>
        </button>
      </div>
    </div>

    <!-- Brand Filter -->
    <div class="filter-section mb-4">
      <h6 class="filter-title">
        <i class="ph-trademark me-2"></i>Thương hiệu
        <button 
          v-if="activeFilters.brands.length > 0" 
          class="btn btn-sm btn-link p-0 ms-2"
          @click="clearFilter('brands')"
        >
          <i class="ph-x"></i>
        </button>
      </h6>
      <div class="brand-list">
        <div 
          v-for="brand in availableBrands" 
          :key="brand"
          class="form-check"
        >
          <input 
            class="form-check-input" 
            type="checkbox" 
            :id="`brand-${brand}`"
            :checked="activeFilters.brands.includes(brand)"
            @change="toggleBrand(brand)"
          >
          <label class="form-check-label" :for="`brand-${brand}`">
            {{ brand }}
          </label>
        </div>
      </div>
    </div>

    <!-- Availability Filter -->
    <div class="filter-section mb-4">
      <h6 class="filter-title">
        <i class="ph-package me-2"></i>Tình trạng
        <button 
          v-if="activeFilters.availability !== 'all'" 
          class="btn btn-sm btn-link p-0 ms-2"
          @click="clearFilter('availability')"
        >
          <i class="ph-x"></i>
        </button>
      </h6>
      <div class="availability-list">
        <div class="form-check">
          <input 
            class="form-check-input" 
            type="radio" 
            id="availability-all"
            value="all"
            v-model="activeFilters.availability"
          >
          <label class="form-check-label" for="availability-all">
            Tất cả
          </label>
        </div>
        <div class="form-check">
          <input 
            class="form-check-input" 
            type="radio" 
            id="availability-instock"
            value="inStock"
            v-model="activeFilters.availability"
          >
          <label class="form-check-label" for="availability-instock">
            Còn hàng
          </label>
        </div>
        <div class="form-check">
          <input 
            class="form-check-input" 
            type="radio" 
            id="availability-outstock"
            value="outOfStock"
            v-model="activeFilters.availability"
          >
          <label class="form-check-label" for="availability-outstock">
            Hết hàng
          </label>
        </div>
      </div>
    </div>

    <!-- Sort Options -->
    <div class="filter-section">
      <h6 class="filter-title">
        <i class="ph-sort-ascending me-2"></i>Sắp xếp
      </h6>
      <select class="form-select form-select-sm" v-model="activeFilters.sortBy">
        <option value="relevance">Độ liên quan</option>
        <option value="price-asc">Giá tăng dần</option>
        <option value="price-desc">Giá giảm dần</option>
        <option value="name-asc">Tên A-Z</option>
        <option value="name-desc">Tên Z-A</option>
        <option value="discount-desc">Giảm giá cao nhất</option>
        <option value="newest">Mới nhất</option>
        <option value="popular">Phổ biến nhất</option>
      </select>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useSearchStore } from '../../stores/search'

const searchStore = useSearchStore()

// Local state
const priceRange = ref({ min: 0, max: 5000000 })

// Computed
const activeFilters = computed(() => searchStore.activeFilters)
const hasActiveFilters = computed(() => searchStore.hasActiveFilters)
const activeFiltersCount = computed(() => searchStore.activeFiltersCount)
const availableSizes = computed(() => searchStore.availableSizes)
const availableColors = computed(() => searchStore.availableColors)
const availableBrands = computed(() => searchStore.availableBrands)
const priceRanges = computed(() => searchStore.priceRanges)

// Methods
const formatPrice = (price) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    maximumFractionDigits: 0
  }).format(price)
}

const updateMinPrice = (event) => {
  const value = parseInt(event.target.value) || 0
  priceRange.value.min = Math.max(0, Math.min(value, priceRange.value.max - 100000))
  searchStore.setPriceRange(priceRange.value.min, priceRange.value.max)
}

const updateMaxPrice = (event) => {
  const value = parseInt(event.target.value) || 5000000
  priceRange.value.max = Math.min(5000000, Math.max(value, priceRange.value.min + 100000))
  searchStore.setPriceRange(priceRange.value.min, priceRange.value.max)
}

const updateMinPriceFromSlider = (event) => {
  const value = parseInt(event.target.value)
  priceRange.value.min = Math.min(value, priceRange.value.max - 100000)
  searchStore.setPriceRange(priceRange.value.min, priceRange.value.max)
}

const updateMaxPriceFromSlider = (event) => {
  const value = parseInt(event.target.value)
  priceRange.value.max = Math.max(value, priceRange.value.min + 100000)
  searchStore.setPriceRange(priceRange.value.min, priceRange.value.max)
}

const isPriceRangeActive = (range) => {
  return priceRange.value.min === range.min && priceRange.value.max === range.max
}

const selectPriceRange = (range) => {
  priceRange.value = { min: range.min, max: range.max }
  searchStore.setPriceRange(range.min, range.max)
}

const toggleSize = (size) => {
  searchStore.toggleSize(size)
}

const toggleColor = (color) => {
  searchStore.toggleColor(color)
}

const toggleBrand = (brand) => {
  searchStore.toggleBrand(brand)
}

const clearFilter = (filterType) => {
  searchStore.clearFilter(filterType)
}

const clearAllFilters = () => {
  searchStore.clearAllFilters()
  priceRange.value = { min: 0, max: 5000000 }
}

// Watch for filter changes
watch(() => activeFilters.value.priceRange, (newRange) => {
  priceRange.value = { ...newRange }
}, { deep: true })
</script>

<style scoped>
.product-filters {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #f0f0f0;
}

.filter-header {
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 16px;
}

.filter-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--auro-dark);
  margin-bottom: 16px;
  display: flex;
  align-items: center;
}

.filter-section {
  border-bottom: 1px solid #f8f9fa;
  padding-bottom: 20px;
}

.filter-section:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

/* Price Range */
.price-range-container {
  margin-bottom: 16px;
}

.range-slider-container {
  position: relative;
  height: 20px;
}

.price-slider {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  background: transparent;
  pointer-events: none;
}

.price-slider::-webkit-slider-thumb {
  pointer-events: all;
  background: var(--auro-accent);
  border: 2px solid white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.price-slider::-moz-range-thumb {
  background: var(--auro-accent);
  border: 2px solid white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.quick-range-btn {
  border-radius: 20px;
  padding: 4px 12px;
  font-size: 0.8rem;
  transition: all 0.3s ease;
}

.quick-range-btn.active {
  background: var(--auro-accent);
  border-color: var(--auro-accent);
  color: var(--auro-dark);
}

/* Size Grid */
.size-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(40px, 1fr));
  gap: 8px;
}

.size-btn {
  border-radius: 8px;
  padding: 8px 12px;
  border: 1px solid #e9ecef;
  background: white;
  color: #495057;
  transition: all 0.3s ease;
  font-weight: 500;
}

.size-btn:hover {
  border-color: var(--auro-accent);
  color: var(--auro-accent);
}

.size-btn.active {
  background: var(--auro-accent);
  border-color: var(--auro-accent);
  color: var(--auro-dark);
}

/* Color Grid */
.color-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(32px, 1fr));
  gap: 8px;
}

.color-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 2px solid transparent;
  position: relative;
  transition: all 0.3s ease;
}

.color-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.color-btn.active {
  border-color: var(--auro-dark);
  box-shadow: 0 0 0 2px white, 0 0 0 4px var(--auro-accent);
}

.color-btn.active::after {
  content: '✓';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: white;
  font-weight: bold;
  text-shadow: 0 0 2px rgba(0, 0, 0, 0.5);
}

/* Brand List */
.brand-list .form-check {
  padding: 8px 0;
}

.brand-list .form-check-input {
  border-radius: 4px;
  border-color: #dee2e6;
}

.brand-list .form-check-input:checked {
  background-color: var(--auro-accent);
  border-color: var(--auro-accent);
}

.brand-list .form-check-label {
  font-size: 0.9rem;
  color: #495057;
  cursor: pointer;
}

/* Availability List */
.availability-list .form-check {
  padding: 8px 0;
}

.availability-list .form-check-input {
  border-color: #dee2e6;
}

.availability-list .form-check-input:checked {
  background-color: var(--auro-accent);
  border-color: var(--auro-accent);
}

.availability-list .form-check-label {
  font-size: 0.9rem;
  color: #495057;
  cursor: pointer;
}

/* Sort Select */
.form-select {
  border-radius: 8px;
  border-color: #dee2e6;
  font-size: 0.9rem;
}

.form-select:focus {
  border-color: var(--auro-accent);
  box-shadow: 0 0 0 0.2rem rgba(212, 175, 55, 0.25);
}

/* Responsive */
@media (max-width: 768px) {
  .product-filters {
    padding: 16px;
  }
  
  .size-grid {
    grid-template-columns: repeat(6, 1fr);
  }
  
  .color-grid {
    grid-template-columns: repeat(8, 1fr);
  }
}
</style>
