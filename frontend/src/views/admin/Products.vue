<template>
  <div class="admin-products">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">Quản lý sản phẩm</h1>
        <p class="page-subtitle">Quản lý danh sách sản phẩm của cửa hàng</p>
      </div>
      <div class="header-right">
        <button class="btn btn-auro-primary" @click="showAddModal = true">
          <i class="ph-plus me-2"></i>Thêm sản phẩm
        </button>
      </div>
    </div>

    <!-- Advanced Filters and Search -->
    <div class="filters-section">
      <!-- Quick Search -->
      <div class="search-row">
        <div class="search-box">
          <i class="ph-magnifying-glass search-icon"></i>
          <input
            type="text"
            class="form-control search-input"
            placeholder="Tìm kiếm sản phẩm, SKU, mô tả..."
            v-model="searchQuery"
          />
        </div>
        <button class="btn btn-outline-primary" @click="toggleAdvancedFilters">
          <i class="ph-funnel me-1"></i>Bộ lọc nâng cao
          <i :class="showAdvancedFilters ? 'ph-caret-up' : 'ph-caret-down'" class="ms-1"></i>
        </button>
        <button class="btn btn-outline-secondary" @click="clearFilters">
          <i class="ph-arrow-clockwise me-1"></i>Xóa bộ lọc
        </button>
      </div>

      <!-- Advanced Filters -->
      <div v-if="showAdvancedFilters" class="advanced-filters">
        <div class="row g-3">
          <div class="col-md-3">
            <label class="form-label">Danh mục</label>
            <select class="form-select" v-model.number="selectedCategory">
              <option value="">Tất cả danh mục</option>
              <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.ten }}</option>
            </select>
          </div>
          <div class="col-md-3">
            <label class="form-label">Trạng thái</label>
            <select class="form-select" v-model="selectedStatus">
              <option value="">Tất cả trạng thái</option>
              <option value="active">Đang bán</option>
              <option value="inactive">Ngừng bán</option>
              <option value="out-of-stock">Hết hàng</option>
            </select>
          </div>
          <div class="col-md-3">
            <label class="form-label">Khoảng giá</label>
            <div class="price-range">
              <input
                type="number"
                class="form-control"
                placeholder="Từ"
                v-model.number="priceRange.min"
              />
              <span class="range-separator">-</span>
              <input
                type="number"
                class="form-control"
                placeholder="Đến"
                v-model.number="priceRange.max"
              />
            </div>
          </div>
          <div class="col-md-3">
            <label class="form-label">Tồn kho</label>
            <select class="form-select" v-model="stockFilter">
              <option value="">Tất cả</option>
              <option value="in-stock">Còn hàng</option>
              <option value="low-stock">Sắp hết hàng (&lt; 10)</option>
              <option value="out-of-stock">Hết hàng</option>
            </select>
          </div>
          <div class="col-md-3">
            <label class="form-label">Ngày tạo</label>
            <input type="date" class="form-control" v-model="createdDate" />
          </div>
          <div class="col-md-3">
            <label class="form-label">Sắp xếp</label>
            <select class="form-select" v-model="sortBy">
              <option value="newest">Mới nhất</option>
              <option value="oldest">Cũ nhất</option>
              <option value="name-asc">Tên A-Z</option>
              <option value="name-desc">Tên Z-A</option>
              <option value="price-low">Giá thấp nhất</option>
              <option value="price-high">Giá cao nhất</option>
              <option value="stock-low">Tồn kho ít nhất</option>
              <option value="stock-high">Tồn kho nhiều nhất</option>
            </select>
          </div>
          <div class="col-md-6">
            <label class="form-label">Thẻ</label>
            <div class="tag-filters">
              <span
                v-for="tag in availableTags"
                :key="tag"
                :class="['tag-filter', { active: selectedTags.includes(tag) }]"
                @click="toggleTag(tag)"
              >
                {{ tag }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Products Table -->
    <div class="products-table">
      <!-- Table Actions -->
      <div class="table-actions">
        <div class="view-options">
          <span class="view-label">Hiển thị:</span>
          <button
            :class="['view-btn', { active: viewMode === 'table' }]"
            @click="viewMode = 'table'"
            title="Dạng bảng"
          >
            <i class="ph-table"></i>
          </button>
          <button
            :class="['view-btn', { active: viewMode === 'grid' }]"
            @click="viewMode = 'grid'"
            title="Dạng lưới"
          >
            <i class="ph-grid-four"></i>
          </button>
        </div>
        <div class="table-stats">
          <span class="stats-text">
            Hiển thị {{ filteredProducts.length }} / {{ products.length }} sản phẩm
          </span>
        </div>
      </div>

      <!-- Table View -->
      <div v-if="viewMode === 'table'" class="table-responsive">
        <table class="table table-hover">
          <thead>
            <tr>
              <th>
                <input
                  type="checkbox"
                  class="form-check-input"
                  v-model="selectAll"
                  @change="toggleSelectAll"
                />
              </th>
              <th>
                <button class="sort-btn" @click="sortTable('name')">
                  Sản phẩm
                  <i :class="getSortIcon('name')"></i>
                </button>
              </th>
              <th>Danh mục</th>
              <th>
                <button class="sort-btn" @click="sortTable('price')">
                  Giá
                  <i :class="getSortIcon('price')"></i>
                </button>
              </th>
              <th>
                <button class="sort-btn" @click="sortTable('stock')">
                  Tồn kho
                  <i :class="getSortIcon('stock')"></i>
                </button>
              </th>
              <th>Đánh giá</th>
              <th>Trạng thái</th>
              <th>
                <button class="sort-btn" @click="sortTable('createdAt')">
                  Ngày tạo
                  <i :class="getSortIcon('createdAt')"></i>
                </button>
              </th>
              <th>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="product in filteredProducts" :key="product.id">
              <td>
                <input
                  type="checkbox"
                  class="form-check-input"
                  v-model="selectedProducts"
                  :value="product.id"
                />
              </td>
              <td>
                <div class="product-info">
                  <div class="product-image">
                    <img :src="product.image" :alt="product.name" class="img-fluid" />
                    <div v-if="product.isNew" class="new-badge">Mới</div>
                    <div v-if="product.isFeatured" class="featured-badge">Nổi bật</div>
                  </div>
                  <div class="product-details">
                    <div class="product-name">{{ product.name }}</div>
                    <div class="product-sku">SKU: {{ product.sku }}</div>
                    <div v-if="product.tags && product.tags.length" class="product-tags">
                      <span v-for="tag in product.tags.slice(0, 2)" :key="tag" class="tag">{{
                        tag
                      }}</span>
                      <span v-if="product.tags.length > 2" class="tag-more"
                        >+{{ product.tags.length - 2 }}</span
                      >
                    </div>
                  </div>
                </div>
              </td>
              <td>
                <span class="badge bg-light text-dark">{{
                  getCategoryName(product.category)
                }}</span>
              </td>
              <td>
                <div class="price-info">
                  <div class="current-price">{{ formatCurrency(product.price) }}</div>
                  <div v-if="product.originalPrice" class="original-price">
                    {{ formatCurrency(product.originalPrice) }}
                  </div>
                  <div v-if="product.originalPrice" class="discount-percent">
                    -{{ Math.round((1 - product.price / product.originalPrice) * 100) }}%
                  </div>
                </div>
              </td>
              <td>
                <div class="stock-info">
                  <span :class="['stock-badge', getStockClass(product.stock)]">
                    {{ product.stock }}
                  </span>
                  <div v-if="product.stock < 10 && product.stock > 0" class="low-stock-warning">
                    <i class="ph-warning"></i> Sắp hết
                  </div>
                </div>
              </td>
              <td>
                <div class="rating-info">
                  <div class="rating-stars">
                    <i
                      v-for="i in 5"
                      :key="i"
                      :class="['star', { filled: i <= product.rating }]"
                    ></i>
                  </div>
                  <div class="rating-text">
                    {{ product.rating }}/5 ({{ product.reviewCount }} đánh giá)
                  </div>
                </div>
              </td>
              <td>
                <span :class="['status-badge', getStatusClass(product.status)]">
                  {{ getStatusText(product.status) }}
                </span>
              </td>
              <td>
                <div class="date-info">
                  <div>{{ formatDate(product.createdAt) }}</div>
                  <div class="date-time">{{ formatTime(product.createdAt) }}</div>
                </div>
              </td>
              <td>
                <div class="action-buttons">
                  <button
                    class="btn btn-sm btn-outline-primary"
                    @click="editProduct(product)"
                    title="Chỉnh sửa"
                  >
                    <i class="ph-pencil"></i>
                  </button>
                  <button
                    class="btn btn-sm btn-outline-info"
                    @click="viewProduct(product)"
                    title="Xem chi tiết"
                  >
                    <i class="ph-eye"></i>
                  </button>
                  <button
                    class="btn btn-sm btn-outline-success"
                    @click="duplicateProduct(product)"
                    title="Nhân bản"
                  >
                    <i class="ph-copy"></i>
                  </button>
                  <button
                    class="btn btn-sm btn-outline-warning"
                    @click="toggleFeatured(product)"
                    title="Đánh dấu nổi bật"
                  >
                    <i :class="product.isFeatured ? 'ph-star-fill' : 'ph-star'"></i>
                  </button>
                  <button
                    class="btn btn-sm btn-outline-danger"
                    @click="deleteProduct(product)"
                    title="Xóa"
                  >
                    <i class="ph-trash"></i>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Grid View -->
      <div v-if="viewMode === 'grid'" class="products-grid">
        <div class="row g-4">
          <div
            v-for="product in filteredProducts"
            :key="product.id"
            class="col-lg-3 col-md-4 col-sm-6"
          >
            <div class="product-card">
              <div class="product-card-header">
                <input
                  type="checkbox"
                  class="form-check-input"
                  v-model="selectedProducts"
                  :value="product.id"
                />
                <div class="product-badges">
                  <span v-if="product.isNew" class="badge bg-success">Mới</span>
                  <span v-if="product.isFeatured" class="badge bg-warning">Nổi bật</span>
                </div>
              </div>
              <div class="product-image-container">
                <img :src="product.image" :alt="product.name" class="product-image" />
                <div class="product-overlay">
                  <button
                    class="btn btn-sm btn-light"
                    @click="viewProduct(product)"
                    title="Xem chi tiết"
                  >
                    <i class="ph-eye"></i>
                  </button>
                  <button
                    class="btn btn-sm btn-primary"
                    @click="editProduct(product)"
                    title="Chỉnh sửa"
                  >
                    <i class="ph-pencil"></i>
                  </button>
                </div>
              </div>
              <div class="product-card-body">
                <h6 class="product-title">{{ product.name }}</h6>
                <div class="product-meta">
                  <span class="product-sku">SKU: {{ product.sku }}</span>
                  <span class="product-category">{{ getCategoryName(product.category) }}</span>
                </div>
                <div class="product-rating">
                  <div class="rating-stars">
                    <i
                      v-for="i in 5"
                      :key="i"
                      :class="['star', { filled: i <= product.rating }]"
                    ></i>
                  </div>
                  <span class="rating-count">({{ product.reviewCount }})</span>
                </div>
                <div class="product-price">
                  <span class="current-price">{{ formatCurrency(product.price) }}</span>
                  <span v-if="product.originalPrice" class="original-price">{{
                    formatCurrency(product.originalPrice)
                  }}</span>
                </div>
                <div class="product-stock">
                  <span :class="['stock-badge', getStockClass(product.stock)]">
                    {{ product.stock }} sản phẩm
                  </span>
                  <span :class="['status-badge', getStatusClass(product.status)]">
                    {{ getStatusText(product.status) }}
                  </span>
                </div>
              </div>
              <div class="product-card-footer">
                <div class="product-actions">
                  <button class="btn btn-sm btn-outline-primary" @click="editProduct(product)">
                    <i class="ph-pencil"></i>
                  </button>
                  <button class="btn btn-sm btn-outline-success" @click="duplicateProduct(product)">
                    <i class="ph-copy"></i>
                  </button>
                  <button class="btn btn-sm btn-outline-warning" @click="toggleFeatured(product)">
                    <i :class="product.isFeatured ? 'ph-star-fill' : 'ph-star'"></i>
                  </button>
                  <button class="btn btn-sm btn-outline-danger" @click="deleteProduct(product)">
                    <i class="ph-trash"></i>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Pagination -->
    <div class="pagination-section">
      <div class="row align-items-center">
        <div class="col-md-6">
          <div class="pagination-info">
            Hiển thị {{ (currentPage - 1) * itemsPerPage + 1 }} -
            {{ Math.min(currentPage * itemsPerPage, totalItems) }} trong tổng số
            {{ totalItems }} sản phẩm
          </div>
        </div>
        <div class="col-md-6">
          <nav>
            <ul class="pagination justify-content-end">
              <li class="page-item" :class="{ disabled: currentPage === 1 }">
                <a class="page-link" href="#" @click.prevent="changePage(currentPage - 1)">Trước</a>
              </li>
              <li
                class="page-item"
                v-for="page in visiblePages"
                :key="page"
                :class="{ active: currentPage === page }"
              >
                <a class="page-link" href="#" @click.prevent="changePage(page)">{{ page }}</a>
              </li>
              <li class="page-item" :class="{ disabled: currentPage === totalPages }">
                <a class="page-link" href="#" @click.prevent="changePage(currentPage + 1)">Sau</a>
              </li>
            </ul>
          </nav>
        </div>
      </div>
    </div>

    <!-- Bulk Actions -->
    <div v-if="selectedProducts.length > 0" class="bulk-actions">
      <div class="bulk-actions-content">
        <span class="selected-count">{{ selectedProducts.length }} sản phẩm đã chọn</span>
        <div class="bulk-buttons">
          <button class="btn btn-sm btn-outline-success" @click="bulkUpdateStatus('active')">
            <i class="ph-check me-1"></i>Kích hoạt
          </button>
          <button class="btn btn-sm btn-outline-warning" @click="bulkUpdateStatus('inactive')">
            <i class="ph-pause me-1"></i>Ngừng bán
          </button>
          <button class="btn btn-sm btn-outline-danger" @click="bulkDelete">
            <i class="ph-trash me-1"></i>Xóa
          </button>
        </div>
      </div>
    </div>

    <!-- Add/Edit Product Modal -->
    <div v-if="showAddModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h5 class="modal-title">
            {{ editingProduct ? 'Chỉnh sửa sản phẩm' : 'Thêm sản phẩm mới' }}
          </h5>
          <button class="btn-close" @click="closeModal">
            <i class="ph-x"></i>
          </button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="saveProduct">
            <div class="row g-3">
              <div class="col-md-8">
                <label class="form-label">Tên sản phẩm *</label>
                <input type="text" class="form-control" v-model="productForm.name" required />
              </div>
              <div class="col-md-4">
                <label class="form-label">SKU *</label>
                <input type="text" class="form-control" v-model="productForm.sku" required />
              </div>
              <div class="col-md-6">
                <label class="form-label">Danh mục *</label>
                <select class="form-select" v-model.number="productForm.categoryId" required>
                  <option value="">Chọn danh mục</option>
                  <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.ten }}</option>
                </select>
              </div>
              <div class="col-md-6">
                <label class="form-label">Trạng thái</label>
                <select class="form-select" v-model="productForm.status">
                  <option value="active">Đang bán</option>
                  <option value="inactive">Ngừng bán</option>
                  <option value="out-of-stock">Hết hàng</option>
                </select>
              </div>
              <div class="col-md-4">
                <label class="form-label">Giá bán *</label>
                <input
                  type="number"
                  class="form-control"
                  v-model.number="productForm.price"
                  required
                />
              </div>
              <div class="col-md-4">
                <label class="form-label">Giá gốc</label>
                <input
                  type="number"
                  class="form-control"
                  v-model.number="productForm.originalPrice"
                />
              </div>
              <div class="col-md-4">
                <label class="form-label">Số lượng tồn kho</label>
                <input type="number" class="form-control" v-model.number="productForm.stock" />
              </div>
              <div class="col-12">
                <label class="form-label">Mô tả</label>
                <textarea
                  class="form-control"
                  rows="3"
                  v-model="productForm.description"
                ></textarea>
              </div>
              <div class="col-12">
                <label class="form-label">Hình ảnh</label>
                <input
                  type="url"
                  class="form-control"
                  v-model="productForm.image"
                  placeholder="URL hình ảnh"
                />
              </div>
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" @click="closeModal">Hủy</button>
          <button type="button" class="btn btn-auro-primary" @click="saveProduct">
            {{ editingProduct ? 'Cập nhật' : 'Thêm sản phẩm' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import sanPhamService from '../../services/sanPhamService'

// Reactive data
const searchQuery = ref('')
const selectedCategory = ref('')
const selectedStatus = ref('')
const selectedProducts = ref([])
const selectAll = ref(false)
const currentPage = ref(0) // backend uses 0-based pages
const itemsPerPage = 10
const showAddModal = ref(false)
const editingProduct = ref(null)
const isLoading = ref(false)

// Advanced filters
const showAdvancedFilters = ref(false)
const priceRange = ref({ min: null, max: null })
const stockFilter = ref('')
const createdDate = ref('')
const sortBy = ref('newest')
const selectedTags = ref([])
const viewMode = ref('table') // 'table' or 'grid'
const tableSort = ref({ field: '', direction: 'asc' })

// Available tags
const availableTags = ref(['Bestseller', 'Sale', 'New', 'Premium', 'Limited'])

// Categories fetched from backend
const categories = ref([])

// Product form (keeps existing UI field names)
const productForm = ref({
  name: '',
  sku: '',
  categoryId: '',
  status: 'active',
  price: 0,
  originalPrice: 0,
  stock: 0,
  description: '',
  image: '',
})

// Products loaded from backend
const products = ref([])
const totalItems = ref(0)
const totalPages = ref(1)

function mapFromApi(sp) {
  return {
    id: sp.id,
    name: sp.ten,
    sku: sp.sku || '',
    categoryId: sp.danhMucId || '',
    price: Number(sp.gia) || 0,
    originalPrice: null,
    stock: 0,
    status: sp.trangThai || 'active',
    description: sp.moTa,
    image: sp.hinhAnhUrl || '',
    createdAt: sp.taoLuc ? new Date(sp.taoLuc) : new Date(),
  }
}

function mapToApi(form) {
  return {
    ten: form.name,
    moTa: form.description,
    danhMucId: form.categoryId || null,
    gia: form.price != null ? Number(form.price) : null,
    trangThai: form.status,
  }
}

const loadProducts = async (pageNumber = 0) => {
  isLoading.value = true
  try {
    const params = {
      page: pageNumber,
      size: itemsPerPage,
      search: searchQuery.value || undefined,
      danhMucId: selectedCategory.value || undefined,
    }
    const res = await sanPhamService.page(params)
    // res is expected to be a Page<SanPhamResponse>
    products.value = (res.content || []).map(mapFromApi)
    totalItems.value = res.totalElements || (res.content || []).length
    totalPages.value = res.totalPages || 1
    // controller returns 0-based page number; expose 1-based for UI
    currentPage.value = typeof res.number === 'number' ? res.number + 1 : pageNumber + 1
  } catch (e) {
    console.error('Load products error', e)
    alert('Lỗi khi tải sản phẩm: ' + (e?.message || JSON.stringify(e)))
  } finally {
    isLoading.value = false
  }
}

const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    loadProducts(page - 1)
  }
}

const editProduct = (product) => {
  editingProduct.value = product
  productForm.value = { ...product }
  showAddModal.value = true
}

const viewProduct = (product) => {
  // simple detail view - open product page in new tab (if exists)
  const slug = product.name ? product.name.toLowerCase().replace(/[^a-z0-9]+/g, '-') : product.id
  window.open(`/product/${slug}`, '_blank')
}

const closeModal = () => {
  showAddModal.value = false
  editingProduct.value = null
  productForm.value = {
    name: '',
    sku: '',
    categoryId: '',
    status: 'active',
    price: 0,
    originalPrice: 0,
    stock: 0,
    description: '',
    image: '',
  }
}

const saveProduct = async () => {
  const payload = mapToApi(productForm.value)
  try {
    if (editingProduct.value) {
      await sanPhamService.update(editingProduct.value.id, payload)
    } else {
      await sanPhamService.create(payload)
    }
    await loadProducts(currentPage.value)
    closeModal()
  } catch (e) {
    console.error('Save product error', e)
    alert('Lưu sản phẩm lỗi: ' + (e?.message || JSON.stringify(e)))
  }
}

const deleteProduct = async (product) => {
  if (!confirm(`Bạn có chắc chắn muốn xóa sản phẩm "${product.name}"?`)) return
  try {
    await sanPhamService.delete(product.id)
    await loadProducts(currentPage.value)
  } catch (e) {
    console.error('Delete product error', e)
    alert('Xóa sản phẩm lỗi: ' + (e?.message || JSON.stringify(e)))
  }
}

const duplicateProduct = (product) => {
  // For now duplicating locally; you may want to implement server-side clone
  const duplicatedProduct = {
    ...product,
    id: Date.now(),
    name: product.name + ' (Copy)',
    sku: product.sku + '-COPY',
    createdAt: new Date(),
    isNew: true,
    isFeatured: false,
  }
  products.value.unshift(duplicatedProduct)
}

const toggleFeatured = (product) => {
  product.isFeatured = !product.isFeatured
}

const toggleSelectAll = () => {
  if (selectAll.value) {
    selectedProducts.value = products.value.map((p) => p.id)
  } else {
    selectedProducts.value = []
  }
}

const bulkUpdateStatus = (status) => {
  if (
    confirm(
      `Bạn có chắc chắn muốn cập nhật trạng thái cho ${selectedProducts.value.length} sản phẩm?`,
    )
  ) {
    // For simplicity update locally; implement batch API if desired
    selectedProducts.value.forEach((productId) => {
      const product = products.value.find((p) => p.id === productId)
      if (product) product.status = status
    })
    selectedProducts.value = []
    selectAll.value = false
  }
}

const bulkDelete = () => {
  if (confirm(`Bạn có chắc chắn muốn xóa ${selectedProducts.value.length} sản phẩm?`)) {
    // For now delete locally; implement batch delete API if desired
    selectedProducts.value.forEach((productId) => {
      const idx = products.value.findIndex((p) => p.id === productId)
      if (idx > -1) products.value.splice(idx, 1)
    })
    selectedProducts.value = []
    selectAll.value = false
  }
}

// Computed and helper functions reused from the original file
const filteredProducts = computed(() => {
  let filtered = products.value

  // Search filter
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(
      (product) =>
        (product.name || '').toLowerCase().includes(query) ||
        (product.sku || '').toLowerCase().includes(query) ||
        (product.description || '').toLowerCase().includes(query),
    )
  }

  // Category filter
  if (selectedCategory.value) {
    filtered = filtered.filter(
      (product) => Number(product.categoryId) === Number(selectedCategory.value),
    )
  }

  // Status filter
  if (selectedStatus.value) {
    filtered = filtered.filter((product) => product.status === selectedStatus.value)
  }

  // Sorting (basic)
  filtered.sort((a, b) => {
    switch (sortBy.value) {
      case 'oldest':
        return new Date(a.createdAt) - new Date(b.createdAt)
      case 'name-asc':
        return a.name.localeCompare(b.name)
      case 'name-desc':
        return b.name.localeCompare(a.name)
      case 'price-low':
        return a.price - b.price
      case 'price-high':
        return b.price - a.price
      case 'stock-low':
        return a.stock - b.stock
      case 'stock-high':
        return b.stock - a.stock
      case 'newest':
      default:
        return new Date(b.createdAt) - new Date(a.createdAt)
    }
  })

  return filtered
})

const totalItemsComputed = computed(() => filteredProducts.value.length)
const totalPagesComputed = computed(() => Math.ceil(totalItemsComputed.value / itemsPerPage))

const visiblePages = computed(() => {
  const total = totalPages.value || totalPagesComputed.value || 1
  const current = currentPage.value || 1
  const delta = 2
  const pages = []
  const start = Math.max(1, current - delta)
  const end = Math.min(total, current + delta)
  for (let p = start; p <= end; p++) pages.push(p)
  return pages
})

// Helpers used in template
function getSortIcon(field) {
  if (tableSort.value.field !== field) return 'ph-caret-down'
  return tableSort.value.direction === 'asc' ? 'ph-caret-up' : 'ph-caret-down'
}

function sortTable(field) {
  if (tableSort.value.field === field) {
    tableSort.value.direction = tableSort.value.direction === 'asc' ? 'desc' : 'asc'
  } else {
    tableSort.value.field = field
    tableSort.value.direction = 'asc'
  }
  // client sort - rearrange products
  products.value.sort((a, b) => {
    const dir = tableSort.value.direction === 'asc' ? 1 : -1
    if (field === 'price' || field === 'stock') return (a[field] - b[field]) * dir
    if (field === 'name') return a.name.localeCompare(b.name) * dir
    if (field === 'createdAt') return (new Date(a.createdAt) - new Date(b.createdAt)) * dir
    return 0
  })
}

function getCategoryName(categoryId) {
  if (!categoryId) return ''
  const c = categories.value.find((x) => Number(x.id) === Number(categoryId))
  return c ? c.ten : ''
}

function formatCurrency(v) {
  if (v == null) return ''
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v)
}

function formatDate(d) {
  if (!d) return ''
  const dt = new Date(d)
  return dt.toLocaleDateString()
}

function formatTime(d) {
  if (!d) return ''
  const dt = new Date(d)
  return dt.toLocaleTimeString()
}

function getStockClass(stock) {
  if (stock == null) return 'bg-light'
  if (stock === 0) return 'bg-danger text-white'
  if (stock < 10) return 'bg-warning text-dark'
  return 'bg-success text-white'
}

function getStatusClass(status) {
  switch (status) {
    case 'active':
      return 'bg-success'
    case 'inactive':
      return 'bg-secondary'
    case 'out-of-stock':
      return 'bg-danger'
    default:
      return 'bg-light text-dark'
  }
}

function getStatusText(status) {
  switch (status) {
    case 'active':
      return 'Đang bán'
    case 'inactive':
      return 'Ngừng bán'
    case 'out-of-stock':
      return 'Hết hàng'
    default:
      return status
  }
}

function toggleTag(tag) {
  const idx = selectedTags.value.indexOf(tag)
  if (idx === -1) selectedTags.value.push(tag)
  else selectedTags.value.splice(idx, 1)
}

// load categories for selects
async function loadCategories() {
  try {
    const res = await (await import('../../services/api')).default.categories.getAll()
    // If res is array of categories, map to simple structure
    categories.value = Array.isArray(res) ? res : []
  } catch (e) {
    // keep fallback
    console.warn('Could not load categories', e)
  }
}

onMounted(async () => {
  await loadCategories()
  await loadProducts(0)
})

// initial load handled in combined onMounted above
</script>

<style scoped>
.admin-products {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 2rem;
}

.page-title {
  font-size: 2rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 0.5rem;
}

.page-subtitle {
  color: #6c757d;
  margin: 0;
}

.filters-section {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.search-row {
  display: flex;
  gap: 1rem;
  align-items: center;
  margin-bottom: 1rem;
}

.search-row .search-box {
  flex: 1;
}

.advanced-filters {
  border-top: 1px solid #e9ecef;
  padding-top: 1.5rem;
  margin-top: 1rem;
}

.price-range {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.range-separator {
  color: #6c757d;
  font-weight: 500;
}

.tag-filters {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.tag-filter {
  padding: 0.25rem 0.75rem;
  background: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 12px;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s;
}

.tag-filter:hover {
  background: #e9ecef;
}

.tag-filter.active {
  background: #3498db;
  color: white;
  border-color: #3498db;
}

.search-box {
  position: relative;
}

.search-icon {
  position: absolute;
  left: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
  color: #6c757d;
  z-index: 1;
}

.search-input {
  padding-left: 2.5rem;
}

.products-table {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.table-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e9ecef;
}

.view-options {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.view-label {
  font-size: 0.9rem;
  color: #6c757d;
  margin-right: 0.5rem;
}

.view-btn {
  padding: 0.5rem;
  border: 1px solid #dee2e6;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.view-btn:hover {
  background: #f8f9fa;
}

.view-btn.active {
  background: #3498db;
  color: white;
  border-color: #3498db;
}

.table-stats {
  color: #6c757d;
  font-size: 0.9rem;
}

.sort-btn {
  background: none;
  border: none;
  padding: 0;
  font-weight: 600;
  color: #2c3e50;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.sort-btn:hover {
  color: #3498db;
}

.table {
  margin-bottom: 0;
}

.table th {
  border-top: none;
  font-weight: 600;
  color: #2c3e50;
  padding: 1rem 0.75rem;
}

.table td {
  padding: 1rem 0.75rem;
  vertical-align: middle;
}

.product-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.product-image {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  overflow: hidden;
}

.product-image {
  position: relative;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.new-badge,
.featured-badge {
  position: absolute;
  top: 0.25rem;
  right: 0.25rem;
  padding: 0.125rem 0.375rem;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 500;
  color: white;
}

.new-badge {
  background: #28a745;
}

.featured-badge {
  background: #ffc107;
  color: #212529;
}

.product-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.25rem;
  margin-top: 0.25rem;
}

.tag {
  padding: 0.125rem 0.375rem;
  background: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  font-size: 0.75rem;
  color: #6c757d;
}

.tag-more {
  padding: 0.125rem 0.375rem;
  background: #e9ecef;
  border-radius: 8px;
  font-size: 0.75rem;
  color: #6c757d;
}

.discount-percent {
  font-size: 0.75rem;
  color: #dc3545;
  font-weight: 600;
}

.stock-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.low-stock-warning {
  font-size: 0.75rem;
  color: #fd7e14;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.rating-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.rating-stars {
  display: flex;
  gap: 0.125rem;
}

.star {
  color: #e9ecef;
  font-size: 0.875rem;
}

.star.filled {
  color: #ffc107;
}

.rating-text {
  font-size: 0.75rem;
  color: #6c757d;
}

.date-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.date-time {
  font-size: 0.75rem;
  color: #6c757d;
}

.product-name {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 0.25rem;
}

.product-sku {
  font-size: 0.85rem;
  color: #6c757d;
}

.price-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.current-price {
  font-weight: 600;
  color: #2c3e50;
}

.original-price {
  font-size: 0.85rem;
  color: #6c757d;
  text-decoration: line-through;
}

.stock-badge {
  font-weight: 600;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  background-color: #f8f9fa;
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
  color: white;
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
}

.action-buttons .btn {
  padding: 0.375rem 0.75rem;
}

/* Grid View */
.products-grid {
  margin-top: 1rem;
}

.product-card {
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.product-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.product-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  border-bottom: 1px solid #e9ecef;
}

.product-badges {
  display: flex;
  gap: 0.5rem;
}

.product-image-container {
  position: relative;
  aspect-ratio: 1;
  overflow: hidden;
}

.product-image-container .product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.product-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.product-card:hover .product-overlay {
  opacity: 1;
}

.product-card:hover .product-image {
  transform: scale(1.05);
}

.product-card-body {
  padding: 1rem;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.product-title {
  font-size: 1rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 0.5rem;
  line-height: 1.4;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
  font-size: 0.8rem;
  color: #6c757d;
}

.product-rating {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.rating-count {
  font-size: 0.8rem;
  color: #6c757d;
}

.product-price {
  margin-bottom: 0.5rem;
}

.product-stock {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
}

.product-card-footer {
  padding: 1rem;
  border-top: 1px solid #e9ecef;
  background: #f8f9fa;
}

.product-actions {
  display: flex;
  gap: 0.5rem;
  justify-content: center;
}

.product-actions .btn {
  padding: 0.375rem 0.75rem;
}

.pagination-section {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.pagination-info {
  color: #6c757d;
  font-size: 0.9rem;
}

.bulk-actions {
  position: fixed;
  bottom: 2rem;
  left: 50%;
  transform: translateX(-50%);
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 12px;
  padding: 1rem 1.5rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  z-index: 1000;
}

.bulk-actions-content {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.selected-count {
  font-weight: 600;
  color: #2c3e50;
}

.bulk-buttons {
  display: flex;
  gap: 0.5rem;
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  padding: 1.5rem;
  border-bottom: 1px solid #e9ecef;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.modal-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.btn-close {
  background: none;
  border: none;
  font-size: 1.25rem;
  color: #6c757d;
  cursor: pointer;
  padding: 0.25rem;
  border-radius: 4px;
  transition: background-color 0.3s ease;
}

.btn-close:hover {
  background-color: #f8f9fa;
}

.modal-body {
  padding: 1.5rem;
}

.modal-footer {
  padding: 1.5rem;
  border-top: 1px solid #e9ecef;
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
}

/* Responsive */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .header-right {
    width: 100%;
  }

  .header-right .btn {
    width: 100%;
  }

  .products-table {
    padding: 1rem;
  }

  .table-responsive {
    font-size: 0.9rem;
  }

  .action-buttons {
    flex-direction: column;
  }

  .bulk-actions {
    left: 1rem;
    right: 1rem;
    transform: none;
  }

  .bulk-actions-content {
    flex-direction: column;
    align-items: flex-start;
  }

  .bulk-buttons {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
