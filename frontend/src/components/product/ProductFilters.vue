<template>
  <div class="product-filters">
    <!-- Filter Header -->
    <div class="filter-header mb-4">
      <div class="d-flex justify-content-between align-items-center">
        <h5 class="mb-0"><i class="bi bi-funnel me-2"></i>Bộ lọc</h5>
        <button
          v-if="hasActiveFilters"
          class="btn btn-sm btn-outline-secondary"
          @click="clearAllFilters"
        >
          <i class="bi bi-x me-1"></i>Xóa tất cả
        </button>
      </div>
      <div v-if="activeFiltersCount > 0" class="mt-2">
        <small class="text-muted"> {{ activeFiltersCount }} bộ lọc đang áp dụng </small>
      </div>
    </div>

    <!-- Price Filter -->
    <div class="filter-section mb-4">
      <h6 class="filter-title"><i class="bi bi-currency-dollar me-2"></i>Khoảng giá</h6>

      <!-- Price Inputs -->
      <div class="price-inputs d-flex gap-2 mb-3">
        <input
          type="number"
          class="form-control form-control-sm"
          placeholder="0"
          :value="formatInputPrice(priceRange.min)"
          @input="updateMinPrice"
          min="0"
          step="50000"
        />
        <span class="align-self-center text-muted">-</span>
        <input
          type="number"
          class="form-control form-control-sm"
          placeholder="5000000"
          :value="formatInputPrice(priceRange.max)"
          @input="updateMaxPrice"
          min="0"
          step="50000"
        />
      </div>

      <!-- Current Range Display -->
      <div class="price-display mb-3">
        <small class="text-muted">
          {{ formatPrice(priceRange.min) }} - {{ formatPrice(priceRange.max) }}
        </small>
      </div>

      <!-- Quick Price Ranges -->
      <div class="quick-price-ranges">
        <div class="d-flex flex-wrap gap-2">
          <button
            v-for="range in priceRanges"
            :key="range.label"
            class="btn btn-sm btn-outline-warning quick-range-btn"
            :class="{ active: isPriceRangeActive(range) }"
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
        <i class="bi bi-rulers me-2"></i>Kích thước
        <button
          v-if="activeFilters.sizes.length > 0"
          class="btn btn-sm btn-link p-0 ms-2"
          @click="clearFilter('sizes')"
        >
          <i class="bi bi-x"></i>
        </button>
      </h6>
      <div class="size-grid">
        <button
          v-for="size in availableSizes"
          :key="size"
          class="btn btn-sm size-btn"
          :class="{ active: activeFilters.sizes.includes(size) }"
          @click="toggleSize(size)"
        >
          {{ size }}
        </button>
      </div>
    </div>

    <!-- Color Filter -->
    <div class="filter-section mb-4">
      <h6 class="filter-title">
        <i class="bi bi-palette me-2"></i>Màu sắc
        <button
          v-if="activeFilters.colors.length > 0"
          class="btn btn-sm btn-link p-0 ms-2"
          @click="clearFilter('colors')"
        >
          <i class="bi bi-x"></i>
        </button>
      </h6>
      <div class="color-grid">
        <button
          v-for="color in availableColors"
          :key="color.value"
          class="btn color-btn"
          :class="{ active: activeFilters.colors.includes(color.value) }"
          :style="{ backgroundColor: color.hex }"
          :title="color.name"
          @click="toggleColor(color.value)"
        >
          <span class="color-name" :style="{ color: getTextColor(color.hex) }">
            {{ color.name }}
          </span>
        </button>
      </div>
    </div>

    <!-- Material Filter -->
    <div class="filter-section mb-4">
      <h6 class="filter-title">
        <i class="bi bi-grid-3x3-gap me-2"></i>Chất liệu
        <button
          v-if="activeFilters.materials.length > 0"
          class="btn btn-sm btn-link p-0 ms-2"
          @click="clearFilter('materials')"
        >
          <i class="bi bi-x"></i>
        </button>
      </h6>
      <div class="material-list">
        <div v-for="material in availableMaterials" :key="material" class="form-check">
          <input
            class="form-check-input"
            type="checkbox"
            :id="`material-${material}`"
            :checked="activeFilters.materials.includes(material)"
            @change="toggleMaterial(material)"
          />
          <label class="form-check-label" :for="`material-${material}`">
            {{ material }}
          </label>
        </div>
      </div>
    </div>

    <!-- Availability Filter -->
    <div class="filter-section mb-4">
      <h6 class="filter-title">
        <i class="bi bi-box me-2"></i>Tình trạng
        <button
          v-if="activeFilters.availability !== 'all'"
          class="btn btn-sm btn-link p-0 ms-2"
          @click="clearFilter('availability')"
        >
          <i class="bi bi-x"></i>
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
          />
          <label class="form-check-label" for="availability-all"> Tất cả </label>
        </div>
        <div class="form-check">
          <input
            class="form-check-input"
            type="radio"
            id="availability-instock"
            value="inStock"
            v-model="activeFilters.availability"
          />
          <label class="form-check-label" for="availability-instock"> Còn hàng </label>
        </div>
        <div class="form-check">
          <input
            class="form-check-input"
            type="radio"
            id="availability-outstock"
            value="outOfStock"
            v-model="activeFilters.availability"
          />
          <label class="form-check-label" for="availability-outstock"> Hết hàng </label>
        </div>
      </div>
    </div>

    <!-- Sort Options -->
    <div class="filter-section">
      <h6 class="filter-title"><i class="bi bi-sort-alpha-down me-2"></i>Sắp xếp</h6>
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
import { ref, computed, watch, onMounted } from 'vue'
import { useSearchStore } from '../../stores/search'

const searchStore = useSearchStore()

// Load sizes when component mounts
onMounted(() => {
  console.log('🔄 ProductFilters mounted, loading sizes...')
  searchStore.loadAvailableSizes()
})

// Local state
const priceRange = ref({ min: 0, max: 5000000 })

// Computed
const activeFilters = computed(() => searchStore.activeFilters)
const hasActiveFilters = computed(() => searchStore.hasActiveFilters)
const activeFiltersCount = computed(() => searchStore.activeFiltersCount)
const availableSizes = computed(() => searchStore.availableSizes)
const availableColors = computed(() => searchStore.availableColors)
const availableMaterials = computed(() => searchStore.availableMaterials)
const priceRanges = computed(() => searchStore.priceRanges)

// Methods
const formatPrice = (price) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    maximumFractionDigits: 0,
  }).format(price)
}

const formatInputPrice = (price) => {
  return price === 0 ? '' : price.toString()
}

const updateMinPrice = (event) => {
  const value = parseInt(event.target.value.replace(/[^\d]/g, '')) || 0
  priceRange.value.min = Math.max(0, Math.min(value, priceRange.value.max - 50000))
  searchStore.setPriceRange(priceRange.value.min, priceRange.value.max)
}

const updateMaxPrice = (event) => {
  const value = parseInt(event.target.value.replace(/[^\d]/g, '')) || 10000000
  priceRange.value.max = Math.min(10000000, Math.max(value, priceRange.value.min + 50000))
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

const toggleMaterial = (material) => {
  searchStore.toggleMaterial(material)
}

const clearFilter = (filterType) => {
  searchStore.clearFilter(filterType)
}

const clearAllFilters = () => {
  searchStore.clearAllFilters()
  priceRange.value = { min: 0, max: 5000000 }
}

// Function to determine text color based on background brightness
const getTextColor = (hexColor) => {
  // Remove # if present
  const hex = hexColor.replace('#', '')

  // Convert to RGB
  const r = parseInt(hex.substr(0, 2), 16)
  const g = parseInt(hex.substr(2, 2), 16)
  const b = parseInt(hex.substr(4, 2), 16)

  // Calculate brightness using luminance formula
  const brightness = (r * 299 + g * 587 + b * 114) / 1000

  // Return dark text for light backgrounds, white text for dark backgrounds
  return brightness > 128 ? '#333' : 'white'
}

// Watch for filter changes
watch(
  () => activeFilters.value.priceRange,
  (newRange) => {
    priceRange.value = { ...newRange }
  },
  { deep: true },
)
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
.price-inputs {
  margin-bottom: 8px;
}

.price-inputs input {
  border-radius: 8px;
  border: 1px solid #dee2e6;
  transition: border-color 0.3s ease;
}

.price-inputs input:focus {
  border-color: #d4af37;
  box-shadow: 0 0 0 0.2rem rgba(212, 175, 55, 0.25);
}

.price-display {
  text-align: center;
  padding: 8px 12px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.quick-range-btn {
  border-radius: 20px;
  padding: 6px 14px;
  font-size: 0.85rem;
  font-weight: 500;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.quick-range-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.quick-range-btn.active {
  background: #d4af37;
  border-color: #d4af37;
  color: #2c2c2c;
  font-weight: 600;
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
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 8px;
}

.color-btn {
  height: 40px;
  border-radius: 8px;
  border: 2px solid transparent;
  position: relative;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px 12px;
}

.color-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.color-btn.active {
  border-color: var(--auro-dark);
  box-shadow:
    0 0 0 2px white,
    0 0 0 4px var(--auro-accent);
}

.color-btn.active::after {
  content: '✓';
  position: absolute;
  top: 4px;
  right: 4px;
  color: white;
  font-weight: bold;
  font-size: 12px;
  text-shadow: 0 0 2px rgba(0, 0, 0, 0.5);
}

.color-name {
  font-weight: 600;
  font-size: 0.8rem;
  text-align: center;
  text-shadow: 0 0 2px rgba(0, 0, 0, 0.3);
}

/* Material List */
.material-list .form-check {
  padding: 8px 0;
}

.material-list .form-check-input {
  border-radius: 4px;
  border-color: #dee2e6;
}

.material-list .form-check-input:checked {
  background-color: var(--auro-accent);
  border-color: var(--auro-accent);
}

.material-list .form-check-label {
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
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
