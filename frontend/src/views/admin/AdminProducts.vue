<template>
  <div class="admin-products">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">Quản lý sản phẩm</h1>
        <p class="page-subtitle">Quản lý danh sách sản phẩm của cửa hàng</p>
      </div>
      <div class="header-right">
        <button class="btn btn-auro-primary" @click="openAddModal">
          <i class="ph-plus me-2"></i>Thêm sản phẩm
        </button>
      </div>
    </div>

    <!-- Advanced Filters and Search -->
    <div class="filters-section">
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

      <div v-if="showAdvancedFilters" class="advanced-filters">
        <div class="row g-3">
          <div class="col-md-3">
            <label class="form-label">Danh mục</label>
            <select class="form-select" v-model.number="selectedCategory">
              <option :value="''">Tất cả danh mục</option>
              <option v-for="c in flatCategoryOptions" :key="c.id" :value="c.id">
                {{ c.label }}
              </option>
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
        <div class="view-options"></div>
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
                    <div class="product-image">
                      <img :src="getProductImage(product)" />
                      <div v-if="product.isNew" class="new-badge">Mới</div>
                    </div>

                    <div v-if="product.isNew" class="new-badge">Mới</div>
                  </div>
                  <div class="product-details">
                    <div class="product-name">{{ product.name }}</div>
                    <div class="product-sku" v-if="product.sku">SKU: {{ product.sku }}</div>
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
                  getCategoryName(product.categoryId)
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
                <div class="action-buttons framed">
                  <button
                    class="btn btn-sm btn-outline-primary"
                    @click="editProduct(product)"
                    title="Chỉnh sửa"
                  >
                    <i class="ph-pencil me-1"></i>
                    Sửa
                  </button>
                  <button
                    class="btn btn-sm btn-outline-success"
                    @click="manageVariants(product)"
                    title="Quản lý biến thể"
                  >
                    <i class="ph-stack me-1"></i>
                    Biến thể
                  </button>
                  <button
                    class="btn btn-sm btn-outline-info"
                    @click="viewProduct(product)"
                    title="Xem chi tiết"
                  >
                    <i class="ph-eye me-1"></i>
                    Xem
                  </button>
                  <button
                    class="btn btn-sm btn-outline-danger"
                    @click="deleteProduct(product)"
                    title="Xóa"
                  >
                    <i class="ph-trash me-1"></i>
                    Xóa
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Grid view removed for cleaner layout -->
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

    <!-- Add/Edit Product Modal with TABS -->
    <div v-if="showAddModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content modal-large" @click.stop>
        <div class="modal-header">
          <h5 class="modal-title">
            {{ editingProduct ? 'Chỉnh sửa sản phẩm' : 'Thêm sản phẩm mới' }}
          </h5>
          <button class="btn-close" @click="closeModal">
            <i class="ph-x"></i>
          </button>
        </div>

        <!-- Tabs Navigation -->
        <div class="modal-tabs">
          <button
            :class="['tab-btn', { active: activeTab === 'basic' }]"
            @click="activeTab = 'basic'"
          >
            <i class="bi bi-info-circle me-1"></i>
            Thông tin cơ bản
          </button>
          <button
            :class="['tab-btn', { active: activeTab === 'images' }]"
            @click="activeTab = 'images'"
          >
            <i class="bi bi-images me-1"></i>
            Hình ảnh
            <span v-if="productForm.images && productForm.images.length > 0" class="tab-badge">
              {{ productForm.images.length }}
            </span>
          </button>
        </div>

        <div class="modal-body">
          <!-- Tab 1: Basic Info -->
          <div v-show="activeTab === 'basic'" class="tab-content">
            <form @submit.prevent="saveProduct">
              <div class="row g-3">
                <div class="col-md-8">
                  <label class="form-label">Tên sản phẩm *</label>
                  <input type="text" class="form-control" v-model="productForm.name" required />
                </div>
                <div class="col-md-4">
                  <label class="form-label">SKU</label>
                  <input 
                    type="text" 
                    class="form-control" 
                    v-model="productForm.sku" 
                    readonly 
                    disabled
                    placeholder="Tự động tạo"
                  />
                </div>
                <div class="col-md-6">
                  <label class="form-label">Danh mục * (CHỈ QUẦN ÁO NAM)</label>
                  <select class="form-select" v-model="productForm.categoryId" required>
                    <option value="">Chọn danh mục</option>
                    <option v-for="c in flatCategoryOptions" :key="c.id" :value="c.id">
                      {{ c.label }}
                    </option>
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
                <div class="">
                  <label class="form-label">Giá bán *</label>
                  <input
                    type="number"
                    class="form-control"
                    v-model.number="productForm.price"
                    required
                    min="0"
                  />
                </div>
              
                <div class="col-12">
                  <label class="form-label">Mô tả sản phẩm</label>
                  <textarea
                    class="form-control"
                    rows="4"
                    v-model="productForm.description"
                    placeholder="Nhập mô tả chi tiết về sản phẩm..."
                  ></textarea>
                </div>
              </div>
            </form>
          </div>

          <!-- Tab 2: Images -->
          <div v-show="activeTab === 'images'" class="tab-content">
            <ImageUploaderAdmin
              :initial-images="productForm.images || []"
              :max-images="10"
              :max-file-size="2"
              :enable-color-association="true"
              :colors="productForm.variantColors || []"
              @update="handleImagesUpdate"
            />
          </div>
        </div>

        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" @click="closeModal">
            <i class="bi bi-x me-2"></i>
            Hủy
          </button>
          <button type="button" class="btn btn-auro-primary" @click="saveProduct">
            <i class="bi bi-check me-2"></i>
            {{ editingProduct ? 'Cập nhật' : 'Thêm sản phẩm' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Variant Management Modal -->
    <div v-if="showVariantModal" class="modal-overlay" @click="closeVariantModal">
      <div class="modal-content modal-large" @click.stop>
        <div class="modal-header">
          <h5 class="modal-title">
            <i class="bi bi-grid-3x3 me-2"></i>
            Quản lý biến thể - {{ productForm.name }}
          </h5>
          <button class="btn-close" @click="closeVariantModal">
            <i class="ph-x"></i>
          </button>
        </div>

        <div class="modal-body">
          <VariantManagerAdmin
            :product-id="productForm.id"
            :product-name="productForm.name"
            :product-sku="productForm.sku"
            :category="getCategoryType(productForm.categoryId)"
            :initial-variants="productForm.variants || []"
            :initial-colors="productForm.variantColors || []"
            :initial-material="productForm.material || ''"
            @update="handleVariantsUpdate"
            @save="handleVariantsSave"
            @cancel="closeVariantModal"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import VariantManagerAdmin from '@/components/admin/VariantManagerAdmin.vue'
import ImageUploaderAdmin from '@/components/admin/ImageUploaderAdmin.vue'
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
const showVariantModal = ref(false)
const editingProduct = ref(null)
const isLoading = ref(false)

// NEW: Tab management
const activeTab = ref('basic')

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
const products = ref([]) // danh sách sản phẩm
const totalItems = ref(0) // tổng bản ghi
const totalPages = ref(1) // tổng số trang
const productForm = ref({
  // model cho modal thêm/sửa
  id: null,
  name: '',
  sku: '',
  categoryId: '', // lưu ID danh mục
  status: 'active',
  price: 0,
  originalPrice: 0, // Đặt 0 thay vì null
  stock: 0,
  description: '',
  image: '',
  images: [],
  variants: [],
  variantColors: [],
  material: '',
  // SEO tab removed, keep minimal flags
  isNew: false,
  tags: [],
})

// flatten categories (tree) into options with indentation
const flatCategoryOptions = computed(() => {
  const out = []
  const walk = (nodes = [], level = 0) => {
    nodes.forEach((n) => {
      const name = n.ten || n.name || n.slug || '#' + n.id
      const label = (level ? '— '.repeat(level) : '') + name
      out.push({ id: n.id, label })
      if (n.children?.length) walk(n.children, level + 1)
    })
  }
  if (!categories.value || categories.value.length === 0) return out
  const firstHasChildren = categories.value.some((c) => c.children && c.children.length)
  if (firstHasChildren) {
    walk(categories.value, 0)
  } else {
    categories.value.forEach((n) => {
      const name = n.ten || n.name || n.slug || '#' + n.id
      out.push({ id: n.id, label: name })
    })
  }
  return out
})

// categories already declared above
// const categories = ref([])

function mapFromApi(sp) {
  return {
    id: sp.id,
    name: sp.ten,
    sku: sp.sku || '',
    categoryId: sp.danhMucId || sp.categoryId || sp.danhMuc?.id || '',
    price: Number(sp.gia) || 0,
    originalPrice: sp.giaGoc ? Number(sp.giaGoc) : 0, // Đặt 0 thay vì null
    stock: sp.tonKho || 0,
    status: sp.trangThai || 'active',
    description: sp.moTa || '',
    image: sp.anhDaiDien || '',
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
    // Eagerly load images for rows missing thumbnails
    for (const p of products.value) {
      if (!p.image) {
        // fire and forget; will set when done
        ensureProductImageLoaded(p)
      }
    }
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
async function loadProductDetail(id) {
  try {
    const res = await sanPhamService.get(id)
    const sp = mapFromApi(res)
    
    // Load hình ảnh chi tiết từ API riêng
    let images = []
    try {
      const { default: api } = await import('../../services/api')
      const hinhAnhs = await api.get(`/hinh-anh/san-pham/${id}`)
      if (hinhAnhs && Array.isArray(hinhAnhs)) {
        images = hinhAnhs.map(ha => ({
          id: ha.id,
          url: ha.url,
          isPrimary: ha.laDaiDien || false,
          order: ha.thuTu || 0,
          alt: sp.name
        }))
      }
    } catch (e) {
      console.warn('Could not load images:', e)
    }
    
    return {
      ...sp,
      images: images,
      variants: res.bienThes || [],
      variantColors: res.mauSac || [],
      material: res.chatLieu || '',
      slug: res.slug || '',
      metaTitle: res.metaTitle || '',
      metaDescription: res.metaDescription || '',
      isNew: !!res.isNew,
      tags: res.tags || [],
    }
  } catch {
    return null
  }
}

const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    loadProducts(page - 1)
  }
}

// Mở modal thêm sản phẩm mới
const openAddModal = () => {
  // Reset form
  productForm.value = {
    id: null,
    name: '',
    sku: `SKU-${Date.now().toString().slice(-8)}`,
    categoryId: '',
    status: 'active',
    price: 0,
    originalPrice: 0, // Đặt 0 thay vì null để hiển thị được
    stock: 0,
    description: '',
    image: '',
    images: [],
    variants: [],
    variantColors: [],
    material: '',
    isNew: false,
    tags: [],
  }
  activeTab.value = 'basic'
  showAddModal.value = true
}

// Nút Sửa - chỉ mở modal thông tin
const editProduct = async (product) => {
  editingProduct.value = product
  const full = await loadProductDetail(product.id)
  productForm.value = full ? { ...full } : { ...product }
  // đảm bảo có categoryId (phòng trường hợp dữ liệu cũ là 'category')
  if (!productForm.value.categoryId && productForm.value.category) {
    productForm.value.categoryId = productForm.value.category
  }
  // Mở modal ở tab thông tin cơ bản (không phải tab biến thể)
  activeTab.value = 'basic'
  showAddModal.value = true
}

const manageVariants = async (product) => {
  editingProduct.value = product
  const full = await loadProductDetail(product.id)
  productForm.value = full ? { ...full } : { ...product }
  // đảm bảo có categoryId
  if (!productForm.value.categoryId && productForm.value.category) {
    productForm.value.categoryId = productForm.value.category
  }
  // Mở modal quản lý biến thể riêng
  showVariantModal.value = true
}



const viewProduct = (product) => {
  // Chuyển đến trang chi tiết sản phẩm
  window.open(`/product/${product.id}`, '_blank')
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
    originalPrice: 0, // Đặt 0 thay vì null
    stock: 0,
    description: '',
    image: '',
  }
}

const closeVariantModal = () => {
  showVariantModal.value = false
  // Reload products to reflect variant changes
  loadProducts(currentPage.value - 1)
}

const saveProduct = async () => {
  const payload = mapToApi(productForm.value)
  try {
    let productId
    if (editingProduct.value) {
      const res = await sanPhamService.update(editingProduct.value.id, payload)
      productId = res?.id || editingProduct.value.id
    } else {
      const res = await sanPhamService.create(payload)
      productId = res?.id
    }

    // Xử lý hình ảnh nếu có productId
    if (productId && productForm.value.images) {
      const { default: api } = await import('../../services/api')
      const images = productForm.value.images || []
      
      // 1. Lấy danh sách hình ảnh hiện tại từ backend (nếu đang edit)
      let existingImages = []
      if (editingProduct.value) {
        try {
          existingImages = await api.get(`/hinh-anh/san-pham/${productId}`)
        } catch (e) {
          console.warn('Could not load existing images:', e)
        }
      }
      
      // 2. Xóa các ảnh không còn trong danh sách
      const currentImageUrls = images.map(img => img.url).filter(Boolean)
      for (const existingImg of existingImages) {
        if (!currentImageUrls.includes(existingImg.url)) {
          try {
            await api.delete(`/hinh-anh/${existingImg.id}`)
            console.log('Deleted image:', existingImg.id)
          } catch (e) {
            console.warn('Failed to delete image:', e)
          }
        }
      }
      
      // 3. Upload các ảnh mới (có file)
      const newFiles = images.filter((img) => img.file instanceof File)
      let firstUploadedUrl = ''
      for (let i = 0; i < newFiles.length; i++) {
        const img = newFiles[i]
        try {
          const saved = await api.upload(`/hinh-anh/san-pham/${productId}`, img.file, {
            laDaiDien: !!img.isPrimary,
            thuTu: images.indexOf(img),
          })
          if (!firstUploadedUrl && saved?.url) firstUploadedUrl = saved.url
          console.log('Uploaded new image:', saved)
        } catch (e) {
          console.warn('Upload image failed:', e)
        }
      }
      
      // 4. Cập nhật thứ tự và ảnh đại diện cho các ảnh đã tồn tại
      for (let i = 0; i < images.length; i++) {
        const img = images[i]
        // Chỉ xử lý ảnh đã có ID (đã lưu trên server)
        if (img.id && !img.file) {
          const existingImg = existingImages.find(e => e.id === img.id)
          if (existingImg) {
            // Kiểm tra xem có thay đổi không
            const needsUpdate = 
              existingImg.thuTu !== i || 
              existingImg.laDaiDien !== !!img.isPrimary
            
            if (needsUpdate) {
              try {
                // Cập nhật trong DB
                await api.put(`/hinh-anh/${img.id}`, {
                  laDaiDien: !!img.isPrimary,
                  thuTu: i
                })
                console.log('Updated image order/primary:', img.id)
              } catch (e) {
                console.warn('Failed to update image:', e)
              }
            }
          }
        }
      }

      // Nếu có ảnh mới, cập nhật ngay trong danh sách hiện tại để hiển thị tức thì
      if (firstUploadedUrl) {
        const idx = products.value.findIndex((p) => String(p.id) === String(productId))
        if (idx > -1) {
          products.value[idx].image = firstUploadedUrl
        }
      }
    }

    // Reload current page (convert 1-based UI page to 0-based API page) để đồng bộ dữ liệu
    await loadProducts((currentPage.value || 1) - 1)
    closeModal()
  } catch (e) {
    console.error('Save product error', e)
    alert('Lưu sản phẩm lỗi: ' + (e?.message || JSON.stringify(e)))
  }
}

const deleteProduct = async (product) => {
  try {
    // Lấy số lượng biến thể của sản phẩm
    const { default: variantService } = await import('../../services/variantService')
    let variantCount = 0
    try {
      const variants = await variantService.getBySanPham(product.id)
      variantCount = variants ? variants.length : 0
    } catch (e) {
      console.warn('Could not load variants:', e)
    }
    
    // Hiển thị thông báo xác nhận với số lượng biến thể
    let message = `Bạn có chắc chắn muốn xóa sản phẩm "${product.name}"?`
    if (variantCount > 0) {
      message = `Sản phẩm "${product.name}" có ${variantCount} biến thể.\n\nXóa sản phẩm sẽ xóa toàn bộ ${variantCount} biến thể này.\n\nBạn có chắc chắn muốn tiếp tục?`
    }
    
    if (!confirm(message)) return
    
    await sanPhamService.delete(product.id)
    alert(`Đã xóa thành công sản phẩm "${product.name}"${variantCount > 0 ? ` và ${variantCount} biến thể` : ''}`)
    // Reload current page (convert 1-based UI page to 0-based API page)
    await loadProducts((currentPage.value || 1) - 1)
  } catch (e) {
    console.error('Delete product error', e)
    alert('Xóa sản phẩm lỗi: ' + (e?.message || JSON.stringify(e)))
  }
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

const formatDate = (date) => {
  return new Intl.DateTimeFormat('vi-VN').format(date)
}

// const getCategoryName = (category) => {
//   const categories = {
//     'ao': 'Áo',
//     'ao-so-mi': 'Áo sơ mi',
//     'ao-thun': 'Áo thun',
//     'ao-khoac': 'Áo khoác',
//     'ao-len': 'Áo len/Hoodie',
//     'ao-vest': 'Áo vest',
//     'quan': 'Quần',
//     'quan-au': 'Quần âu',
//     'quan-jean': 'Quần jean',
//     'quan-kaki': 'Quần kaki',
//     'quan-short': 'Quần short',
//     'quan-jogger': 'Quần jogger'
//   }
//   return categories[category] || category
// }

const getStatusText = (status) => {
  const statuses = {
    active: 'Đang bán',
    inactive: 'Ngừng bán',
    'out-of-stock': 'Hết hàng',
  }
  return statuses[status] || status
}

// const getStatusClass = (status) => {
//   const classes = {
//     'active': 'bg-success',
//     'inactive': 'bg-warning',
//     'out-of-stock': 'bg-danger'
//   }
//   return classes[status] || 'bg-secondary'
// }

const getStockClass = (stock) => {
  if (stock === 0) return 'text-danger'
  if (stock < 10) return 'text-warning'
  return 'text-success'
}

const clearFilters = () => {
  searchQuery.value = ''
  selectedCategory.value = ''
  selectedStatus.value = ''
  priceRange.value = { min: null, max: null }
  stockFilter.value = ''
  createdDate.value = ''
  selectedTags.value = []
  sortBy.value = 'newest'
}

const toggleAdvancedFilters = () => {
  showAdvancedFilters.value = !showAdvancedFilters.value
}

const toggleTag = (tag) => {
  const index = selectedTags.value.indexOf(tag)
  if (index > -1) {
    selectedTags.value.splice(index, 1)
  } else {
    selectedTags.value.push(tag)
  }
}

const sortTable = (field) => {
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
  // try flat options (includes nested children flattened)
  const opt = flatCategoryOptions.value.find((x) => Number(x.id) === Number(categoryId))
  if (opt) return opt.label.replace(/^—\s+/g, '')
  const c = categories.value.find((x) => Number(x.id) === Number(categoryId))
  return c ? c.ten || c.name || '' : ''
}

function formatCurrency(v) {
  if (v == null) return ''
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v)
}

// function formatDate(d) {
//   if (!d) return ''
//   const dt = new Date(d)
//   return dt.toLocaleDateString()
// }

function formatTime(d) {
  if (!d) return ''
  const dt = new Date(d)
  return dt.toLocaleTimeString()
}

const productImageCache = new Map()
async function ensureProductImageLoaded(product) {
  if (!product || !product.id) return
  if (product.image && typeof product.image === 'string') return
  if (productImageCache.has(product.id)) {
    product.image = productImageCache.get(product.id)
    return
  }
  try {
    const { default: api } = await import('../../services/api')
    const images = await api.get(`/hinh-anh/san-pham/${product.id}`)
    const first =
      Array.isArray(images) && images.length ? images.find((i) => i.laDaiDien) || images[0] : null
    if (first?.url) {
      product.image = first.url
      productImageCache.set(product.id, first.url)
    }
  } catch {
    // ignore; fallback will handle
  }
}

function getProductImage(product) {
  const url = product?.image
  if (!url || typeof url !== 'string') {
    ensureProductImageLoaded(product)
    return '/favicon.ico'
  }
  return url // cho phép '/files/..' đi qua proxy
}

// function getStockClass(stock) {
//   if (stock == null) return 'bg-light'
//   if (stock === 0) return 'bg-danger text-white'
//   if (stock < 10) return 'bg-warning text-dark'
//   return 'bg-success text-white'
// }

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

/* Duplicate block removed: editProduct, viewProduct, deleteProduct, closeModal, saveProduct, and getStatusText.
   The correct implementations are already present above in the script. */

// function toggleTag(tag) {
//   const idx = selectedTags.value.indexOf(tag)
//   if (idx === -1) selectedTags.value.push(tag)
//   else selectedTags.value.splice(idx, 1)
// }

// load categories for selects
async function loadCategories() {
  try {
    const api = (await import('../../services/api')).default
    const res = await api.categories.getAll()
    const mapItem = (item) => ({
      id: item.id,
      ten: item.ten || item.name || '',
      slug: item.slug || '',
      parentId: item.idCha ?? null,
      children: [],
    })

    const flat = (Array.isArray(res) ? res : []).map(mapItem)
    const byId = Object.fromEntries(flat.map((f) => [f.id, f]))
    const roots = []
    flat.forEach((f) => {
      if (f.parentId && byId[f.parentId]) {
        byId[f.parentId].children ||= []
        byId[f.parentId].children.push(f)
      } else {
        roots.push(f)
      }
    })
    categories.value = roots
  } catch (e) {
    console.warn('Could not load categories', e)
    // fallback keep existing categories value
  }
}



// NEW: Helper methods
const getCategoryType = (categoryId) => {
  const c = categories.value.find((x) => Number(x.id) === Number(categoryId))
  const slug = c?.slug || ''
  if (slug.startsWith('ao')) return 'ao'
  if (slug.startsWith('quan')) return 'quan'
  return 'ao'
}



// SEO & tag helpers removed with SEO tab

const handleImagesUpdate = (images) => {
  productForm.value.images = images
  // Set primary image as main product image
  const primary = images.find((img) => img.isPrimary)
  if (primary) {
    productForm.value.image = primary.url
  }
}

const handleVariantsUpdate = (variantsData) => {
  productForm.value.variants = variantsData.variants
  productForm.value.variantColors = variantsData.colors
  productForm.value.material = variantsData.material
  // Update total stock
  productForm.value.stock = variantsData.totalStock
}

const handleVariantsSave = (variantsData) => {
  // Cập nhật dữ liệu
  handleVariantsUpdate(variantsData)
  // Đóng modal sau khi lưu thành công
  closeVariantModal()
}

// Lifecycle
onMounted(async () => {
  await loadCategories()
  await loadProducts((currentPage.value || 1) - 1)
})

// initial load handled in combined onMounted above
</script>

<style scoped>
.admin-products {
  width: 100%;
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
  background: #6366f1;
  color: white;
  border-color: #6366f1;
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
  background: #6366f1;
  color: white;
  border-color: #6366f1;
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
  color: #6366f1;
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

.new-badge {
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

/* removed .featured-badge */

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

.action-buttons.framed {
  border: 1px solid #dee2e6;
  border-radius: 8px;
  padding: 0.25rem;
  background: #f8f9fa;
}

.badge-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-left: 6px;
  background: #ced4da; /* off state */
}
/* removed .badge-dot.on (no featured state) */

/* Dark theme: make outline buttons more visible */
.action-buttons .btn-outline-info {
  color: #0dcaf0;
  border-color: #0dcaf0;
}
.action-buttons .btn-outline-info:hover {
  background-color: rgba(13, 202, 240, 0.1);
}
.action-buttons .btn-outline-success {
  color: #10b981;
  border-color: #10b981;
}
.action-buttons .btn-outline-success:hover {
  background-color: rgba(16, 185, 129, 0.1);
}
.action-buttons .btn-outline-warning {
  color: #f59e0b;
  border-color: #f59e0b;
}
.action-buttons .btn-outline-warning:hover {
  background-color: rgba(245, 158, 11, 0.12);
}
.action-buttons .btn-outline-danger {
  color: #ef4444;
  border-color: #ef4444;
}
.action-buttons .btn-outline-danger:hover {
  background-color: rgba(239, 68, 68, 0.12);
}
/* Remove border for Edit button only to make it look primary without outline */
.action-buttons .btn-outline-primary {
  border-color: transparent;
  color: #3b82f6;
}
.action-buttons .btn-outline-primary:hover {
  background-color: rgba(59, 130, 246, 0.12);
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

.modal-content.modal-large {
  max-width: 900px;
  max-height: 95vh;
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

/* Modal Tabs */
.modal-tabs {
  display: flex;
  border-bottom: 2px solid #e2e8f0;
  background: #f8fafb;
  padding: 0 1.5rem;
  overflow-x: auto;
}

.tab-btn {
  position: relative;
  padding: 1rem 1.5rem;
  background: none;
  border: none;
  color: #64748b;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.tab-btn:hover {
  color: #6366f1;
  background: rgba(99, 102, 241, 0.05);
}

.tab-btn.active {
  color: #6366f1;
  font-weight: 600;
  border-bottom: 3px solid #6366f1;
  margin-bottom: -2px;
}

.tab-badge {
  background: #6366f1;
  color: white;
  padding: 0.2rem 0.5rem;
  border-radius: 10px;
  font-size: 0.75rem;
  font-weight: 600;
  min-width: 20px;
  text-align: center;
}

.tab-content {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Tags Input */
.tags-input {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  padding: 0.75rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  min-height: 48px;
  background: white;
}

.tag-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.4rem 0.75rem;
  background: #eef2ff;
  border: 1px solid #c7d2fe;
  border-radius: 6px;
  color: #4338ca;
  font-size: 0.85rem;
  font-weight: 500;
}

.tag-remove {
  background: none;
  border: none;
  color: #6366f1;
  cursor: pointer;
  padding: 0;
  display: flex;
  align-items: center;
  font-size: 1rem;
  transition: color 0.2s;
}

.tag-remove:hover {
  color: #ef4444;
}

.tag-input {
  flex: 1;
  border: none;
  outline: none;
  min-width: 150px;
  font-size: 0.9rem;
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

  .modal-tabs {
    padding: 0 0.5rem;
  }

  .tab-btn {
    padding: 0.75rem 1rem;
    font-size: 0.85rem;
  }

  .modal-content.modal-large {
    width: 95%;
    max-width: none;
  }
}
</style>
