<template>
  <div class="category">
    <div class="container py-4">
      <!-- Breadcrumb -->
      <nav aria-label="breadcrumb">
        <ol class="breadcrumb">
          <li class="breadcrumb-item">
            <router-link to="/">Trang chủ</router-link>
          </li>
          <li class="breadcrumb-item active" aria-current="page">
            {{ categoryName || 'Danh mục' }}
          </li>
        </ol>
      </nav>

      <!-- Category Header -->
      <div class="row mb-4">
        <div class="col-12">
          <h1 class="h2 fw-bold">{{ categoryName || 'Tất cả sản phẩm' }}</h1>
          <p class="text-muted">{{ products.length }} sản phẩm</p>
        </div>
      </div>

      <div class="row">
        <!-- Sidebar Filters -->
        <div class="col-lg-3 mb-4">
          <div class="card">
            <div class="card-header">
              <h5 class="mb-0">Bộ lọc</h5>
            </div>
            <div class="card-body">
              <!-- Price Filter -->
              <div class="mb-4">
                <h6>Giá</h6>
                <div class="form-check">
                  <input class="form-check-input" type="checkbox" id="price1" v-model="filters.price" value="0-500000">
                  <label class="form-check-label" for="price1">
                    Dưới 500.000đ
                  </label>
                </div>
                <div class="form-check">
                  <input class="form-check-input" type="checkbox" id="price2" v-model="filters.price" value="500000-1000000">
                  <label class="form-check-label" for="price2">
                    500.000đ - 1.000.000đ
                  </label>
                </div>
                <div class="form-check">
                  <input class="form-check-input" type="checkbox" id="price3" v-model="filters.price" value="1000000-2000000">
                  <label class="form-check-label" for="price3">
                    1.000.000đ - 2.000.000đ
                  </label>
                </div>
                <div class="form-check">
                  <input class="form-check-input" type="checkbox" id="price4" v-model="filters.price" value="2000000+">
                  <label class="form-check-label" for="price4">
                    Trên 2.000.000đ
                  </label>
                </div>
              </div>

              <!-- Size Filter -->
              <div class="mb-4">
                <h6>Kích thước</h6>
                <div class="form-check" v-for="size in sizes" :key="size">
                  <input class="form-check-input" type="checkbox" :id="'size' + size" v-model="filters.sizes" :value="size">
                  <label class="form-check-label" :for="'size' + size">
                    {{ size }}
                  </label>
                </div>
              </div>

              <!-- Color Filter -->
              <div class="mb-4">
                <h6>Màu sắc</h6>
                <div class="form-check" v-for="color in colors" :key="color">
                  <input class="form-check-input" type="checkbox" :id="'color' + color" v-model="filters.colors" :value="color">
                  <label class="form-check-label" :for="'color' + color">
                    {{ color }}
                  </label>
                </div>
              </div>

              <button @click="clearFilters" class="btn btn-outline-secondary btn-sm">
                Xóa bộ lọc
              </button>
            </div>
          </div>
        </div>

        <!-- Products Grid -->
        <div class="col-lg-9">
          <!-- Sort and View Options -->
          <div class="d-flex justify-content-between align-items-center mb-4">
            <div class="d-flex align-items-center gap-3">
              <span class="text-muted">Sắp xếp theo:</span>
              <select v-model="sortBy" class="form-select form-select-sm" style="width: auto;">
                <option value="name">Tên A-Z</option>
                <option value="price-low">Giá thấp đến cao</option>
                <option value="price-high">Giá cao đến thấp</option>
                <option value="newest">Mới nhất</option>
              </select>
            </div>
            <div class="d-flex align-items-center gap-2">
              <button @click="viewMode = 'grid'" :class="['btn btn-sm', viewMode === 'grid' ? 'btn-primary' : 'btn-outline-secondary']">
                <i class="bi bi-grid-3x3-gap"></i>
              </button>
              <button @click="viewMode = 'list'" :class="['btn btn-sm', viewMode === 'list' ? 'btn-primary' : 'btn-outline-secondary']">
                <i class="bi bi-list"></i>
              </button>
            </div>
          </div>

          <!-- Products -->
          <div v-if="filteredProducts.length > 0" :class="['row', viewMode === 'list' ? 'g-3' : 'g-4']">
            <div v-for="product in filteredProducts" :key="product.id" 
                 :class="viewMode === 'list' ? 'col-12' : 'col-md-6 col-lg-4'">
              <div class="card product-card h-100">
                <img :src="product.image || 'https://via.placeholder.com/300x300'" 
                     class="card-img-top" :alt="product.name">
                <div class="card-body d-flex flex-column">
                  <h6 class="card-title">{{ product.name }}</h6>
                  <p class="card-text text-muted small flex-grow-1">{{ product.description }}</p>
                  <div class="d-flex justify-content-between align-items-center">
                    <span class="price">{{ formatPrice(product.price) }}</span>
                    <button class="btn btn-sm btn-warning" @click="addToCart(product)">
                      <i class="bi bi-cart-plus"></i>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- No Products -->
          <div v-else class="text-center py-5">
            <i class="bi bi-search display-1 text-muted"></i>
            <h5 class="mt-3">Không tìm thấy sản phẩm</h5>
            <p class="text-muted">Hãy thử điều chỉnh bộ lọc của bạn</p>
          </div>

          <!-- Pagination -->
          <nav v-if="totalPages > 1" class="mt-4">
            <ul class="pagination justify-content-center">
              <li class="page-item" :class="{ disabled: currentPage === 1 }">
                <button class="page-link" @click="changePage(currentPage - 1)">Trước</button>
              </li>
              <li v-for="page in visiblePages" :key="page" class="page-item" :class="{ active: page === currentPage }">
                <button class="page-link" @click="changePage(page)">{{ page }}</button>
              </li>
              <li class="page-item" :class="{ disabled: currentPage === totalPages }">
                <button class="page-link" @click="changePage(currentPage + 1)">Sau</button>
              </li>
            </ul>
          </nav>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useCartStore } from '../stores/cart'

const route = useRoute()
const cartStore = useCartStore()

// Reactive data
const products = ref([])
const categoryName = ref('')
const viewMode = ref('grid')
const sortBy = ref('name')
const currentPage = ref(1)
const itemsPerPage = 12

const filters = ref({
  price: [],
  sizes: [],
  colors: []
})

const sizes = ref(['S', 'M', 'L', 'XL', 'XXL'])
const colors = ref(['Đen', 'Trắng', 'Xám', 'Xanh navy', 'Nâu'])

// Mock data
const mockProducts = [
  {
    id: 1,
    name: 'Áo sơ mi nam cao cấp',
    description: 'Áo sơ mi nam chất liệu cotton 100%',
    price: 450000,
    image: 'https://via.placeholder.com/300x300/6c757d/ffffff?text=Áo+sơ+mi',
    category: 'ao',
    sizes: ['S', 'M', 'L', 'XL'],
    colors: ['Trắng', 'Xanh navy']
  },
  {
    id: 2,
    name: 'Quần âu nam',
    description: 'Quần âu nam thiết kế hiện đại',
    price: 650000,
    image: 'https://via.placeholder.com/300x300/6c757d/ffffff?text=Quần+âu',
    category: 'quan',
    sizes: ['M', 'L', 'XL'],
    colors: ['Đen', 'Xám']
  },
  {
    id: 3,
    name: 'Áo khoác nam',
    description: 'Áo khoác nam phong cách casual',
    price: 850000,
    image: 'https://via.placeholder.com/300x300/6c757d/ffffff?text=Áo+khoác',
    category: 'ao',
    sizes: ['M', 'L', 'XL', 'XXL'],
    colors: ['Đen', 'Xám', 'Nâu']
  }
]

// Computed properties
const filteredProducts = computed(() => {
  let filtered = [...products.value]

  // Apply filters
  if (filters.value.price.length > 0) {
    filtered = filtered.filter(product => {
      return filters.value.price.some(priceRange => {
        const [min, max] = priceRange.split('-').map(p => p === '2000000+' ? Infinity : parseInt(p))
        return product.price >= min && (max === undefined || product.price <= max)
      })
    })
  }

  if (filters.value.sizes.length > 0) {
    filtered = filtered.filter(product => {
      return filters.value.sizes.some(size => product.sizes?.includes(size))
    })
  }

  if (filters.value.colors.length > 0) {
    filtered = filtered.filter(product => {
      return filters.value.colors.some(color => product.colors?.includes(color))
    })
  }

  // Apply sorting
  filtered.sort((a, b) => {
    switch (sortBy.value) {
      case 'name':
        return a.name.localeCompare(b.name)
      case 'price-low':
        return a.price - b.price
      case 'price-high':
        return b.price - a.price
      case 'newest':
        return b.id - a.id
      default:
        return 0
    }
  })

  return filtered
})

const totalPages = computed(() => {
  return Math.ceil(filteredProducts.value.length / itemsPerPage)
})

const visiblePages = computed(() => {
  const pages = []
  const start = Math.max(1, currentPage.value - 2)
  const end = Math.min(totalPages.value, start + 4)
  
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  
  return pages
})

// Methods
const formatPrice = (price) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

const addToCart = (product) => {
  cartStore.addItem(product)
}

const clearFilters = () => {
  filters.value = {
    price: [],
    sizes: [],
    colors: []
  }
}

const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

// Watch for route changes
watch(() => route.params.slug, (newSlug) => {
  if (newSlug) {
    // Load category data based on slug
    categoryName.value = newSlug === 'ao' ? 'Áo' : 
                        newSlug === 'quan' ? 'Quần' : 
                        newSlug === 'phu-kien' ? 'Phụ kiện' : 'Danh mục'
    
    // Filter products by category
    products.value = mockProducts.filter(p => p.category === newSlug)
  } else {
    // Show all products
    categoryName.value = 'Tất cả sản phẩm'
    products.value = mockProducts
  }
}, { immediate: true })

onMounted(() => {
  // Load products based on route
  const slug = route.params.slug
  if (slug) {
    categoryName.value = slug === 'ao' ? 'Áo' : 
                        slug === 'quan' ? 'Quần' : 
                        slug === 'phu-kien' ? 'Phụ kiện' : 'Danh mục'
    products.value = mockProducts.filter(p => p.category === slug)
  } else {
    categoryName.value = 'Tất cả sản phẩm'
    products.value = mockProducts
  }
})
</script>

<style scoped>
.product-card {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  border: none;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 1rem 3rem rgba(0, 0, 0, 0.175);
}

.product-card .card-img-top {
  transition: transform 0.3s ease;
  height: 250px;
  object-fit: cover;
}

.product-card:hover .card-img-top {
  transform: scale(1.05);
}

.price {
  font-size: 1.25rem;
  font-weight: 700;
  color: #ffc107;
}

.breadcrumb {
  background: none;
  padding: 0;
}

.breadcrumb-item a {
  text-decoration: none;
  color: #6c757d;
}

.breadcrumb-item a:hover {
  color: #ffc107;
}
</style>
