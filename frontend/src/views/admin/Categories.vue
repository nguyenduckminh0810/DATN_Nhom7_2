<template>
  <div class="admin-categories">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">Quản lý danh mục</h1>
        <p class="page-subtitle">Quản lý danh mục sản phẩm của cửa hàng</p>
      </div>
      <div class="header-right">
        <button class="btn btn-auro-primary" @click="showAddModal = true">
          <i class="ph-plus me-2"></i>Thêm danh mục
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
            placeholder="Tìm kiếm danh mục, mô tả..."
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
            <label class="form-label">Trạng thái</label>
            <select class="form-select" v-model="selectedStatus">
              <option value="">Tất cả trạng thái</option>
              <option value="active">Hoạt động</option>
              <option value="inactive">Không hoạt động</option>
            </select>
          </div>
          <div class="col-md-3">
            <label class="form-label">Cấp độ</label>
            <select class="form-select" v-model="selectedLevel">
              <option value="">Tất cả cấp độ</option>
              <option value="1">Cấp 1</option>
              <option value="2">Cấp 2</option>
              <option value="3">Cấp 3</option>
            </select>
          </div>
          <div class="col-md-3">
            <label class="form-label">Sắp xếp</label>
            <select class="form-select" v-model="sortBy">
              <option value="name-asc">Tên A-Z</option>
              <option value="name-desc">Tên Z-A</option>
              <option value="products-high">Sản phẩm nhiều nhất</option>
              <option value="products-low">Sản phẩm ít nhất</option>
              <option value="newest">Mới nhất</option>
              <option value="oldest">Cũ nhất</option>
            </select>
          </div>
          <div class="col-md-3">
            <label class="form-label">Hiển thị</label>
            <select class="form-select" v-model="viewMode">
              <option value="tree">Tree View</option>
              <option value="table">Table View</option>
              <option value="grid">Grid View</option>
            </select>
          </div>
        </div>
      </div>
    </div>

    <!-- Category Statistics -->
    <div class="category-stats-section">
      <div class="row g-3">
        <div class="col-md-3">
          <div class="stat-card total">
            <div class="stat-icon">
              <i class="ph-folder"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ categoryStats.total }}</div>
              <div class="stat-label">Tổng danh mục</div>
            </div>
          </div>
        </div>
        <div class="col-md-3">
          <div class="stat-card active">
            <div class="stat-icon">
              <i class="ph-check-circle"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ categoryStats.active }}</div>
              <div class="stat-label">Hoạt động</div>
            </div>
          </div>
        </div>
        <div class="col-md-3">
          <div class="stat-card products">
            <div class="stat-icon">
              <i class="ph-package"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ categoryStats.totalProducts }}</div>
              <div class="stat-label">Tổng sản phẩm</div>
            </div>
          </div>
        </div>
        <div class="col-md-3">
          <div class="stat-card levels">
            <div class="stat-icon">
              <i class="ph-tree"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ categoryStats.maxLevel }}</div>
              <div class="stat-label">Cấp độ tối đa</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Categories Tree View -->
    <div v-if="viewMode === 'tree'" class="categories-tree">
      <div class="tree-header">
        <h3>Cây danh mục</h3>
        <div class="tree-actions">
          <button class="btn btn-sm btn-outline-secondary" @click="expandAll">
            <i class="ph-arrows-out me-1"></i>Mở rộng tất cả
          </button>
          <button class="btn btn-sm btn-outline-secondary" @click="collapseAll">
            <i class="ph-arrows-in me-1"></i>Thu gọn tất cả
          </button>
          <button class="btn btn-sm btn-outline-primary" @click="toggleDragMode">
            <i :class="dragMode ? 'ph-hand-grabbing' : 'ph-hand'" class="me-1"></i>
            {{ dragMode ? 'Tắt' : 'Bật' }} kéo thả
          </button>
        </div>
      </div>

      <div class="tree-content">
        <div 
          class="tree-list"
          :class="{ 'drag-mode': dragMode }"
          @dragover.prevent
          @drop="handleDrop"
        >
          <div
            v-for="category in filteredRootCategories"
            :key="category.id"
            class="tree-node"
            :draggable="dragMode"
            @dragstart="handleDragStart(category)"
            @dragend="handleDragEnd"
          >
            <div class="node-content" :class="{ expanded: category.expanded }">
              <button 
                class="expand-btn" 
                @click="toggleCategory(category)" 
                v-if="category.children && category.children.length > 0"
              >
                <i :class="category.expanded ? 'ph-caret-down' : 'ph-caret-right'"></i>
              </button>
              <div class="node-info">
                <div class="node-icon">
                  <i :class="category.icon || 'ph-folder'"></i>
                </div>
                <div class="node-details">
                  <div class="node-name">{{ category.name }}</div>
                  <div class="node-meta">{{ category.productCount || 0 }} sản phẩm</div>
                </div>
              </div>
              <div class="node-actions">
                <button class="btn btn-sm btn-outline-primary" @click="editCategory(category)" title="Chỉnh sửa">
                  <i class="ph-pencil"></i>
                </button>
                <button class="btn btn-sm btn-outline-success" @click="addSubCategory(category)" title="Thêm danh mục con">
                  <i class="ph-plus"></i>
                </button>
                <button class="btn btn-sm btn-outline-danger" @click="deleteCategory(category)" title="Xóa">
                  <i class="ph-trash"></i>
                </button>
              </div>
            </div>
            <div v-if="category.expanded && category.children" class="node-children">
              <div
                v-for="child in category.children"
                :key="child.id"
                class="tree-node"
                :style="{ paddingLeft: '20px' }"
              >
                <div class="node-content">
                  <div class="node-info">
                    <div class="node-icon">
                      <i :class="child.icon || 'ph-folder'"></i>
                    </div>
                    <div class="node-details">
                      <div class="node-name">{{ child.name }}</div>
                      <div class="node-meta">{{ child.productCount || 0 }} sản phẩm</div>
                    </div>
                  </div>
                  <div class="node-actions">
                    <button class="btn btn-sm btn-outline-primary" @click="editCategory(child)" title="Chỉnh sửa">
                      <i class="ph-pencil"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-danger" @click="deleteCategory(child)" title="Xóa">
                      <i class="ph-trash"></i>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Categories Table -->
    <div v-if="viewMode === 'table'" class="categories-table">
      <div class="table-header">
        <h3>Danh sách danh mục</h3>
        <div class="table-actions">
          <div class="bulk-actions" v-if="selectedCategories.length > 0">
            <span class="selected-count">{{ selectedCategories.length }} đã chọn</span>
            <button class="btn btn-sm btn-outline-success" @click="bulkUpdateStatus('active')">
              <i class="ph-check me-1"></i>Kích hoạt
            </button>
            <button class="btn btn-sm btn-outline-warning" @click="bulkUpdateStatus('inactive')">
              <i class="ph-x me-1"></i>Vô hiệu hóa
            </button>
            <button class="btn btn-sm btn-outline-danger" @click="bulkDelete">
              <i class="ph-trash me-1"></i>Xóa
            </button>
          </div>
          <div class="table-stats">
            <span class="stats-text">
              Hiển thị {{ filteredCategories.length }} / {{ categories.length }} danh mục
            </span>
          </div>
        </div>
      </div>

      <div class="table-responsive">
        <table class="table table-hover">
          <thead>
            <tr>
              <th>
                <input type="checkbox" class="form-check-input" v-model="selectAll" @change="toggleSelectAll">
              </th>
              <th>Tên danh mục</th>
              <th>Slug</th>
              <th>Danh mục cha</th>
              <th>Số sản phẩm</th>
              <th>Thứ tự</th>
              <th>Trạng thái</th>
              <th>Ngày tạo</th>
              <th>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="category in filteredCategories" :key="category.id">
              <td>
                <input type="checkbox" class="form-check-input" v-model="selectedCategories" :value="category.id">
              </td>
              <td>
                <div class="category-info">
                  <div class="category-icon">
                    <i :class="category.icon || 'ph-folder'"></i>
                  </div>
                  <div class="category-details">
                    <div class="category-name">{{ category.name }}</div>
                    <div class="category-description">{{ category.description }}</div>
                  </div>
                </div>
              </td>
              <td>
                <code class="category-slug">{{ category.slug }}</code>
              </td>
              <td>
                <span v-if="category.parent" class="parent-category">
                  {{ getCategoryName(category.parent) }}
                </span>
                <span v-else class="text-muted">Danh mục gốc</span>
              </td>
              <td>
                <span class="product-count">{{ category.productCount }}</span>
              </td>
              <td>
                <span class="order-number">{{ category.order }}</span>
              </td>
              <td>
                <span :class="['status-badge', getStatusClass(category.status)]">
                  {{ getStatusText(category.status) }}
                </span>
              </td>
              <td>{{ formatDate(category.createdAt) }}</td>
              <td>
                <div class="action-buttons">
                  <button class="btn btn-sm btn-outline-primary" @click="editCategory(category)" title="Chỉnh sửa">
                    <i class="ph-pencil"></i>
                  </button>
                  <button class="btn btn-sm btn-outline-success" @click="addSubCategory(category)" title="Thêm danh mục con">
                    <i class="ph-plus"></i>
                  </button>
                  <button class="btn btn-sm btn-outline-danger" @click="deleteCategory(category)" title="Xóa">
                    <i class="ph-trash"></i>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Categories Grid View -->
    <div v-if="viewMode === 'grid'" class="categories-grid">
      <div class="grid-header">
        <h3>Danh mục dạng lưới</h3>
        <div class="grid-actions">
          <div class="bulk-actions" v-if="selectedCategories.length > 0">
            <span class="selected-count">{{ selectedCategories.length }} đã chọn</span>
            <button class="btn btn-sm btn-outline-success" @click="bulkUpdateStatus('active')">
              <i class="ph-check me-1"></i>Kích hoạt
            </button>
            <button class="btn btn-sm btn-outline-warning" @click="bulkUpdateStatus('inactive')">
              <i class="ph-x me-1"></i>Vô hiệu hóa
            </button>
            <button class="btn btn-sm btn-outline-danger" @click="bulkDelete">
              <i class="ph-trash me-1"></i>Xóa
            </button>
          </div>
        </div>
      </div>
      <div class="grid-content">
        <div class="row g-4">
          <div v-for="category in filteredCategories" :key="category.id" class="col-lg-3 col-md-4 col-sm-6">
            <div class="category-card">
              <div class="card-header">
                <input type="checkbox" class="form-check-input" v-model="selectedCategories" :value="category.id">
                <div class="category-badges">
                  <span v-if="category.isNew" class="badge bg-success">Mới</span>
                  <span v-if="category.isPopular" class="badge bg-warning">Phổ biến</span>
                </div>
              </div>
              <div class="card-body">
                <div class="category-icon">
                  <i :class="category.icon || 'ph-folder'"></i>
                </div>
                <h6 class="category-title">{{ category.name }}</h6>
                <p class="category-description">{{ category.description }}</p>
                <div class="category-meta">
                  <div class="meta-item">
                    <i class="ph-package"></i>
                    <span>{{ category.productCount }} sản phẩm</span>
                  </div>
                  <div class="meta-item">
                    <i class="ph-tree"></i>
                    <span>Cấp {{ category.level }}</span>
                  </div>
                </div>
                <div class="category-status">
                  <span :class="['status-badge', getStatusClass(category.status)]">
                    {{ getStatusText(category.status) }}
                  </span>
                </div>
              </div>
              <div class="card-footer">
                <div class="category-actions">
                  <button class="btn btn-sm btn-outline-primary" @click="editCategory(category)">
                    <i class="ph-pencil"></i>
                  </button>
                  <button class="btn btn-sm btn-outline-success" @click="duplicateCategory(category)">
                    <i class="ph-copy"></i>
                  </button>
                  <button class="btn btn-sm btn-outline-info" @click="viewCategoryProducts(category)">
                    <i class="ph-eye"></i>
                  </button>
                  <button class="btn btn-sm btn-outline-danger" @click="deleteCategory(category)">
                    <i class="ph-trash"></i>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Bulk Actions -->
    <div v-if="selectedCategories.length > 0" class="bulk-actions">
      <div class="bulk-actions-content">
        <span class="selected-count">{{ selectedCategories.length }} danh mục đã chọn</span>
        <div class="bulk-buttons">
          <button class="btn btn-sm btn-outline-success" @click="bulkUpdateStatus('active')">
            <i class="ph-check me-1"></i>Kích hoạt
          </button>
          <button class="btn btn-sm btn-outline-warning" @click="bulkUpdateStatus('inactive')">
            <i class="ph-pause me-1"></i>Ngừng hoạt động
          </button>
          <button class="btn btn-sm btn-outline-danger" @click="bulkDelete">
            <i class="ph-trash me-1"></i>Xóa
          </button>
        </div>
      </div>
    </div>

    <!-- Add/Edit Category Modal -->
    <div v-if="showAddModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h5 class="modal-title">{{ editingCategory ? 'Chỉnh sửa danh mục' : 'Thêm danh mục mới' }}</h5>
          <button class="btn-close" @click="closeModal">
            <i class="ph-x"></i>
          </button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="saveCategory">
            <div class="row g-3">
              <div class="col-md-8">
                <label class="form-label">Tên danh mục *</label>
                <input type="text" class="form-control" v-model="categoryForm.name" required>
              </div>
              <div class="col-md-4">
                <label class="form-label">Icon</label>
                <select class="form-select" v-model="categoryForm.icon">
                  <option value="ph-folder">Thư mục</option>
                  <option value="ph-t-shirt">Áo</option>
                  <option value="ph-bag">Quần</option>
                  <option value="ph-watch">Đồng hồ</option>
                  <option value="ph-sunglasses">Kính</option>
                  <option value="ph-sneaker">Giày</option>
                </select>
              </div>
              <div class="col-md-6">
                <label class="form-label">Slug *</label>
                <input type="text" class="form-control" v-model="categoryForm.slug" required>
              </div>
              <div class="col-md-6">
                <label class="form-label">Danh mục cha</label>
                <select class="form-select" v-model="categoryForm.parentId">
                  <option value="">Danh mục gốc</option>
                  <option v-for="cat in availableParents" :key="cat.id" :value="cat.id">
                    {{ cat.name }}
                  </option>
                </select>
              </div>
              <div class="col-md-6">
                <label class="form-label">Thứ tự</label>
                <input type="number" class="form-control" v-model.number="categoryForm.order">
              </div>
              <div class="col-md-6">
                <label class="form-label">Trạng thái</label>
                <select class="form-select" v-model="categoryForm.status">
                  <option value="active">Đang hoạt động</option>
                  <option value="inactive">Ngừng hoạt động</option>
                </select>
              </div>
              <div class="col-12">
                <label class="form-label">Mô tả</label>
                <textarea class="form-control" rows="3" v-model="categoryForm.description"></textarea>
              </div>
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" @click="closeModal">Hủy</button>
          <button type="button" class="btn btn-auro-primary" @click="saveCategory">
            {{ editingCategory ? 'Cập nhật' : 'Thêm danh mục' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

// CategoryTreeNode component sẽ được định nghĩa inline trong template

// Reactive data
const searchQuery = ref('')
const selectedStatus = ref('')
const selectedCategories = ref([])
const selectAll = ref(false)
const showAddModal = ref(false)
const editingCategory = ref(null)

// Advanced filters
const showAdvancedFilters = ref(false)
const selectedLevel = ref('')
const sortBy = ref('name-asc')
const viewMode = ref('tree') // 'tree', 'table', 'grid'
const dragMode = ref(false)
const draggedCategory = ref(null)

const categoryForm = ref({
  name: '',
  slug: '',
  description: '',
  icon: 'ph-folder',
  parentId: '',
  order: 0,
  status: 'active'
})

// Mock data
const categories = ref([
  {
    id: 1,
    name: 'Áo',
    slug: 'ao',
    description: 'Các loại áo nam',
    icon: 'ph-t-shirt',
    parentId: null,
    order: 1,
    status: 'active',
    productCount: 25,
    createdAt: new Date('2024-01-15'),
    expanded: true,
    children: [
      {
        id: 11,
        name: 'Áo sơ mi',
        slug: 'ao-so-mi',
        description: 'Áo sơ mi nam',
        icon: 'ph-t-shirt',
        parentId: 1,
        order: 1,
        status: 'active',
        productCount: 15,
        createdAt: new Date('2024-01-16'),
        expanded: false,
        children: []
      },
      {
        id: 12,
        name: 'Áo thun',
        slug: 'ao-thun',
        description: 'Áo thun nam',
        icon: 'ph-t-shirt',
        parentId: 1,
        order: 2,
        status: 'active',
        productCount: 10,
        createdAt: new Date('2024-01-17'),
        expanded: false,
        children: []
      }
    ]
  },
  {
    id: 2,
    name: 'Quần',
    slug: 'quan',
    description: 'Các loại quần nam',
    icon: 'ph-bag',
    parentId: null,
    order: 2,
    status: 'active',
    productCount: 18,
    createdAt: new Date('2024-01-20'),
    expanded: false,
    children: [
      {
        id: 21,
        name: 'Quần âu',
        slug: 'quan-au',
        description: 'Quần âu nam',
        icon: 'ph-bag',
        parentId: 2,
        order: 1,
        status: 'active',
        productCount: 12,
        createdAt: new Date('2024-01-21'),
        expanded: false,
        children: []
      },
      {
        id: 22,
        name: 'Quần jean',
        slug: 'quan-jean',
        description: 'Quần jean nam',
        icon: 'ph-bag',
        parentId: 2,
        order: 2,
        status: 'active',
        productCount: 6,
        createdAt: new Date('2024-01-22'),
        expanded: false,
        children: []
      }
    ]
  },
  {
    id: 3,
    name: 'Phụ kiện',
    slug: 'phu-kien',
    description: 'Các phụ kiện nam',
    icon: 'ph-watch',
    parentId: null,
    order: 3,
    status: 'active',
    productCount: 12,
    createdAt: new Date('2024-01-25'),
    expanded: false,
    children: []
  }
])

// Computed
const categoryStats = computed(() => {
  const total = categories.value.length
  const active = categories.value.filter(cat => cat.status === 'active').length
  const totalProducts = categories.value.reduce((sum, cat) => sum + (cat.productCount || 0), 0)
  const maxLevel = Math.max(...categories.value.map(cat => cat.level || 1), 1)
  
  return { total, active, totalProducts, maxLevel }
})

const rootCategories = computed(() => {
  return categories.value.filter(cat => !cat.parentId)
})

const filteredRootCategories = computed(() => {
  let filtered = rootCategories.value

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(category =>
      category.name.toLowerCase().includes(query) ||
      category.description.toLowerCase().includes(query)
    )
  }

  if (selectedStatus.value) {
    filtered = filtered.filter(category => category.status === selectedStatus.value)
  }

  if (selectedLevel.value) {
    filtered = filtered.filter(category => category.level === parseInt(selectedLevel.value))
  }

  return filtered
})

const allCategories = computed(() => {
  const flatten = (cats) => {
    let result = []
    cats.forEach(cat => {
      result.push(cat)
      if (cat.children && cat.children.length > 0) {
        result = result.concat(flatten(cat.children))
      }
    })
    return result
  }
  return flatten(categories.value)
})

const filteredCategories = computed(() => {
  let filtered = allCategories.value

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(category =>
      category.name.toLowerCase().includes(query) ||
      category.slug.toLowerCase().includes(query) ||
      category.description.toLowerCase().includes(query)
    )
  }

  if (selectedStatus.value) {
    filtered = filtered.filter(category => category.status === selectedStatus.value)
  }

  return filtered
})

const availableParents = computed(() => {
  return allCategories.value.filter(cat => 
    !editingCategory.value || cat.id !== editingCategory.value.id
  )
})

// Methods
const formatDate = (date) => {
  return new Intl.DateTimeFormat('vi-VN').format(date)
}

const getCategoryName = (parentId) => {
  const category = allCategories.value.find(cat => cat.id === parentId)
  return category ? category.name : 'Không xác định'
}

const getStatusText = (status) => {
  const statuses = {
    'active': 'Đang hoạt động',
    'inactive': 'Ngừng hoạt động'
  }
  return statuses[status] || status
}

const getStatusClass = (status) => {
  const classes = {
    'active': 'bg-success',
    'inactive': 'bg-warning'
  }
  return classes[status] || 'bg-secondary'
}

const toggleSelectAll = () => {
  if (selectAll.value) {
    selectedCategories.value = filteredCategories.value.map(c => c.id)
  } else {
    selectedCategories.value = []
  }
}

const expandAll = () => {
  const expand = (cats) => {
    cats.forEach(cat => {
      cat.expanded = true
      if (cat.children && cat.children.length > 0) {
        expand(cat.children)
      }
    })
  }
  expand(categories.value)
}

const collapseAll = () => {
  const collapse = (cats) => {
    cats.forEach(cat => {
      cat.expanded = false
      if (cat.children && cat.children.length > 0) {
        collapse(cat.children)
      }
    })
  }
  collapse(categories.value)
}

const toggleAdvancedFilters = () => {
  showAdvancedFilters.value = !showAdvancedFilters.value
}

const clearFilters = () => {
  searchQuery.value = ''
  selectedStatus.value = ''
  selectedLevel.value = ''
  sortBy.value = 'name-asc'
}

const toggleDragMode = () => {
  dragMode.value = !dragMode.value
}

const handleDragStart = (category) => {
  draggedCategory.value = category
}

const handleDragEnd = () => {
  draggedCategory.value = null
}

const handleDrop = (event) => {
  event.preventDefault()
  if (draggedCategory.value) {
    // Handle drop logic here
    console.log('Dropped category:', draggedCategory.value)
  }
}

const bulkUpdateStatus = (status) => {
  if (confirm(`Bạn có chắc chắn muốn cập nhật trạng thái cho ${selectedCategories.value.length} danh mục?`)) {
    selectedCategories.value.forEach(categoryId => {
      const category = categories.value.find(c => c.id === categoryId)
      if (category) {
        category.status = status
      }
    })
    selectedCategories.value = []
  }
}

const bulkDelete = () => {
  if (confirm(`Bạn có chắc chắn muốn xóa ${selectedCategories.value.length} danh mục?`)) {
    categories.value = categories.value.filter(cat => !selectedCategories.value.includes(cat.id))
    selectedCategories.value = []
  }
}

const duplicateCategory = (category) => {
  const duplicatedCategory = {
    ...category,
    id: Date.now(),
    name: category.name + ' (Copy)',
    slug: category.slug + '-copy',
    createdAt: new Date()
  }
  categories.value.push(duplicatedCategory)
}

const viewCategoryProducts = (category) => {
  // Navigate to products filtered by category
  console.log('View products for category:', category)
}

const addSubCategory = (parentCategory) => {
  const newCategory = {
    id: Date.now(),
    name: 'Danh mục con mới',
    slug: 'sub-category-' + Date.now(),
    description: '',
    icon: 'ph-folder',
    level: (parentCategory.level || 0) + 1,
    parentId: parentCategory.id,
    status: 'active',
    productCount: 0,
    expanded: false,
    children: []
  }
  
  if (!parentCategory.children) {
    parentCategory.children = []
  }
  parentCategory.children.push(newCategory)
  parentCategory.expanded = true
  
  // Mở modal để chỉnh sửa danh mục con mới
  editCategory(newCategory)
}

const toggleCategory = (category) => {
  category.expanded = !category.expanded
}

const editCategory = (category) => {
  editingCategory.value = category
  categoryForm.value = { ...category }
  showAddModal.value = true
}


const deleteCategory = (category) => {
  if (confirm(`Bạn có chắc chắn muốn xóa danh mục "${category.name}"?`)) {
    // Remove from tree
    const removeFromTree = (cats) => {
      for (let i = 0; i < cats.length; i++) {
        if (cats[i].id === category.id) {
          cats.splice(i, 1)
          return true
        }
        if (cats[i].children && cats[i].children.length > 0) {
          if (removeFromTree(cats[i].children)) {
            return true
          }
        }
      }
      return false
    }
    removeFromTree(categories.value)
  }
}

const closeModal = () => {
  showAddModal.value = false
  editingCategory.value = null
  categoryForm.value = {
    name: '',
    slug: '',
    description: '',
    icon: 'ph-folder',
    parentId: '',
    order: 0,
    status: 'active'
  }
}

const saveCategory = () => {
  if (editingCategory.value) {
    // Update existing category
    const updateInTree = (cats) => {
      for (let cat of cats) {
        if (cat.id === editingCategory.value.id) {
          Object.assign(cat, categoryForm.value)
          return true
        }
        if (cat.children && cat.children.length > 0) {
          if (updateInTree(cat.children)) {
            return true
          }
        }
      }
      return false
    }
    updateInTree(categories.value)
  } else {
    // Add new category
    const newCategory = {
      ...categoryForm.value,
      id: Date.now(),
      productCount: 0,
      createdAt: new Date(),
      expanded: false,
      children: []
    }

    if (categoryForm.value.parentId) {
      // Add as child
      const addToParent = (cats) => {
        for (let cat of cats) {
          if (cat.id === categoryForm.value.parentId) {
            if (!cat.children) cat.children = []
            cat.children.push(newCategory)
            return true
          }
          if (cat.children && cat.children.length > 0) {
            if (addToParent(cat.children)) {
              return true
            }
          }
        }
        return false
      }
      addToParent(categories.value)
    } else {
      // Add as root
      categories.value.push(newCategory)
    }
  }
  
  closeModal()
}



// Lifecycle
onMounted(() => {
  console.log('Categories page loaded')
})
</script>

<style scoped>
.admin-categories {
  max-width: 1200px;
  margin: 0 auto;
}

/* Filters Section */
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

/* Category Statistics */
.category-stats-section {
  margin-bottom: 2rem;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 1rem;
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-card.total {
  border-left: 4px solid #3498db;
}

.stat-card.active {
  border-left: 4px solid #28a745;
}

.stat-card.products {
  border-left: 4px solid #ffc107;
}

.stat-card.levels {
  border-left: 4px solid #6f42c1;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  color: white;
}

.stat-card.total .stat-icon {
  background: #3498db;
}

.stat-card.active .stat-icon {
  background: #28a745;
}

.stat-card.products .stat-icon {
  background: #ffc107;
}

.stat-card.levels .stat-icon {
  background: #6f42c1;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 2rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 0.25rem;
}

.stat-label {
  color: #6c757d;
  font-size: 0.9rem;
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

/* Categories Tree */
.categories-tree {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.tree-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}

.tree-header h3 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.tree-actions {
  display: flex;
  gap: 0.5rem;
}

.tree-content {
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 1rem;
  background-color: #f8f9fa;
}

.tree-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.tree-node {
  border-radius: 8px;
  background: white;
  border: 1px solid #e9ecef;
  transition: all 0.3s ease;
}

.tree-node:hover {
  border-color: #3498db;
  box-shadow: 0 2px 8px rgba(52, 152, 219, 0.1);
}

.node-content {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem;
}

.expand-btn {
  background: none;
  border: none;
  color: #6c757d;
  cursor: pointer;
  padding: 0.25rem;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.expand-btn:hover {
  background-color: #f8f9fa;
  color: #3498db;
}

.node-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex: 1;
}

.node-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background-color: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
  color: #3498db;
}

.node-details {
  flex: 1;
}

.node-name {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 0.25rem;
}

.node-meta {
  font-size: 0.85rem;
  color: #6c757d;
}

.node-actions {
  display: flex;
  gap: 0.5rem;
}

.node-actions .btn {
  padding: 0.375rem 0.75rem;
}

.node-children {
  border-top: 1px solid #e9ecef;
  background-color: #f8f9fa;
}

/* Categories Table */
.categories-table {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}

.table-header h3 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.table-actions {
  display: flex;
  gap: 1rem;
  align-items: center;
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
  width: 250px;
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

.category-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.category-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background-color: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
  color: #3498db;
}

.category-name {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 0.25rem;
}

.category-description {
  font-size: 0.85rem;
  color: #6c757d;
}

.category-slug {
  background-color: #f8f9fa;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.85rem;
  color: #6c757d;
}

.parent-category {
  color: #6c757d;
  font-size: 0.9rem;
}

.product-count {
  font-weight: 600;
  color: #2c3e50;
}

.order-number {
  font-weight: 600;
  color: #6c757d;
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

/* Bulk Actions */
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
  
  .tree-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .table-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .table-actions {
    width: 100%;
    flex-direction: column;
  }
  
  .search-input {
    width: 100%;
  }
  
  .categories-table {
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
