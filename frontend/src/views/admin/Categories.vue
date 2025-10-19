<template>
  <div class="admin-categories">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">Quản lý danh mục</h1>
        <p class="page-subtitle">Quản lý danh mục sản phẩm của cửa hàng</p>
      </div>
      <div class="header-right">
        <button class="btn btn-auro-primary" @click="openCreateModal">
          <i class="bi bi-plus me-2"></i>Thêm danh mục
        </button>
      </div>
    </div>

    <!-- Advanced Filters and Search -->
    <div class="filters-section">
      <!-- Quick Search -->
      <div class="search-row">
        <div class="search-box">
          <i class="bi bi-search search-icon"></i>
          <input
            type="text"
            class="form-control search-input"
            placeholder="Tìm kiếm danh mục, mô tả..."
            v-model="searchQuery"
          />
        </div>
        <button class="btn btn-outline-primary" @click="toggleAdvancedFilters">
          <i class="bi bi-funnel me-1"></i>Bộ lọc nâng cao
          <i :class="showAdvancedFilters ? 'bi bi-caret-up' : 'bi bi-caret-down'" class="ms-1"></i>
        </button>
        <button class="btn btn-outline-secondary" @click="clearFilters">
          <i class="bi bi-arrow-clockwise me-1"></i>Xóa bộ lọc
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
              <i class="bi bi-folder"></i>
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
              <i class="bi bi-check-circle"></i>
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
              <i class="bi bi-box"></i>
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
              <i class="bi bi-tree"></i>
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
            <i class="bi bi-arrows-expand me-1"></i>Mở rộng tất cả
          </button>
          <button class="btn btn-sm btn-outline-secondary" @click="collapseAll">
            <i class="bi bi-arrows-collapse me-1"></i>Thu gọn tất cả
          </button>
          <button class="btn btn-sm btn-outline-primary" @click="toggleDragMode">
            <i :class="dragMode ? 'bi bi-hand-index' : 'bi bi-hand-index'" class="me-1"></i>
            {{ dragMode ? 'Tắt' : 'Bật' }} kéo thả
          </button>
        </div>
      </div>

      <div class="tree-content">
        <div class="tree-list" :class="{ 'drag-mode': dragMode }">
          <TreeNode
            v-for="category in filteredRootCategories"
            :key="category.id"
            :node="category"
            :dragMode="dragMode"
            @edit="editCategory"
            @add-child="addSubCategory"
            @delete="deleteCategory"
            @drag-start="handleDragStart"
            @drag-end="handleDragEnd"
            @drop="handleDrop"
            @toggle="toggleCategory"
          />
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
              <i class="bi bi-check me-1"></i>Kích hoạt
            </button>
            <button class="btn btn-sm btn-outline-warning" @click="bulkUpdateStatus('inactive')">
              <i class="bi bi-x me-1"></i>Vô hiệu hóa
            </button>
            <button class="btn btn-sm btn-outline-danger" @click="bulkDelete">
              <i class="bi bi-trash me-1"></i>Xóa
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
                    <i :class="category.icon || 'bi bi-folder'"></i>
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
                <span v-if="category.parentId !== null" class="parent-category">
                  {{ getCategoryName(category.parentId) }}
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
                  <button class="btn btn-sm btn-outline-primary" @click.stop="editCategory(category)" title="Chỉnh sửa">
                    <i class="bi bi-pencil"></i>
                  </button>
                  <button class="btn btn-sm btn-outline-success" @click.stop="addSubCategory(category)" title="Thêm danh mục con">
                    <i class="bi bi-plus"></i>
                  </button>
                  <button class="btn btn-sm btn-outline-danger" @click.stop="deleteCategory(category)" title="Xóa">
                    <i class="bi bi-trash"></i>
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
              <i class="bi bi-check me-1"></i>Kích hoạt
            </button>
            <button class="btn btn-sm btn-outline-warning" @click="bulkUpdateStatus('inactive')">
              <i class="bi bi-x me-1"></i>Vô hiệu hóa
            </button>
            <button class="btn btn-sm btn-outline-danger" @click="bulkDelete">
              <i class="bi bi-trash me-1"></i>Xóa
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
                  <i :class="category.icon || 'bi bi-folder'"></i>
                </div>
                <h6 class="category-title">{{ category.name }}</h6>
                <p class="category-description">{{ category.description }}</p>
                <div class="category-meta">
                  <div class="meta-item">
                    <i class="bi bi-box"></i>
                    <span>{{ category.productCount }} sản phẩm</span>
                  </div>
                  <div class="meta-item">
                    <i class="bi bi-tree"></i>
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
                  <button class="btn btn-sm btn-outline-primary" @click.stop="editCategory(category)">
                    <i class="bi bi-pencil"></i>
                  </button>
                  <button class="btn btn-sm btn-outline-success" @click.stop="duplicateCategory(category)">
                    <i class="bi bi-copy"></i>
                  </button>
                  <button class="btn btn-sm btn-outline-info" @click.stop="viewCategoryProducts(category)">
                    <i class="bi bi-eye"></i>
                  </button>
                  <button class="btn btn-sm btn-outline-danger" @click.stop="deleteCategory(category)">
                    <i class="bi bi-trash"></i>
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
            <i class="bi bi-check me-1"></i>Kích hoạt
          </button>
          <button class="btn btn-sm btn-outline-warning" @click="bulkUpdateStatus('inactive')">
            <i class="bi bi-pause me-1"></i>Ngừng hoạt động
          </button>
          <button class="btn btn-sm btn-outline-danger" @click="bulkDelete">
            <i class="bi bi-trash me-1"></i>Xóa
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
            <i class="bi bi-x"></i>
          </button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="saveCategory">
            <div class="row g-3">
              <div class="col-md-8">
                <label class="form-label">Tên danh mục </label>
                <input type="text" class="form-control" v-model="categoryForm.name" required>
              </div>
              <div class="col-md-4">
                <label class="form-label">Icon</label>
                <select class="form-select" v-model="categoryForm.icon">
                  <option value="bi bi-folder">Thư mục</option>
                  <option value="bi bi-tshirt">Áo</option>
                  <option value="bi bi-bag">Quần</option>
                  <option value="bi bi-watch">Đồng hồ</option>
                  <option value="bi bi-sunglasses">Kính</option>
                  <option value="bi bi-shoe-prints">Giày</option>
                </select>
              </div>
              <div class="col-md-6">
                <label class="form-label">Slug *</label>
                <input type="text" class="form-control" v-model="categoryForm.slug" required>
              </div>

              <div class="col-md-6">
                <label class="form-label">
                  Danh mục cha
                  <small v-if="currentParent" class="text-muted ms-1">— Đang thuộc: {{ currentParent.name }}</small>
                </label>
                <select class="form-select" v-model.number="categoryForm.parentId">
                  <option :value="null">Danh mục gốc</option>
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
          <button type="button" class="btn btn-auro-primary"
            @click="(editingCategory && !editingCategory.isLocal) ? updateCategory() : createCategory()">
            {{ (editingCategory && !editingCategory.isLocal) ? 'Cập nhật' : 'Thêm danh mục' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Confirm Modal -->
    <div v-if="showConfirmModal" class="modal-overlay" @click.self="confirmModalCancel">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h5 class="modal-title">Xác nhận</h5>
        </div>
        <div class="modal-body">
          <p>{{ confirmMessage }}</p>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="confirmModalCancel">Hủy</button>
          <button class="btn btn-auro-primary" @click="confirmModalConfirm">Xác nhận</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import apiService from '../../services/api'
import { useToast } from '../../composables/useToast'
import TreeNode from '../../components/admin/TreeNode.vue'

// ===== state =====
const searchQuery = ref('')
const selectedStatus = ref('')
const selectedCategories = ref([])
const selectAll = ref(false)
const showAddModal = ref(false)
const editingCategory = ref(null)

const showConfirmModal = ref(false)
const confirmMessage = ref('')
let confirmResolve = null

const toast = useToast()

const showConfirm = (message) => {
  confirmMessage.value = message
  showConfirmModal.value = true
  return new Promise((resolve) => { confirmResolve = resolve })
}
const confirmModalConfirm = () => { showConfirmModal.value = false; if (confirmResolve) confirmResolve(true);  confirmResolve = null }
const confirmModalCancel  = () => { showConfirmModal.value = false; if (confirmResolve) confirmResolve(false); confirmResolve = null }

// Filters
const showAdvancedFilters = ref(false)
const selectedLevel = ref('')
const sortBy = ref('name-asc')
const viewMode = ref('tree')
const dragMode = ref(false)
const draggedCategory = ref(null)

const categoryForm = ref({
  name: '',
  slug: '',
  description: '',
  icon: 'bi bi-folder',
  parentId: null,
  order: 0,
  status: 'active'
})

const buildPayload = () => ({
  ten: categoryForm.value.name?.trim(),
  slug: categoryForm.value.slug?.trim(),
  idCha: categoryForm.value.parentId ? Number(categoryForm.value.parentId) : null,
  thuTu: Number(categoryForm.value.order) || 0,
  // send numeric flag expected by backend (1 = active, 0 = inactive)
  hoatDong: categoryForm.value.status === 'active' ? 1 : 0
})

const categories = ref([])

// ===== computed =====
const rootCategories = computed(() => categories.value.filter(cat => !cat.parentId))

const allCategories = computed(() => {
  const flatten = (cats) => {
    const res = []
    cats.forEach(cat => {
      res.push(cat)
      if (cat.children?.length) res.push(...flatten(cat.children))
    })
    return res
  }
  return flatten(categories.value)
})

const currentParent = computed(() =>
  allCategories.value.find(c => c.id === categoryForm.value.parentId) || null
)

const categoryStats = computed(() => {
  const total = categories.value.length
  const active = allCategories.value.filter(cat => cat.status === 'active').length
  const totalProducts = allCategories.value.reduce((s, c) => s + (c.productCount || 0), 0)
  const maxLevel = Math.max(...allCategories.value.map(c => c.level || 1), 1)
  return { total, active, totalProducts, maxLevel }
})

const filteredRootCategories = computed(() => {
  let filtered = rootCategories.value
  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase()
    filtered = filtered.filter(c =>
      (c.name || '').toLowerCase().includes(q) ||
      (c.description || '').toLowerCase().includes(q)
    )
  }
  if (selectedStatus.value) filtered = filtered.filter(c => c.status === selectedStatus.value)
  if (selectedLevel.value)  filtered = filtered.filter(c => c.level === parseInt(selectedLevel.value))
  return filtered
})

const filteredCategories = computed(() => {
  let filtered = allCategories.value
  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase()
    filtered = filtered.filter(c =>
      (c.name || '').toLowerCase().includes(q) ||
      (c.slug || '').toLowerCase().includes(q) ||
      (c.description || '').toLowerCase().includes(q)
    )
  }
  if (selectedStatus.value) filtered = filtered.filter(c => c.status === selectedStatus.value)
  return filtered
})

const availableParents = computed(() =>
  allCategories.value.filter(cat => !editingCategory.value || cat.id !== editingCategory.value.id)
)

// ===== helpers & actions =====
const formatDate = (date) => new Intl.DateTimeFormat('vi-VN').format(date)
const getCategoryName = (parentId) => {
  const c = allCategories.value.find(cat => cat.id === parentId)
  return c ? c.name : 'Không xác định'
}
const getStatusText = (s) => ({ active:'Đang hoạt động', inactive:'Ngừng hoạt động' }[s] || s)
const getStatusClass = (s) => ({ active:'bg-success', inactive:'bg-warning' }[s] || 'bg-secondary')

const toggleSelectAll = () => {
  selectedCategories.value = selectAll.value ? filteredCategories.value.map(c => c.id) : []
}

const expandAll = () => {
  const walk = (cats) => cats.forEach(c => { c.expanded = true; if (c.children?.length) walk(c.children) })
  walk(categories.value)
}
const collapseAll = () => {
  const walk = (cats) => cats.forEach(c => { c.expanded = false; if (c.children?.length) walk(c.children) })
  walk(categories.value)
}
const toggleAdvancedFilters = () => { showAdvancedFilters.value = !showAdvancedFilters.value }
const clearFilters = () => { searchQuery.value=''; selectedStatus.value=''; selectedLevel.value=''; sortBy.value='name-asc' }
const toggleDragMode = () => { dragMode.value = !dragMode.value }
const handleDragStart = (cat) => { draggedCategory.value = cat }
const handleDragEnd = () => { draggedCategory.value = null }
const handleDrop = (e) => { e.preventDefault(); if (draggedCategory.value) { /* TODO: reorder */ } }

// add helper to open modal for creating
const openCreateModal = () => {
  editingCategory.value = null
  categoryForm.value = { name:'', slug:'', description:'', icon:'bi bi-folder', parentId:null, order:0, status:'active' }
  showAddModal.value = true
}

// replace unified saveCategory with createCategory + updateCategory
const createCategory = async () => {
  try {
    const payload = {
      ten: categoryForm.value.name?.trim(),
      slug: categoryForm.value.slug?.trim(),
      moTa: categoryForm.value.description?.trim(),
      icon: categoryForm.value.icon,
      idCha: categoryForm.value.parentId ? Number(categoryForm.value.parentId) : null,
      thuTu: Number(categoryForm.value.order) || 0,
      hoatDong: categoryForm.value.status === 'active' ? 1 : 0
    }
    console.info('Create payload:', payload)
    if (!payload.ten || !payload.slug) {
      toast.error('Tên và slug là bắt buộc')
      return
    }
    const res = await apiService.post('/danh-muc/create', payload)
    console.info('Create response:', res)
    toast.success('Tạo danh mục thành công')
    await loadCategories()
    window.dispatchEvent(new Event('categories-updated'))
    closeModal()
  } catch (err) {
    console.error('Create category error:', err, err?.response?.data)
    const detail = err?.response?.data || err?.message || 'Lỗi server'
    toast.error('Tạo thất bại: ' + (detail?.message || JSON.stringify(detail)))
  }
}

const updateCategory = async () => {
  // nếu đây là item local (chưa persist) => thực hiện create thay vì từ chối
  if (editingCategory.value?.isLocal) {
    return await createCategory()
  }

  if (!editingCategory.value) {
    toast.error('Không có danh mục để cập nhật')
    return
  }

  try {
    const payload = {
      ten: categoryForm.value.name?.trim(),
      slug: categoryForm.value.slug?.trim(),
      moTa: categoryForm.value.description?.trim(),
      icon: categoryForm.value.icon,
      idCha: categoryForm.value.parentId ? Number(categoryForm.value.parentId) : null,
      thuTu: Number(categoryForm.value.order) || 0,
      hoatDong: categoryForm.value.status === 'active' ? 1 : 0
    }
    console.info('Update payload:', payload)
    const res = await apiService.put(`/danh-muc/${editingCategory.value.id}`, payload)
    console.info('Update response:', res)
    toast.success('Cập nhật danh mục thành công')
    await loadCategories()
    window.dispatchEvent(new Event('categories-updated'))
    closeModal()
  } catch (err) {
    console.error('Update category error:', err, err?.response?.data)
    const detail = err?.response?.data || err?.message || 'Lỗi server'
    toast.error('Cập nhật thất bại: ' + (detail?.message || JSON.stringify(detail)))
  }
}

const deleteCategory = async (cat) => {
  const ok = await showConfirm(`Bạn có chắc chắn muốn xóa danh mục "${cat.name}"?`)
  if (!ok) return
  try {
    await apiService.categoriesDelete.delete(cat.id, false)
    await loadCategories()
    toast.success('Xóa danh mục thành công')
    // notify clients
    window.dispatchEvent(new Event('categories-updated'))
} catch (err) {
  const status = err?.status || err?.response?.status
  if (status === 409) {
    // DEBUG: xem server trả gì
    console.log('409 payload:', err?.response?.data)

    // Ưu tiên số từ server; nếu không có, fallback về tổng ngay trên node hiện tại
    const srv = err?.response?.data?.relatedProducts
    const related = (typeof srv === 'number' && !Number.isNaN(srv))
      ? srv
      : Number(cat.productCount ?? 0)

    const force = await showConfirm(
      `Danh mục hoặc danh mục con có liên kết tới ${related} sản phẩm. Xóa tất cả?`
    )

    if (force) {
      await apiService.categoriesDelete.delete(cat.id, true)
      await loadCategories()
      toast.success('Đã xóa kèm dữ liệu liên quan')
      window.dispatchEvent(new Event('categories-updated'))
    }
  } else {
    toast.error('Xảy ra lỗi khi xóa: ' + (err?.message || 'Không xác định'))
  }
}
}

// bulk actions (local update) -> also dispatch so navbar refreshes if admin expects change reflected
const bulkUpdateStatus = (status) => {
  showConfirm(`Bạn có chắc chắn muốn cập nhật trạng thái cho ${selectedCategories.value.length} danh mục?`)
    .then(ok => {
      if (!ok) return
      selectedCategories.value.forEach(id => {
        const c = allCategories.value.find(x => x.id === id)
        if (c) c.status = status
      })
      selectedCategories.value = []
      toast.success('Cập nhật trạng thái thành công')
      // notify clients (if you persist to backend elsewhere, also call event after persistence)
      window.dispatchEvent(new Event('categories-updated'))
    })
}

const bulkDelete = () => {
  showConfirm(`Bạn có chắc chắn muốn xóa ${selectedCategories.value.length} danh mục?`)
    .then(ok => {
      if (!ok) return
      const removeSet = new Set(selectedCategories.value)
      const filterTree = (nodes) =>
        nodes.filter(n => !removeSet.has(n.id)).map(n => ({ ...n, children: n.children ? filterTree(n.children) : [] }))
      categories.value = filterTree(categories.value)
      selectedCategories.value = []
      toast.success('Xóa danh mục đã chọn thành công')
      window.dispatchEvent(new Event('categories-updated'))
    })
}

const addSubCategory = (parent) => {
  const child = {
    id: Date.now(),
    name: 'Danh mục con mới',
    slug: 'sub-' + Date.now(),
    description: '',
    icon: 'bi bi-folder',
    level: (parent.level || 0) + 1,
    parentId: parent.id,
    status: 'active',
    productCount: 0,
    expanded: false,
    children: [],
    isLocal: true
  }
  parent.children ||= []
  parent.children.push(child)
  parent.expanded = true
  editCategory(child)
}

const toggleCategory = (cat) => { cat.expanded = !cat.expanded }
const editCategory = async (cat) => {
  // If this is a local placeholder (not yet persisted), open modal with local data
  if (cat?.isLocal) {
    editingCategory.value = cat
    categoryForm.value = {
      ...cat,
      parentId: cat.parentId ?? null,
      status: (cat.status === 'active' || cat.status === true || cat.status === 1) ? 'active' : 'inactive'
    }
    showAddModal.value = true
    return
  }

  // Otherwise fetch the latest data from server so the modal shows authoritative values
  try {
    let res = await apiService.get(`/danh-muc/${cat.id}`)
    res = res.data ?? res
    // Map backend fields to form model
    const mapped = {
      id: res.id,
      name: res.ten || res.name || '',
      slug: res.slug || '',
      description: res.moTa || res.description || '',
      icon: res.icon || 'bi bi-folder',
      parentId: res.idCha ?? null,
      order: res.thuTu || 0,
      status: (res.hoatDong === 1 || res.hoatDong === true) ? 'active' : 'inactive'
    }

    editingCategory.value = { ...mapped }
    categoryForm.value = { ...mapped }
    showAddModal.value = true
  } catch (err) {
    toast.error('Không thể tải thông tin danh mục: ' + (err?.message || 'Không xác định'))
  }
}

// const deleteCategory = async (cat) => {
//   const ok = await showConfirm(`Bạn có chắc chắn muốn xóa danh mục "${cat.name}"?`)
//   if (!ok) return
//   try {
//     await apiService.categoriesDelete.delete(cat.id, false)
//     await loadCategories()
//     toast.success('Xóa danh mục thành công')
//     // notify clients
//     window.dispatchEvent(new Event('categories-updated'))
//   } catch (err) {
//     const status = err?.status || err?.response?.status
//     if (status === 409) {
//       const force = await showConfirm('Danh mục hoặc danh mục con có liên kết tới sản phẩm. Xóa tất cả?')
//       if (force) {
//         await apiService.categoriesDelete.delete(cat.id, true)
//         await loadCategories()
//         toast.success('Đã xóa kèm dữ liệu liên quan')
//         window.dispatchEvent(new Event('categories-updated'))
//       }
//     } else {
//       toast.error('Xảy ra lỗi khi xóa: ' + (err?.message || 'Không xác định'))
//     }
//   }
// }

const closeModal = () => {
  showAddModal.value = false
  editingCategory.value = null
  categoryForm.value = { name:'', slug:'', description:'', icon:'bi bi-folder', parentId:null, order:0, status:'active' }
}

// const saveCategory = async () => {
//   try {
//     const payload = buildPayload()
//     if (editingCategory.value && !editingCategory.value.isLocal) {
//       await apiService.put(`/categories/${editingCategory.value.id}`, payload)
//       toast.success('Cập nhật danh mục thành công')
//     } else {
//       await apiService.categories.create(payload)
//       toast.success('Tạo danh mục thành công')
//     }
//     await loadCategories()
//     // notify clients
//     window.dispatchEvent(new Event('categories-updated'))
//   } catch (err) {
//     toast.error('Lưu danh mục thất bại: ' + (err?.message || 'Không xác định'))
//   } finally {
//     closeModal()
//   }
// }

// lấy dữ liệu & build tree
const loadCategories = async () => {
  try {
    const resp = await apiService.get('/danh-muc')
    const raw = resp.data ?? resp
    const mapItem = (item) => ({
      id: item.id,
      name: item.ten || item.name || '',
      slug: item.slug || '',
      description: item.moTa || '',
      icon: item.icon || 'bi bi-folder',
      parentId: item.idCha ?? null,
      order: item.thuTu || 0,
      // hoatDong is numeric (1/0) now
      status: (item.hoatDong === 1) ? 'active' : 'inactive',
      productCount: item.productCount || 0,
      createdAt: item.taoLuc ? new Date(item.taoLuc) : new Date(),
      expanded: false,
      children: []
    })

    const flat = (Array.isArray(raw) ? raw : []).map(mapItem)
    const byId = Object.fromEntries(flat.map(f => [f.id, f]))
    const roots = []
    flat.forEach(f => {
      if (f.parentId && byId[f.parentId]) {
        byId[f.parentId].children ||= []
        byId[f.parentId].children.push(f)
      } else {
        roots.push(f)
      }
    })
    categories.value = roots
  } catch (err) {
    console.error('Lỗi khi tải danh mục:', err)
    categories.value = []
  }
}

onMounted(loadCategories)
</script>

<style scoped>
.admin-categories { width: 100%; }

/* Filters Section */
.filters-section { background: white; border-radius: 12px; padding: 1.5rem; margin-bottom: 2rem; box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1); }
.search-row { display: flex; gap: 1rem; align-items: center; margin-bottom: 1rem; }
.search-row .search-box { flex: 1; }
.advanced-filters { border-top: 1px solid #e9ecef; padding-top: 1.5rem; margin-top: 1rem; }

/* Category Statistics */
.category-stats-section { margin-bottom: 2rem; }
.stat-card { background: white; border-radius: 12px; padding: 1.5rem; box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1); display: flex; align-items: center; gap: 1rem; transition: transform 0.2s; }
.stat-card:hover { transform: translateY(-2px); }
.stat-card.total { border-left: 4px solid #6366f1; }
.stat-card.active { border-left: 4px solid #10b981; }
.stat-card.products { border-left: 4px solid #ffc107; }
.stat-card.levels { border-left: 4px solid #6f42c1; }
.stat-icon { width: 50px; height: 50px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 1.5rem; color: white; }
.stat-card.total .stat-icon { background: #6366f1; }
.stat-card.active .stat-icon { background: #10b981; }
.stat-card.products .stat-icon { background: #ffc107; }
.stat-card.levels .stat-icon { background: #6f42c1; }
.stat-content { flex: 1; }
.stat-value { font-size: 2rem; font-weight: 700; color: #2c3e50; margin-bottom: 0.25rem; }
.stat-label { color: #6c757d; font-size: 0.9rem; }

.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 2rem; }
.page-title { font-size: 2rem; font-weight: 700; color: #2c3e50; margin-bottom: 0.5rem; }
.page-subtitle { color: #6c757d; margin: 0; }

/* Categories Tree */
.categories-tree { background: white; border-radius: 12px; padding: 1.5rem; margin-bottom: 2rem; box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1); }
.tree-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 1rem; }
.tree-header h3 { font-size: 1.25rem; font-weight: 600; color: #2c3e50; margin: 0; }
.tree-actions { display: flex; gap: 0.5rem; }
.tree-content { border: 1px solid #e9ecef; border-radius: 8px; padding: 1rem; background-color: #f8f9fa; }
.tree-list { display: flex; flex-direction: column; gap: 0.5rem; }

.tree-node { border-radius: 8px; background: white; border: 1px solid #e9ecef; transition: all 0.3s ease; }
.tree-node:hover { border-color: #6366f1; box-shadow: 0 2px 8px rgba(99,102,241, 0.1); }
.node-content { display: flex; align-items: center; gap: 0.75rem; padding: 1rem; cursor: pointer; }
.expand-btn { background: none; border: none; color: #6c757d; cursor: pointer; padding: 0.25rem; border-radius: 4px; transition: all 0.3s ease; }
.expand-btn:hover { background-color: #f8f9fa; color: #6366f1; }
.node-info { display: flex; align-items: center; gap: 0.75rem; flex: 1; }

/* Root (parent) category emphasis */
.root-node .node-content { padding: 1.25rem; background: linear-gradient(90deg, #ffffff, #fbfbfe); border-left: 4px solid rgba(99,102,241,0.15); }
.root-node .node-name { font-size: 1.05rem; font-weight: 700; }
.root-node .node-meta { color: #6c757d; }

/* Child nodes visually smaller */
.node-children .tree-node .node-content { padding: 0.75rem; background: #fff; }
.node-actions button { cursor: default; }
.node-icon { width: 40px; height: 40px; border-radius: 8px; background-color: #f8f9fa; display: flex; align-items: center; justify-content: center; font-size: 1.25rem; color: #6366f1; }
.node-details { flex: 1; }
.node-name { font-weight: 600; color: #2c3e50; margin-bottom: 0.25rem; }
.node-meta { font-size: 0.85rem; color: #6c757d; }
.node-actions { display: flex; gap: 0.5rem; }
.node-actions .btn { padding: 0.375rem 0.75rem; }
.node-children { border-top: 1px solid #e9ecef; background-color: #f8f9fa; }

/* Table */
.categories-table { background: white; border-radius: 12px; padding: 1.5rem; margin-bottom: 2rem; box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1); }
.table-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 1rem; }
.table-header h3 { font-size: 1.25rem; font-weight: 600; color: #2c3e50; margin: 0; }
.table-actions { display: flex; gap: 1rem; align-items: center; }
.search-box { position: relative; }
.search-icon { position: absolute; left: 0.75rem; top: 50%; transform: translateY(-50%); color: #6c757d; z-index: 1; }
.search-input { padding-left: 2.5rem; width: 250px; }
.table { margin-bottom: 0; }
.table th { border-top: none; font-weight: 600; color: #2c3e50; padding: 1rem 0.75rem; }
.table td { padding: 1rem 0.75rem; vertical-align: middle; }
.category-info { display: flex; align-items: center; gap: 1rem; }
.category-icon { width: 40px; height: 40px; border-radius: 8px; background-color: #f8f9fa; display: flex; align-items: center; justify-content: center; font-size: 1.25rem; color: #6366f1; }
.category-name { font-weight: 600; color: #2c3e50; margin-bottom: 0.25rem; }
.category-description { font-size: 0.85rem; color: #6c757d; }
.category-slug { background-color: #f8f9fa; padding: 0.25rem 0.5rem; border-radius: 4px; font-size: 0.85rem; color: #6c757d; }
.parent-category { color: #6c757d; font-size: 0.9rem; }
.product-count { font-weight: 600; color: #2c3e50; }
.order-number { font-weight: 600; color: #6c757d; }
.status-badge { padding: 0.25rem 0.75rem; border-radius: 12px; font-size: 0.8rem; font-weight: 500; color: white; }
.action-buttons { display: flex; gap: 0.5rem; }
.action-buttons .btn { padding: 0.375rem 0.75rem; }

/* Bulk Actions */
.bulk-actions { position: fixed; bottom: 2rem; left: 50%; transform: translateX(-50%); background: white; border: 1px solid #e9ecef; border-radius: 12px; padding: 1rem 1.5rem; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15); z-index: 1000; }
.bulk-actions-content { display: flex; align-items: center; gap: 1rem; }
.selected-count { font-weight: 600; color: #2c3e50; }
.bulk-buttons { display: flex; gap: 0.5rem; }

/* Modal */
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0, 0, 0, 0.5); display: flex; align-items: center; justify-content: center; z-index: 2000; }
.modal-content { background: white; border-radius: 12px; width: 90%; max-width: 600px; max-height: 90vh; overflow-y: auto; }
.modal-header { padding: 1.5rem; border-bottom: 1px solid #e9ecef; display: flex; align-items: center; justify-content: space-between; }
.modal-title { font-size: 1.25rem; font-weight: 600; color: #2c3e50; margin: 0; }
.btn-close { background: none; border: none; font-size: 1.25rem; color: #6c757d; cursor: pointer; padding: 0.25rem; border-radius: 4px; transition: background-color 0.3s ease; }
.btn-close:hover { background-color: #f8f9fa; }
.modal-body { padding: 1.5rem; }
.modal-footer { padding: 1.5rem; border-top: 1px solid #e9ecef; display: flex; gap: 0.75rem; justify-content: flex-end; }

/* Responsive */
@media (max-width: 768px) {
  .page-header { flex-direction: column; align-items: flex-start; gap: 1rem; }
  .header-right { width: 100%; }
  .header-right .btn { width: 100%; }
  .tree-header { flex-direction: column; align-items: flex-start; gap: 1rem; }
  .table-header { flex-direction: column; align-items: flex-start; gap: 1rem; }
  .table-actions { width: 100%; flex-direction: column; }
  .search-input { width: 100%; }
  .categories-table { padding: 1rem; }
  .table-responsive { font-size: 0.9rem; }
  .action-buttons { flex-direction: column; }
  .bulk-actions { left: 1rem; right: 1rem; transform: none; }
  .bulk-actions-content { flex-direction: column; align-items: flex-start; }
  .bulk-buttons { width: 100%; justify-content: space-between; }
}

/* Tree presentation polish */
.tree-node { border: 1px solid #e9ecef; border-radius: 10px; background: #fff; transition: all .2s ease; }
.tree-node:hover { border-color: #6366f1; box-shadow: 0 2px 8px rgba(99,102,241,.08); }
.tree-node > .node-content { display: flex; align-items: center; gap: .75rem; padding: 1rem; cursor: pointer; }
.tree-node > .node-children { border-top: 1px solid #edf0f3; background: #fbfbfe; padding: .5rem 0 .75rem .75rem; position: relative; }
.tree-node > .node-children::before { content: ""; position: absolute; top: .25rem; bottom: .25rem; left: .5rem; width: 2px; background: #e5e7eb; border-radius: 2px; }
.tree-node > .node-children .tree-node { border: none; background: transparent; box-shadow: none; margin: .25rem 0 0 .5rem; }
.tree-node > .node-children .tree-node > .node-content { padding: .5rem .75rem; background: #fff; border: 1px solid #eef1f4; border-radius: 8px; }
.root-node > .node-content { padding: 1.1rem 1rem; background: linear-gradient(90deg, #ffffff, #fbfbfe); border-left: 4px solid rgba(99,102,241,.15); }
</style>
