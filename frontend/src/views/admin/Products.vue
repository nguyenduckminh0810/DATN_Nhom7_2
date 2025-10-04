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

    <!-- Filters and Search -->
    <div class="filters-section">
      <div class="row g-3">
        <div class="col-md-4">
          <div class="search-box">
            <i class="ph-magnifying-glass search-icon"></i>
            <input
              type="text"
              class="form-control search-input"
              placeholder="Tìm kiếm sản phẩm..."
              v-model="searchQuery"
            />
          </div>
        </div>
        <div class="col-md-3">
          <select class="form-select" v-model="selectedCategory">
            <option value="">Tất cả danh mục</option>
            <option value="ao">Áo</option>
            <option value="quan">Quần</option>
            <option value="phu-kien">Phụ kiện</option>
          </select>
        </div>
        <div class="col-md-3">
          <select class="form-select" v-model="selectedStatus">
            <option value="">Tất cả trạng thái</option>
            <option value="active">Đang bán</option>
            <option value="inactive">Ngừng bán</option>
            <option value="out-of-stock">Hết hàng</option>
          </select>
        </div>
        <div class="col-md-2">
          <button class="btn btn-outline-secondary w-100" @click="clearFilters">
            <i class="ph-arrow-clockwise me-1"></i>Xóa bộ lọc
          </button>
        </div>
      </div>
    </div>

    <!-- Products Table -->
    <div class="products-table">
      <div class="table-responsive">
        <table class="table table-hover">
          <thead>
            <tr>
              <th>
                <input type="checkbox" class="form-check-input" v-model="selectAll" @change="toggleSelectAll">
              </th>
              <th>Sản phẩm</th>
              <th>Danh mục</th>
              <th>Giá</th>
              <th>Tồn kho</th>
              <th>Trạng thái</th>
              <th>Ngày tạo</th>
              <th>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="product in filteredProducts" :key="product.id">
              <td>
                <input type="checkbox" class="form-check-input" v-model="selectedProducts" :value="product.id">
              </td>
              <td>
                <div class="product-info">
                  <div class="product-image">
                    <img :src="product.image" :alt="product.name" class="img-fluid">
                  </div>
                  <div class="product-details">
                    <div class="product-name">{{ product.name }}</div>
                    <div class="product-sku">SKU: {{ product.sku }}</div>
                  </div>
                </div>
              </td>
              <td>
                <span class="badge bg-light text-dark">{{ getCategoryName(product.category) }}</span>
              </td>
              <td>
                <div class="price-info">
                  <div class="current-price">{{ formatCurrency(product.price) }}</div>
                  <div v-if="product.originalPrice" class="original-price">{{ formatCurrency(product.originalPrice) }}</div>
                </div>
              </td>
              <td>
                <span :class="['stock-badge', getStockClass(product.stock)]">
                  {{ product.stock }}
                </span>
              </td>
              <td>
                <span :class="['status-badge', getStatusClass(product.status)]">
                  {{ getStatusText(product.status) }}
                </span>
              </td>
              <td>{{ formatDate(product.createdAt) }}</td>
              <td>
                <div class="action-buttons">
                  <button class="btn btn-sm btn-outline-primary" @click="editProduct(product)" title="Chỉnh sửa">
                    <i class="ph-pencil"></i>
                  </button>
                  <button class="btn btn-sm btn-outline-info" @click="viewProduct(product)" title="Xem chi tiết">
                    <i class="ph-eye"></i>
                  </button>
                  <button class="btn btn-sm btn-outline-danger" @click="deleteProduct(product)" title="Xóa">
                    <i class="ph-trash"></i>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Pagination -->
    <div class="pagination-section">
      <div class="row align-items-center">
        <div class="col-md-6">
          <div class="pagination-info">
            Hiển thị {{ (currentPage - 1) * itemsPerPage + 1 }} - {{ Math.min(currentPage * itemsPerPage, totalItems) }} 
            trong tổng số {{ totalItems }} sản phẩm
          </div>
        </div>
        <div class="col-md-6">
          <nav>
            <ul class="pagination justify-content-end">
              <li class="page-item" :class="{ disabled: currentPage === 1 }">
                <a class="page-link" href="#" @click.prevent="changePage(currentPage - 1)">Trước</a>
              </li>
              <li class="page-item" v-for="page in visiblePages" :key="page" :class="{ active: currentPage === page }">
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
          <h5 class="modal-title">{{ editingProduct ? 'Chỉnh sửa sản phẩm' : 'Thêm sản phẩm mới' }}</h5>
          <button class="btn-close" @click="closeModal">
            <i class="ph-x"></i>
          </button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="saveProduct">
            <div class="row g-3">
              <div class="col-md-8">
                <label class="form-label">Tên sản phẩm *</label>
                <input type="text" class="form-control" v-model="productForm.name" required>
              </div>
              <div class="col-md-4">
                <label class="form-label">SKU *</label>
                <input type="text" class="form-control" v-model="productForm.sku" required>
              </div>
              <div class="col-md-6">
                <label class="form-label">Danh mục *</label>
                <select class="form-select" v-model="productForm.category" required>
                  <option value="">Chọn danh mục</option>
                  <option value="ao">Áo</option>
                  <option value="quan">Quần</option>
                  <option value="phu-kien">Phụ kiện</option>
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
                <input type="number" class="form-control" v-model.number="productForm.price" required>
              </div>
              <div class="col-md-4">
                <label class="form-label">Giá gốc</label>
                <input type="number" class="form-control" v-model.number="productForm.originalPrice">
              </div>
              <div class="col-md-4">
                <label class="form-label">Số lượng tồn kho</label>
                <input type="number" class="form-control" v-model.number="productForm.stock">
              </div>
              <div class="col-12">
                <label class="form-label">Mô tả</label>
                <textarea class="form-control" rows="3" v-model="productForm.description"></textarea>
              </div>
              <div class="col-12">
                <label class="form-label">Hình ảnh</label>
                <input type="url" class="form-control" v-model="productForm.image" placeholder="URL hình ảnh">
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

// Reactive data
const searchQuery = ref('')
const selectedCategory = ref('')
const selectedStatus = ref('')
const selectedProducts = ref([])
const selectAll = ref(false)
const currentPage = ref(1)
const itemsPerPage = 10
const showAddModal = ref(false)
const editingProduct = ref(null)

const productForm = ref({
  name: '',
  sku: '',
  category: '',
  status: 'active',
  price: 0,
  originalPrice: 0,
  stock: 0,
  description: '',
  image: ''
})

// Mock data
const products = ref([
  {
    id: 1,
    name: 'Áo sơ mi nam cao cấp',
    sku: 'ASM001',
    category: 'ao',
    price: 450000,
    originalPrice: 600000,
    stock: 25,
    status: 'active',
    description: 'Áo sơ mi nam chất liệu cotton 100%',
    image: 'https://images.unsplash.com/photo-1594938298605-cd64d190e6bc?ixlib=rb-4.0.3&auto=format&fit=crop&w=100&q=80',
    createdAt: new Date('2024-01-15')
  },
  {
    id: 2,
    name: 'Quần âu nam',
    sku: 'QAN002',
    category: 'quan',
    price: 650000,
    originalPrice: 800000,
    stock: 15,
    status: 'active',
    description: 'Quần âu nam thiết kế hiện đại',
    image: 'https://images.unsplash.com/photo-1506629905607-1a5a1b1b1b1b?ixlib=rb-4.0.3&auto=format&fit=crop&w=100&q=80',
    createdAt: new Date('2024-01-20')
  },
  {
    id: 3,
    name: 'Áo khoác nam',
    sku: 'AKN003',
    category: 'ao',
    price: 850000,
    originalPrice: 1200000,
    stock: 0,
    status: 'out-of-stock',
    description: 'Áo khoác nam phong cách casual',
    image: 'https://images.unsplash.com/photo-1551028719-00167b16eac5?ixlib=rb-4.0.3&auto=format&fit=crop&w=100&q=80',
    createdAt: new Date('2024-01-25')
  },
  {
    id: 4,
    name: 'Áo thun nam',
    sku: 'ATN004',
    category: 'ao',
    price: 250000,
    originalPrice: 350000,
    stock: 50,
    status: 'active',
    description: 'Áo thun nam chất liệu cotton mềm mại',
    image: 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?ixlib=rb-4.0.3&auto=format&fit=crop&w=100&q=80',
    createdAt: new Date('2024-02-01')
  }
])

// Computed
const filteredProducts = computed(() => {
  let filtered = products.value

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(product =>
      product.name.toLowerCase().includes(query) ||
      product.sku.toLowerCase().includes(query)
    )
  }

  if (selectedCategory.value) {
    filtered = filtered.filter(product => product.category === selectedCategory.value)
  }

  if (selectedStatus.value) {
    filtered = filtered.filter(product => product.status === selectedStatus.value)
  }

  return filtered
})

const totalItems = computed(() => filteredProducts.value.length)
const totalPages = computed(() => Math.ceil(totalItems.value / itemsPerPage))

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
const formatCurrency = (amount) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount)
}

const formatDate = (date) => {
  return new Intl.DateTimeFormat('vi-VN').format(date)
}

const getCategoryName = (category) => {
  const categories = {
    'ao': 'Áo',
    'quan': 'Quần',
    'phu-kien': 'Phụ kiện'
  }
  return categories[category] || category
}

const getStatusText = (status) => {
  const statuses = {
    'active': 'Đang bán',
    'inactive': 'Ngừng bán',
    'out-of-stock': 'Hết hàng'
  }
  return statuses[status] || status
}

const getStatusClass = (status) => {
  const classes = {
    'active': 'bg-success',
    'inactive': 'bg-warning',
    'out-of-stock': 'bg-danger'
  }
  return classes[status] || 'bg-secondary'
}

const getStockClass = (stock) => {
  if (stock === 0) return 'text-danger'
  if (stock < 10) return 'text-warning'
  return 'text-success'
}

const clearFilters = () => {
  searchQuery.value = ''
  selectedCategory.value = ''
  selectedStatus.value = ''
}

const toggleSelectAll = () => {
  if (selectAll.value) {
    selectedProducts.value = filteredProducts.value.map(p => p.id)
  } else {
    selectedProducts.value = []
  }
}

const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
  }
}

const editProduct = (product) => {
  editingProduct.value = product
  productForm.value = { ...product }
  showAddModal.value = true
}

const viewProduct = (product) => {
  // Navigate to product detail
  console.log('View product:', product)
}

const deleteProduct = (product) => {
  if (confirm(`Bạn có chắc chắn muốn xóa sản phẩm "${product.name}"?`)) {
    const index = products.value.findIndex(p => p.id === product.id)
    if (index > -1) {
      products.value.splice(index, 1)
    }
  }
}

const closeModal = () => {
  showAddModal.value = false
  editingProduct.value = null
  productForm.value = {
    name: '',
    sku: '',
    category: '',
    status: 'active',
    price: 0,
    originalPrice: 0,
    stock: 0,
    description: '',
    image: ''
  }
}

const saveProduct = () => {
  if (editingProduct.value) {
    // Update existing product
    const index = products.value.findIndex(p => p.id === editingProduct.value.id)
    if (index > -1) {
      products.value[index] = { ...productForm.value, id: editingProduct.value.id }
    }
  } else {
    // Add new product
    const newProduct = {
      ...productForm.value,
      id: Date.now(),
      createdAt: new Date()
    }
    products.value.unshift(newProduct)
  }
  
  closeModal()
}

const bulkUpdateStatus = (status) => {
  if (confirm(`Bạn có chắc chắn muốn cập nhật trạng thái cho ${selectedProducts.value.length} sản phẩm?`)) {
    selectedProducts.value.forEach(productId => {
      const product = products.value.find(p => p.id === productId)
      if (product) {
        product.status = status
      }
    })
    selectedProducts.value = []
    selectAll.value = false
  }
}

const bulkDelete = () => {
  if (confirm(`Bạn có chắc chắn muốn xóa ${selectedProducts.value.length} sản phẩm?`)) {
    selectedProducts.value.forEach(productId => {
      const index = products.value.findIndex(p => p.id === productId)
      if (index > -1) {
        products.value.splice(index, 1)
      }
    })
    selectedProducts.value = []
    selectAll.value = false
  }
}

// Lifecycle
onMounted(() => {
  console.log('Products page loaded')
})
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

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
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
