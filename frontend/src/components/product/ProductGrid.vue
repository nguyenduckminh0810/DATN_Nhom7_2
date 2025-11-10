<template>
  <div class="product-grid">
    <!-- Grid Header -->
    <div class="grid-header d-flex justify-content-between align-items-center mb-4">
      <div class="results-info">
        <span class="fw-medium text-dark">{{ products.length }}</span>
        <span class="text-muted ms-1">sản phẩm</span>
        <span v-if="hasActiveFilters" class="text-warning ms-2">
          <i class="bi bi-funnel-fill me-1"></i>đã lọc
        </span>
      </div>

      <div class="view-controls d-flex align-items-center gap-3">
        <!-- Items per page -->
        <div class="d-flex align-items-center gap-2">
          <small class="text-muted">Hiển thị:</small>
          <select v-model="itemsPerPage" class="form-select form-select-sm" style="width: auto">
            <option value="12">12</option>
            <option value="24">24</option>
            <option value="48">48</option>
            <option value="96">96</option>
          </select>
        </div>

        <!-- View mode toggle -->
        <div class="btn-group btn-group-sm" role="group">
          <button
            @click="viewMode = 'grid'"
            :class="['btn', viewMode === 'grid' ? 'btn-warning' : 'btn-outline-secondary']"
            title="Xem dạng lưới"
          >
            <i class="bi bi-grid-3x3-gap"></i>
          </button>
          <button
            @click="viewMode = 'list'"
            :class="['btn', viewMode === 'list' ? 'btn-warning' : 'btn-outline-secondary']"
            title="Xem dạng danh sách"
          >
            <i class="bi bi-list"></i>
          </button>
        </div>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" :class="['row', viewMode === 'list' ? 'g-3' : 'g-4']">
      <div v-for="n in parseInt(itemsPerPage)" :key="`skeleton-${n}`" :class="getColumnClass">
        <ProductSkeleton :view-mode="viewMode" />
      </div>
    </div>

    <!-- Empty State -->
    <div v-else-if="products.length === 0" class="empty-state text-center py-5">
      <div class="empty-icon mb-3">
        <i class="bi bi-search fs-1 text-muted"></i>
      </div>
      <h4 class="text-muted mb-3">Không tìm thấy sản phẩm</h4>
      <p class="text-muted mb-4">Thử điều chỉnh bộ lọc hoặc từ khóa tìm kiếm</p>
      <button @click="clearFilters" class="btn btn-warning">
        <i class="bi bi-funnel me-2"></i>Xóa bộ lọc
      </button>
    </div>

    <!-- Products Grid -->
    <div v-else :class="['row', viewMode === 'list' ? 'g-3' : 'g-4']">
      <div v-for="product in paginatedProducts" :key="product.id" :class="getColumnClass">
        <ProductCard
          :id="product.id"
          :name="product.name || product.ten"
          :img="product.anhDaiDien"
          :price-now="product.price || product.gia"
          :price-old="product.originalPrice || product.giaGoc"
          :discount="product.discount || product.giamGia"
          :promotional-badge="product.promotionalBadge"
          :color-options="product.colorOptions || product.mauSac"
          :sizes="product.sizes || product.kichCo"
          :view-mode="viewMode"
          :loading="loading"
        />
      </div>
    </div>

    <!-- Pagination -->
    <div v-if="totalPages > 1" class="pagination-wrapper d-flex justify-content-center mt-5">
      <nav aria-label="Product pagination">
        <ul class="pagination pagination-lg">
          <!-- Previous -->
          <li class="page-item" :class="{ disabled: currentPage === 1 }">
            <button
              class="page-link"
              @click="goToPage(currentPage - 1)"
              :disabled="currentPage === 1"
            >
              <i class="bi bi-chevron-left"></i>
            </button>
          </li>

          <!-- Pages -->
          <li
            v-for="page in visiblePages"
            :key="page"
            class="page-item"
            :class="{ active: page === currentPage }"
          >
            <button class="page-link" @click="goToPage(page)">
              {{ page }}
            </button>
          </li>

          <!-- Next -->
          <li class="page-item" :class="{ disabled: currentPage === totalPages }">
            <button
              class="page-link"
              @click="goToPage(currentPage + 1)"
              :disabled="currentPage === totalPages"
            >
              <i class="bi bi-chevron-right"></i>
            </button>
          </li>
        </ul>
      </nav>
    </div>

    <!-- Results summary -->
    <div v-if="products.length > 0" class="results-summary text-center mt-4">
      <small class="text-muted">
        Hiển thị {{ startIndex + 1 }} - {{ endIndex }} trong {{ products.length }} sản phẩm
      </small>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useSearchStore } from '../../stores/search'
import ProductCard from './ProductCard.vue'
import ProductSkeleton from './ProductSkeleton.vue'

// Props
const props = defineProps({
  products: {
    type: Array,
    default: () => [],
  },
  loading: {
    type: Boolean,
    default: false,
  },
})

// Emits
const emit = defineEmits(['clear-filters'])

// Store
const searchStore = useSearchStore()

// Local state
const viewMode = ref('grid') // 'grid' or 'list'
const itemsPerPage = ref(12)
const currentPage = ref(1)

// Computed
const hasActiveFilters = computed(() => searchStore.hasActiveFilters)

const getColumnClass = computed(() => {
  if (viewMode.value === 'list') {
    return 'col-12'
  }
  return 'col-6 col-md-4 col-lg-3'
})

const totalPages = computed(() => {
  return Math.ceil(props.products.length / itemsPerPage.value)
})

const startIndex = computed(() => {
  return (currentPage.value - 1) * itemsPerPage.value
})

const endIndex = computed(() => {
  return Math.min(startIndex.value + itemsPerPage.value, props.products.length)
})

const paginatedProducts = computed(() => {
  return props.products.slice(startIndex.value, endIndex.value)
})

const visiblePages = computed(() => {
  const pages = []
  const totalPagesVal = totalPages.value
  const current = currentPage.value

  if (totalPagesVal <= 7) {
    for (let i = 1; i <= totalPagesVal; i++) {
      pages.push(i)
    }
  } else {
    if (current <= 4) {
      for (let i = 1; i <= 5; i++) {
        pages.push(i)
      }
      pages.push('...')
      pages.push(totalPagesVal)
    } else if (current >= totalPagesVal - 3) {
      pages.push(1)
      pages.push('...')
      for (let i = totalPagesVal - 4; i <= totalPagesVal; i++) {
        pages.push(i)
      }
    } else {
      pages.push(1)
      pages.push('...')
      for (let i = current - 1; i <= current + 1; i++) {
        pages.push(i)
      }
      pages.push('...')
      pages.push(totalPagesVal)
    }
  }

  return pages
})

// Methods
const goToPage = (page) => {
  if (page >= 1 && page <= totalPages.value && page !== currentPage.value) {
    currentPage.value = page
    // Scroll to top
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

const clearFilters = () => {
  emit('clear-filters')
  searchStore.clearAllFilters()
}

// Watch for changes that should reset pagination
watch([() => props.products.length, itemsPerPage], () => {
  currentPage.value = 1
})

watch(hasActiveFilters, (newValue) => {
  if (newValue) {
    currentPage.value = 1
  }
})
</script>

<style scoped>
.product-grid {
  min-height: 500px;
}

.grid-header {
  background: white;
  padding: 16px 20px;
  border-radius: 12px;
  border: 1px solid #f0f0f0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.results-info {
  display: flex;
  align-items: center;
  font-size: 0.95rem;
}

.view-controls .form-select {
  border-radius: 8px;
  border: 1px solid #dee2e6;
  font-size: 0.85rem;
}

.view-controls .btn-group .btn {
  border-radius: 6px;
  padding: 8px 12px;
  border-color: #dee2e6;
}

.view-controls .btn-group .btn:first-child {
  border-top-right-radius: 0;
  border-bottom-right-radius: 0;
}

.view-controls .btn-group .btn:last-child {
  border-top-left-radius: 0;
  border-bottom-left-radius: 0;
  border-left: 0;
}

.empty-state {
  background: white;
  border-radius: 16px;
  padding: 60px 40px;
  border: 2px dashed #e9ecef;
}

.empty-icon i {
  font-size: 4rem !important;
}

.pagination-wrapper {
  margin-top: 40px;
}

.pagination {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.page-link {
  border: none;
  background: white;
  color: #666;
  padding: 12px 16px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.page-link:hover {
  background: #f8f9fa;
  color: #d4af37;
}

.page-item.active .page-link {
  background: #d4af37;
  color: #2c2c2c;
  font-weight: 600;
}

.page-item.disabled .page-link {
  background: #f8f9fa;
  color: #ccc;
}

.results-summary {
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

/* Responsive */
@media (max-width: 768px) {
  .grid-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start !important;
  }

  .view-controls {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start !important;
    width: 100%;
  }

  .empty-state {
    padding: 40px 20px;
  }

  .pagination {
    font-size: 0.85rem;
  }

  .page-link {
    padding: 8px 12px;
  }
}
</style>
