<template>
  <div class="admin-inventory">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">Quản lý tồn kho</h1>
        <p class="page-subtitle">Theo dõi và cập nhật tồn kho theo từng biến thể sản phẩm</p>
      </div>
      <div class="header-right">
        <button class="btn btn-outline-info" @click="exportInventory">
          <i class="bi bi-download me-2"></i>
          Xuất Excel
        </button>
        <button class="btn btn-outline-success" @click="showBulkUpdateModal = true">
          <i class="bi bi-pencil-square me-2"></i>
          Cập nhật hàng loạt
        </button>
      </div>
    </div>

    <!-- Inventory Stats -->
    <div class="inventory-stats">
      <div class="row g-3">
        <div class="col-lg-3 col-md-6">
          <div class="stat-card total">
            <div class="stat-icon">
              <i class="bi bi-box"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ inventoryStats.totalVariants }}</div>
              <div class="stat-label">Tổng biến thể</div>
            </div>
          </div>
        </div>
        <div class="col-lg-3 col-md-6">
          <div class="stat-card in-stock">
            <div class="stat-icon">
              <i class="bi bi-check-circle"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ inventoryStats.inStock }}</div>
              <div class="stat-label">Còn hàng</div>
              <div class="stat-progress">
                <div class="progress-mini">
                  <div class="progress-bar bg-success" :style="{ width: (inventoryStats.inStock / inventoryStats.totalVariants * 100) + '%' }"></div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="col-lg-3 col-md-6">
          <div class="stat-card low-stock">
            <div class="stat-icon">
              <i class="bi bi-exclamation-triangle"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value text-warning">{{ inventoryStats.lowStock }}</div>
              <div class="stat-label">Sắp hết (< 10)</div>
            </div>
          </div>
        </div>
        <div class="col-lg-3 col-md-6">
          <div class="stat-card out-stock">
            <div class="stat-icon">
              <i class="bi bi-x-circle"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value text-danger">{{ inventoryStats.outOfStock }}</div>
              <div class="stat-label">Hết hàng</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Low Stock Alerts -->
    <div v-if="lowStockAlerts.length > 0" class="alerts-section">
      <div class="alert-card">
        <div class="alert-header">
          <h5>
            <i class="bi bi-exclamation-triangle text-warning me-2"></i>
            Cảnh báo tồn kho ({{ lowStockAlerts.length }} biến thể cần chú ý)
          </h5>
          <button class="btn btn-sm btn-outline-warning" @click="showLowStockModal = true">
            <i class="bi bi-list-check me-1"></i>
            Xem danh sách
          </button>
        </div>
        <div class="alerts-preview">
          <div v-for="alert in lowStockAlerts.slice(0, 5)" :key="alert.id" class="alert-item">
            <div class="alert-product">
              <img :src="alert.productImage" :alt="alert.productName" class="alert-product-img" />
              <div class="alert-product-info">
                <div class="alert-product-name">{{ alert.productName }}</div>
                <div class="alert-variant">Size {{ alert.size }} - {{ alert.color }}</div>
              </div>
            </div>
            <div class="alert-stock" :class="getStockClass(alert.stock)">
              <strong>{{ alert.stock }}</strong> sản phẩm
            </div>
            <button class="btn btn-sm btn-outline-primary" @click="quickUpdateStock(alert)">
              <i class="bi bi-pencil"></i>
              Nhập hàng
            </button>
          </div>
          <div v-if="lowStockAlerts.length > 5" class="alert-more">
            Và {{ lowStockAlerts.length - 5 }} cảnh báo khác...
          </div>
        </div>
      </div>
    </div>

    <!-- Filters -->
    <div class="filters-section">
      <div class="search-row">
        <div class="search-box">
          <i class="bi bi-search search-icon"></i>
          <input
            type="text"
            class="form-control search-input"
            placeholder="Tìm sản phẩm, SKU, biến thể..."
            v-model="searchQuery"
          />
        </div>
        <select class="form-select" v-model="stockFilter" style="max-width: 200px;">
          <option value="">Tất cả tồn kho</option>
          <option value="in-stock">Còn hàng (> 10)</option>
          <option value="low-stock">Sắp hết (1-10)</option>
          <option value="out-of-stock">Hết hàng (0)</option>
        </select>
        <select class="form-select" v-model="categoryFilter" style="max-width: 200px;">
          <option value="">Tất cả danh mục</option>
          <option value="ao">Áo</option>
          <option value="quan">Quần</option>
        </select>
        <button class="btn btn-outline-secondary" @click="clearFilters">
          <i class="bi bi-arrow-clockwise"></i>
        </button>
      </div>
    </div>

    <!-- Inventory Table -->
    <div class="inventory-table">
      <div class="table-header">
        <h5>Danh sách tồn kho</h5>
        <div class="table-stats">
          Hiển thị {{ filteredInventory.length }} / {{ inventoryItems.length }} biến thể
        </div>
      </div>

      <div class="table-responsive">
        <table class="table table-hover">
          <thead>
            <tr>
              <th>
                <input type="checkbox" class="form-check-input" v-model="selectAll" @change="toggleSelectAll">
              </th>
              <th>Sản phẩm</th>
              <th>Biến thể</th>
              <th>SKU</th>
              <th>Tồn kho</th>
              <th>Trạng thái</th>
              <th>Giá trị tồn</th>
              <th>Cập nhật gần nhất</th>
              <th>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="isLoadingInventory">
              <td colspan="9" class="text-center py-4">
                <div class="loading-state">
                  <i class="bi bi-arrow-repeat spin me-2"></i>
                  Đang tải dữ liệu...
                </div>
              </td>
            </tr>
            <tr v-else-if="paginatedInventory.length === 0">
              <td colspan="9" class="text-center py-4">
                <div class="empty-state">
                  Không có dữ liệu tồn kho
                </div>
              </td>
            </tr>
            <tr v-else v-for="item in paginatedInventory" :key="item.id">
              <td>
                <input type="checkbox" class="form-check-input" v-model="selectedItems" :value="item.id">
              </td>
              <td>
                <div class="product-cell">
                  <img :src="item.productImage" :alt="item.productName" class="product-thumbnail" />
                  <div class="product-info">
                    <div class="product-name">{{ item.productName }}</div>
                    <div class="product-sku">{{ item.productSku }}</div>
                  </div>
                </div>
              </td>
              <td>
                <div class="variant-cell">
                  <div class="variant-info">
                    <span class="variant-size-badge">{{ item.size }}</span>
                    <span class="variant-color">
                      <span class="color-dot" :style="{ backgroundColor: item.colorHex }"></span>
                      {{ item.color }}
                    </span>
                  </div>
                  <div class="variant-material">{{ item.material }}</div>
                </div>
              </td>
              <td>
                <code class="sku-code">{{ item.variantSku }}</code>
              </td>
              <td>
                <div class="stock-cell">
                  <div class="stock-value" :class="getStockClass(item.stock)">
                    {{ item.stock }}
                  </div>
                  <div class="stock-indicator" :class="getStockIndicatorClass(item.stock)"></div>
                </div>
              </td>
              <td>
                <span :class="['status-badge', getStatusBadgeClass(item.stock)]">
                  {{ getStatusText(item.stock) }}
                </span>
              </td>
              <td>
                <div class="value-cell">
                  <strong>{{ formatCurrency(item.stock * item.price) }}</strong>
                </div>
              </td>
              <td>
                <div class="date-cell">
                  <div>{{ formatDate(item.lastUpdated) }}</div>
                  <div class="time-text">{{ formatTime(item.lastUpdated) }}</div>
                </div>
              </td>
              <td>
                <div class="action-buttons">
                  <button class="btn btn-sm btn-outline-primary" @click="editStock(item)" title="Cập nhật tồn kho">
                    <i class="bi bi-pencil"></i>
                  </button>
                  <button class="btn btn-sm btn-outline-info" @click="viewHistory(item)" title="Lịch sử nhập xuất">
                    <i class="bi bi-clock-history"></i>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="pagination-section">
        <div class="row align-items-center">
          <div class="col-md-6">
            <div class="pagination-info">
              Hiển thị {{ (currentPage - 1) * itemsPerPage + 1 }} - {{ Math.min(currentPage * itemsPerPage, totalItems) }} 
              trong tổng số {{ totalItems }} biến thể
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
    </div>

    <!-- Quick Update Stock Modal -->
    <div v-if="editingItem" class="modal-overlay" @click="closeEditModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h5 class="modal-title">Cập nhật tồn kho</h5>
          <button class="btn-close" @click="closeEditModal">
            <i class="bi bi-x"></i>
          </button>
        </div>
        <div class="modal-body">
          <div class="edit-preview">
            <img :src="editingItem.productImage" :alt="editingItem.productName" />
            <div class="edit-info">
              <h6>{{ editingItem.productName }}</h6>
              <p>Size {{ editingItem.size }} - {{ editingItem.color }}</p>
              <code>{{ editingItem.variantSku }}</code>
            </div>
          </div>

          <div class="stock-update-form">
            <label class="form-label">Tồn kho hiện tại: <strong class="text-primary">{{ editingItem.stock }}</strong></label>
            <div class="row g-3">
              <div class="col-6">
                <label class="form-label">Nhập thêm</label>
                <input type="number" class="form-control" v-model.number="stockToAdd" min="0" placeholder="0">
              </div>
              <div class="col-6">
                <label class="form-label">Xuất/Điều chỉnh</label>
                <input type="number" class="form-control" v-model.number="stockToRemove" min="0" placeholder="0">
              </div>
              <div class="col-12">
                <div class="stock-preview">
                  <span>Tồn kho mới: </span>
                  <strong :class="getStockClass(newStock)">{{ newStock }}</strong>
                </div>
              </div>
              <div class="col-12">
                <label class="form-label">Ghi chú</label>
                <textarea class="form-control" rows="2" v-model="stockNote" placeholder="Lý do nhập/xuất kho..."></textarea>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" @click="closeEditModal">Hủy</button>
          <button type="button" class="btn btn-primary" @click="saveStockUpdate">
            <i class="bi bi-check me-2"></i>
            Lưu thay đổi
          </button>
        </div>
      </div>
    </div>

    <!-- Low Stock Alerts Modal -->
    <div v-if="showLowStockModal" class="modal-overlay" @click="showLowStockModal = false">
      <div class="modal-content modal-large" @click.stop>
        <div class="modal-header">
          <h5 class="modal-title">
            <i class="bi bi-exclamation-triangle text-warning me-2"></i>
            Danh sách tồn kho sắp hết
          </h5>
          <button class="btn-close" @click="showLowStockModal = false">
            <i class="bi bi-x"></i>
          </button>
        </div>
        <div class="modal-body">
          <div class="low-stock-list">
            <div v-for="alert in lowStockAlerts" :key="alert.id" class="low-stock-item">
              <div class="item-product">
                <img :src="alert.productImage" :alt="alert.productName" />
                <div class="item-info">
                  <div class="item-name">{{ alert.productName }}</div>
                  <div class="item-variant">Size {{ alert.size }} - {{ alert.color }}</div>
                  <code>{{ alert.variantSku }}</code>
                </div>
              </div>
              <div class="item-stock">
                <span class="stock-badge" :class="getStockClass(alert.stock)">
                  {{ alert.stock }} sản phẩm
                </span>
              </div>
              <button class="btn btn-sm btn-primary" @click="quickUpdateStock(alert)">
                <i class="bi bi-plus-circle me-1"></i>
                Nhập hàng
              </button>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" @click="showLowStockModal = false">Đóng</button>
          <button type="button" class="btn btn-success" @click="bulkRestock">
            <i class="bi bi-arrow-repeat me-2"></i>
            Nhập hàng hàng loạt
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import variantService from '@/services/variantService'

// Reactive data
const searchQuery = ref('')
const stockFilter = ref('')
const categoryFilter = ref('')
const currentPage = ref(1)
const itemsPerPage = 20
const selectAll = ref(false)
const selectedItems = ref([])

const editingItem = ref(null)
const stockToAdd = ref(0)
const stockToRemove = ref(0)
const stockNote = ref('')
const showLowStockModal = ref(false)
const showBulkUpdateModal = ref(false)
const isLoadingInventory = ref(false)

// Inventory data from API
const inventoryItems = ref([])

// Computed
const inventoryStats = computed(() => ({
  totalVariants: inventoryItems.value.length,
  inStock: inventoryItems.value.filter(i => i.stock > 10).length,
  lowStock: inventoryItems.value.filter(i => i.stock > 0 && i.stock <= 10).length,
  outOfStock: inventoryItems.value.filter(i => i.stock === 0).length
}))

const lowStockAlerts = computed(() => {
  return inventoryItems.value.filter(i => i.stock > 0 && i.stock <= 10)
})

const filteredInventory = computed(() => {
  let filtered = inventoryItems.value

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(item =>
      item.productName.toLowerCase().includes(query) ||
      item.variantSku.toLowerCase().includes(query) ||
      item.productSku.toLowerCase().includes(query)
    )
  }

  if (stockFilter.value) {
    switch (stockFilter.value) {
      case 'in-stock':
        filtered = filtered.filter(item => item.stock > 10)
        break
      case 'low-stock':
        filtered = filtered.filter(item => item.stock > 0 && item.stock <= 10)
        break
      case 'out-of-stock':
        filtered = filtered.filter(item => item.stock === 0)
        break
    }
  }

  if (categoryFilter.value) {
    filtered = filtered.filter(item => {
      if (categoryFilter.value === 'ao') {
        return item.categoryName && item.categoryName.toLowerCase().includes('áo')
      } else if (categoryFilter.value === 'quan') {
        return item.categoryName && item.categoryName.toLowerCase().includes('quần')
      }
      return true
    })
  }

  return filtered
})

const totalItems = computed(() => filteredInventory.value.length)
const totalPages = computed(() => Math.ceil(totalItems.value / itemsPerPage))

const paginatedInventory = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage
  const end = start + itemsPerPage
  return filteredInventory.value.slice(start, end)
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

const newStock = computed(() => {
  if (!editingItem.value) return 0
  return editingItem.value.stock + (stockToAdd.value || 0) - (stockToRemove.value || 0)
})

// Methods
const getStockClass = (stock) => {
  if (stock === 0) return 'stock-zero'
  if (stock <= 5) return 'stock-critical'
  if (stock <= 10) return 'stock-low'
  if (stock <= 20) return 'stock-medium'
  return 'stock-high'
}

const getStockIndicatorClass = (stock) => {
  if (stock === 0) return 'indicator-gray'
  if (stock <= 5) return 'indicator-red'
  if (stock <= 10) return 'indicator-yellow'
  return 'indicator-green'
}

const getStatusBadgeClass = (stock) => {
  if (stock === 0) return 'bg-secondary'
  if (stock <= 10) return 'bg-warning'
  return 'bg-success'
}

const getStatusText = (stock) => {
  if (stock === 0) return 'Hết hàng'
  if (stock <= 10) return 'Sắp hết'
  return 'Còn hàng'
}

const formatCurrency = (amount) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount)
}

const formatDate = (date) => {
  return new Intl.DateTimeFormat('vi-VN').format(date)
}

const formatTime = (date) => {
  return new Intl.DateTimeFormat('vi-VN', { hour: '2-digit', minute: '2-digit' }).format(date)
}

const clearFilters = () => {
  searchQuery.value = ''
  stockFilter.value = ''
  categoryFilter.value = ''
}

const toggleSelectAll = () => {
  if (selectAll.value) {
    selectedItems.value = paginatedInventory.value.map(i => i.id)
  } else {
    selectedItems.value = []
  }
}

const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
  }
}

const editStock = (item) => {
  editingItem.value = { ...item }
  stockToAdd.value = 0
  stockToRemove.value = 0
  stockNote.value = ''
}

const quickUpdateStock = (item) => {
  editStock(item)
}

const closeEditModal = () => {
  editingItem.value = null
  stockToAdd.value = 0
  stockToRemove.value = 0
  stockNote.value = ''
}

// Load inventory items from API
const loadInventoryItems = async () => {
  try {
    isLoadingInventory.value = true
    console.log('📦 Loading inventory items...')
    const response = await variantService.getAllInventoryItems()
    const data = response?.data ?? response
    console.log('📦 Inventory response:', data)
    
    // Map API data to component format
    inventoryItems.value = (data || []).map(item => ({
      id: item.id,
      productId: item.productId,
      productName: item.productName || 'N/A',
      productSku: item.productSku || '',
      productImage: item.productImage || item.imageUrl || 'https://via.placeholder.com/50x50/e2e8f0/94a3b8?text=SP',
      categoryName: item.categoryName || '',
      size: item.size || 'N/A',
      color: item.color || 'N/A',
      colorHex: item.colorHex || '#808080',
      material: item.material || 'N/A',
      variantSku: item.variantSku || '',
      stock: item.stock ?? 0,
      price: item.price ? Number(item.price) : (item.productPrice ? Number(item.price) : 0),
      lastUpdated: item.lastUpdated ? new Date(item.lastUpdated) : new Date()
    }))
    
    console.log('✅ Inventory items loaded:', inventoryItems.value.length)
  } catch (error) {
    console.error('❌ Error loading inventory items:', error)
    console.error('❌ Error details:', error.response?.data || error.message)
    inventoryItems.value = []
    alert('Lỗi khi tải dữ liệu tồn kho. Vui lòng thử lại sau.')
  } finally {
    isLoadingInventory.value = false
  }
}

const saveStockUpdate = async () => {
  if (!editingItem.value) return
  
  try {
    const newStockValue = newStock.value
    if (newStockValue < 0) {
      alert('Tồn kho không thể âm!')
      return
    }
    
    console.log('💾 Updating stock...', { id: editingItem.value.id, stock: newStockValue })
    const response = await variantService.updateStock(editingItem.value.id, newStockValue)
    console.log('✅ Stock updated:', response)
    
    // Update local data
    const item = inventoryItems.value.find(i => i.id === editingItem.value.id)
    if (item) {
      item.stock = newStockValue
      item.lastUpdated = new Date()
    }
    
    alert(`Đã cập nhật tồn kho!\nSKU: ${editingItem.value.variantSku}\nTồn kho mới: ${newStockValue}`)
    closeEditModal()
  } catch (error) {
    console.error('❌ Error updating stock:', error)
    alert('Lỗi khi cập nhật tồn kho. Vui lòng thử lại.')
  }
}

const viewHistory = (item) => {
  alert(`Xem lịch sử nhập xuất kho cho ${item.variantSku}\n(Tính năng đang phát triển)`)
}

const exportInventory = () => {
  // Export to Excel/CSV
  const data = filteredInventory.value.map(item => ({
    'Mã biến thể': item.variantSku,
    'Tên sản phẩm': item.productName,
    'Size': item.size,
    'Màu sắc': item.color,
    'Chất liệu': item.material,
    'Tồn kho': item.stock,
    'Giá': item.price,
    'Cập nhật': formatDate(item.lastUpdated)
  }))
  
  console.log('Export data:', data)
  alert('Tính năng xuất Excel - Coming soon!\nDữ liệu đã log ra console.')
}

const bulkRestock = () => {
  alert('Nhập hàng hàng loạt - Coming soon!')
}

// Load data on mount
onMounted(() => {
  loadInventoryItems()
})
</script>

<style scoped>
.admin-inventory {
  width: 100%;
}

/* Page Header */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.page-title {
  font-size: 2rem;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 0.5rem;
}

.page-subtitle {
  color: #64748b;
  margin: 0;
}

.header-right {
  display: flex;
  gap: 1rem;
}

/* Inventory Stats */
.inventory-stats {
  margin-bottom: 2rem;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  border: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  gap: 1rem;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-card.total {
  border-left: 4px solid #6366f1;
}

.stat-card.in-stock {
  border-left: 4px solid #10b981;
}

.stat-card.low-stock {
  border-left: 4px solid #f59e0b;
}

.stat-card.out-stock {
  border-left: 4px solid #ef4444;
}

.stat-icon {
  width: 3rem;
  height: 3rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
}

.stat-card.total .stat-icon {
  background: rgba(99, 102, 241, 0.1);
  color: #6366f1;
}

.stat-card.in-stock .stat-icon {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.stat-card.low-stock .stat-icon {
  background: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
}

.stat-card.out-stock .stat-icon {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 2rem;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 0.25rem;
}

.stat-label {
  color: #64748b;
  font-size: 0.9rem;
}

.stat-progress {
  margin-top: 0.5rem;
}

.progress-mini {
  height: 4px;
  background: #e2e8f0;
  border-radius: 2px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  transition: width 0.3s ease;
}

/* Alerts Section */
.alerts-section {
  margin-bottom: 2rem;
}

.alert-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  border: 1px solid #fde047;
  border-left: 4px solid #f59e0b;
  box-shadow: 0 2px 8px rgba(245, 158, 11, 0.1);
}

.alert-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.alert-header h5 {
  margin: 0;
  font-weight: 600;
  color: #854d0e;
}

.alerts-preview {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.alert-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: #fffbeb;
  border: 1px solid #fde047;
  border-radius: 8px;
}

.alert-product {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex: 1;
}

.alert-product-img {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  object-fit: cover;
}

.alert-product-name {
  font-weight: 600;
  color: #1e293b;
}

.alert-variant {
  font-size: 0.85rem;
  color: #64748b;
}

.alert-stock {
  font-size: 0.9rem;
  font-weight: 600;
}

.alert-more {
  text-align: center;
  padding: 0.75rem;
  color: #64748b;
  font-style: italic;
}

/* Filters */
.filters-section {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 2rem;
  border: 1px solid #e2e8f0;
}

.search-row {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.search-box {
  flex: 1;
  position: relative;
}

.search-icon {
  position: absolute;
  left: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
  color: #64748b;
  z-index: 1;
}

.search-input {
  padding-left: 2.5rem;
}

/* Inventory Table */
.inventory-table {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  border: 1px solid #e2e8f0;
  margin-bottom: 2rem;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e2e8f0;
}

.table-header h5 {
  margin: 0;
  font-weight: 600;
  color: #1e293b;
}

.table-stats {
  color: #64748b;
  font-size: 0.9rem;
}

.table {
  margin-bottom: 0;
}

.table th {
  background: #f8fafb;
  font-weight: 600;
  color: #475569;
  padding: 1rem 0.75rem;
  border-bottom: 2px solid #e2e8f0;
}

.table td {
  padding: 1rem 0.75rem;
  vertical-align: middle;
}

/* Table Cells */
.product-cell {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.product-thumbnail {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  object-fit: cover;
  border: 1px solid #e2e8f0;
}

.product-name {
  font-weight: 600;
  color: #1e293b;
}

.product-sku {
  font-size: 0.85rem;
  color: #64748b;
}

.variant-cell {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.variant-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.variant-size-badge {
  padding: 0.25rem 0.6rem;
  background: #eef2ff;
  border: 1px solid #c7d2fe;
  border-radius: 6px;
  color: #4338ca;
  font-weight: 700;
  font-size: 0.85rem;
}

.variant-color {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  font-size: 0.85rem;
  color: #475569;
}

.color-dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 2px solid white;
  box-shadow: 0 0 0 1px #cbd5e1;
}

.variant-material {
  font-size: 0.8rem;
  color: #94a3b8;
}

.sku-code {
  background: #f1f5f9;
  padding: 0.35rem 0.6rem;
  border-radius: 6px;
  font-size: 0.8rem;
  color: #475569;
  font-family: 'Courier New', monospace;
}

.stock-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.stock-value {
  font-weight: 700;
  font-size: 1.1rem;
}

.stock-zero { color: #94a3b8; }
.stock-critical { color: #ef4444; }
.stock-low { color: #f59e0b; }
.stock-medium { color: #3b82f6; }
.stock-high { color: #10b981; }

.stock-indicator {
  width: 40px;
  height: 4px;
  border-radius: 2px;
}

.indicator-gray { background: #cbd5e1; }
.indicator-red { background: #ef4444; }
.indicator-yellow { background: #f59e0b; }
.indicator-green { background: #10b981; }

.status-badge {
  padding: 0.35rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
  color: white;
}

.value-cell strong {
  color: #10b981;
}

.date-cell {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.time-text {
  font-size: 0.8rem;
  color: #94a3b8;
}

.action-buttons {
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
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-content.modal-large {
  max-width: 800px;
}

.modal-header {
  padding: 1.5rem;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-title {
  margin: 0;
  font-weight: 600;
  color: #1e293b;
}

.btn-close {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: #64748b;
  cursor: pointer;
}

.modal-body {
  padding: 1.5rem;
}

.modal-footer {
  padding: 1.5rem;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
}

/* Edit Preview */
.edit-preview {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: #f8fafb;
  border-radius: 8px;
  margin-bottom: 1.5rem;
}

.edit-preview img {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
}

.edit-info h6 {
  margin: 0 0 0.5rem 0;
  font-weight: 600;
}

.edit-info p {
  margin: 0.25rem 0;
  color: #64748b;
  font-size: 0.9rem;
}

.edit-info code {
  font-size: 0.8rem;
}

.stock-update-form {
  margin-top: 1rem;
}

.stock-preview {
  padding: 1rem;
  background: #eef2ff;
  border-radius: 8px;
  text-align: center;
  font-size: 1.1rem;
}

.stock-preview strong {
  font-size: 1.5rem;
  margin-left: 0.5rem;
}

/* Low Stock List */
.low-stock-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.low-stock-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: #fffbeb;
  border: 1px solid #fde047;
  border-radius: 8px;
}

.item-product {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex: 1;
}

.item-product img {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  object-fit: cover;
}

.item-name {
  font-weight: 600;
  color: #1e293b;
}

.item-variant {
  font-size: 0.85rem;
  color: #64748b;
  margin-top: 0.25rem;
}

.item-stock {
  text-align: center;
}

.stock-badge {
  padding: 0.5rem 1rem;
  border-radius: 8px;
  font-weight: 600;
}

/* Pagination */
.pagination-section {
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid #e2e8f0;
}

.pagination-info {
  color: #64748b;
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
    flex-direction: column;
  }

  .alert-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .action-buttons {
    width: 100%;
  }
}
</style>

