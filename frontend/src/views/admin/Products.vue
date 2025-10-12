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
          <i class="bi bi-plus me-2"></i>Thêm sản phẩm
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
            placeholder="Tìm kiếm sản phẩm, SKU, mô tả..."
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
            <label class="form-label">Danh mục</label>
            <select class="form-select" v-model="selectedCategory">
              <option value="">Tất cả danh mục</option>
              <optgroup label="Áo nam">
                <option value="ao">Áo (Tất cả)</option>
                <option value="ao-so-mi">Áo sơ mi</option>
                <option value="ao-thun">Áo thun</option>
                <option value="ao-khoac">Áo khoác</option>
                <option value="ao-len">Áo len/Hoodie</option>
              </optgroup>
              <optgroup label="Quần nam">
                <option value="quan">Quần (Tất cả)</option>
                <option value="quan-au">Quần âu</option>
                <option value="quan-jean">Quần jean</option>
                <option value="quan-kaki">Quần kaki</option>
                <option value="quan-short">Quần short</option>
              </optgroup>
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
              <input type="number" class="form-control" placeholder="Từ" v-model.number="priceRange.min">
              <span class="range-separator">-</span>
              <input type="number" class="form-control" placeholder="Đến" v-model.number="priceRange.max">
            </div>
          </div>
          <div class="col-md-3">
            <label class="form-label">Tồn kho</label>
            <select class="form-select" v-model="stockFilter">
              <option value="">Tất cả</option>
              <option value="in-stock">Còn hàng</option>
              <option value="low-stock">Sắp hết hàng (< 10)</option>
              <option value="out-of-stock">Hết hàng</option>
            </select>
          </div>
          <div class="col-md-3">
            <label class="form-label">Ngày tạo</label>
            <input type="date" class="form-control" v-model="createdDate">
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
            <i class="bi bi-table"></i>
          </button>
          <button 
            :class="['view-btn', { active: viewMode === 'grid' }]"
            @click="viewMode = 'grid'"
            title="Dạng lưới"
          >
            <i class="bi bi-grid-3x3-gap"></i>
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
                <input type="checkbox" class="form-check-input" v-model="selectAll" @change="toggleSelectAll">
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
                <input type="checkbox" class="form-check-input" v-model="selectedProducts" :value="product.id">
              </td>
              <td>
                <div class="product-info">
                  <div class="product-image">
                    <img :src="product.image" :alt="product.name" class="img-fluid">
                    <div v-if="product.isNew" class="new-badge">Mới</div>
                    <div v-if="product.isFeatured" class="featured-badge">Nổi bật</div>
                  </div>
                  <div class="product-details">
                    <div class="product-name">{{ product.name }}</div>
                    <div class="product-sku">SKU: {{ product.sku }}</div>
                    <div v-if="product.tags && product.tags.length" class="product-tags">
                      <span v-for="tag in product.tags.slice(0, 2)" :key="tag" class="tag">{{ tag }}</span>
                      <span v-if="product.tags.length > 2" class="tag-more">+{{ product.tags.length - 2 }}</span>
                    </div>
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
                    <i class="bi bi-exclamation-triangle"></i> Sắp hết
                  </div>
                </div>
              </td>
              <td>
                <div class="rating-info">
                  <div class="rating-stars">
                    <i v-for="i in 5" :key="i" 
                       :class="['star', { filled: i <= product.rating }]"></i>
                  </div>
                  <div class="rating-text">{{ product.rating }}/5 ({{ product.reviewCount }} đánh giá)</div>
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
                  <button class="btn btn-sm btn-outline-primary" @click="editProduct(product)" title="Chỉnh sửa">
                    <i class="bi bi-pencil"></i>
                  </button>
                  <button class="btn btn-sm btn-outline-info" @click="viewProduct(product)" title="Xem chi tiết">
                    <i class="bi bi-eye"></i>
                  </button>
                  <button class="btn btn-sm btn-outline-success" @click="duplicateProduct(product)" title="Nhân bản">
                    <i class="bi bi-copy"></i>
                  </button>
                  <button class="btn btn-sm btn-outline-warning" @click="toggleFeatured(product)" title="Đánh dấu nổi bật">
                    <i :class="product.isFeatured ? 'bi bi-star-fill' : 'bi bi-star'"></i>
                  </button>
                  <button class="btn btn-sm btn-outline-danger" @click="deleteProduct(product)" title="Xóa">
                    <i class="bi bi-trash"></i>
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
          <div v-for="product in filteredProducts" :key="product.id" class="col-lg-3 col-md-4 col-sm-6">
            <div class="product-card">
              <div class="product-card-header">
                <input type="checkbox" class="form-check-input" v-model="selectedProducts" :value="product.id">
                <div class="product-badges">
                  <span v-if="product.isNew" class="badge bg-success">Mới</span>
                  <span v-if="product.isFeatured" class="badge bg-warning">Nổi bật</span>
                </div>
              </div>
              <div class="product-image-container">
                <img :src="product.image" :alt="product.name" class="product-image">
                <div class="product-overlay">
                  <button class="btn btn-sm btn-light" @click="viewProduct(product)" title="Xem chi tiết">
                    <i class="bi bi-eye"></i>
                  </button>
                  <button class="btn btn-sm btn-primary" @click="editProduct(product)" title="Chỉnh sửa">
                    <i class="bi bi-pencil"></i>
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
                    <i v-for="i in 5" :key="i" 
                       :class="['star', { filled: i <= product.rating }]"></i>
                  </div>
                  <span class="rating-count">({{ product.reviewCount }})</span>
                </div>
                <div class="product-price">
                  <span class="current-price">{{ formatCurrency(product.price) }}</span>
                  <span v-if="product.originalPrice" class="original-price">{{ formatCurrency(product.originalPrice) }}</span>
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
                    <i class="bi bi-pencil"></i>
                  </button>
                  <button class="btn btn-sm btn-outline-success" @click="duplicateProduct(product)">
                    <i class="bi bi-copy"></i>
                  </button>
                  <button class="btn btn-sm btn-outline-warning" @click="toggleFeatured(product)">
                    <i :class="product.isFeatured ? 'bi bi-star-fill' : 'bi bi-star'"></i>
                  </button>
                  <button class="btn btn-sm btn-outline-danger" @click="deleteProduct(product)">
                    <i class="bi bi-trash"></i>
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
            <i class="bi bi-check me-1"></i>Kích hoạt
          </button>
          <button class="btn btn-sm btn-outline-warning" @click="bulkUpdateStatus('inactive')">
            <i class="bi bi-pause me-1"></i>Ngừng bán
          </button>
          <button class="btn btn-sm btn-outline-danger" @click="bulkDelete">
            <i class="bi bi-trash me-1"></i>Xóa
          </button>
        </div>
      </div>
    </div>

    <!-- Add/Edit Product Modal with TABS -->
    <div v-if="showAddModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content modal-large" @click.stop>
        <div class="modal-header">
          <h5 class="modal-title">{{ editingProduct ? 'Chỉnh sửa sản phẩm' : 'Thêm sản phẩm mới' }}</h5>
          <button class="btn-close" @click="closeModal">
            <i class="bi bi-x"></i>
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
          <button
            :class="['tab-btn', { active: activeTab === 'variants' }]"
            @click="activeTab = 'variants'"
          >
            <i class="bi bi-grid-3x3 me-1"></i>
            Biến thể & Tồn kho
            <span v-if="productForm.variants && productForm.variants.length > 0" class="tab-badge">
              {{ productForm.variants.length }}
            </span>
          </button>
          <button
            :class="['tab-btn', { active: activeTab === 'seo' }]"
            @click="activeTab = 'seo'"
          >
            <i class="bi bi-search me-1"></i>
            SEO & Marketing
          </button>
        </div>

        <div class="modal-body">
          <!-- Tab 1: Basic Info -->
          <div v-show="activeTab === 'basic'" class="tab-content">
            <form @submit.prevent="saveProduct">
              <div class="row g-3">
                <div class="col-md-8">
                  <label class="form-label">Tên sản phẩm *</label>
                  <input type="text" class="form-control" v-model="productForm.name" required>
                </div>
                <div class="col-md-4">
                  <label class="form-label">SKU *</label>
                  <div class="input-group">
                    <input type="text" class="form-control" v-model="productForm.sku" required>
                    <button type="button" class="btn btn-outline-secondary" @click="generateSKU" title="Tạo SKU tự động">
                      <i class="bi bi-arrow-clockwise"></i>
                    </button>
                  </div>
                </div>
                <div class="col-md-6">
                  <label class="form-label">Danh mục * (CHỈ QUẦN ÁO NAM)</label>
                  <select class="form-select" v-model="productForm.category" required>
                    <option value="">Chọn danh mục</option>
                    <optgroup label="ÁO NAM">
                      <option value="ao">Áo (Tất cả)</option>
                      <option value="ao-so-mi">Áo sơ mi</option>
                      <option value="ao-thun">Áo thun</option>
                      <option value="ao-khoac">Áo khoác</option>
                      <option value="ao-len">Áo len/Hoodie</option>
                      <option value="ao-vest">Áo vest</option>
                    </optgroup>
                    <optgroup label="QUẦN NAM">
                      <option value="quan">Quần (Tất cả)</option>
                      <option value="quan-au">Quần âu</option>
                      <option value="quan-jean">Quần jean</option>
                      <option value="quan-kaki">Quần kaki</option>
                      <option value="quan-short">Quần short</option>
                      <option value="quan-jogger">Quần jogger</option>
                    </optgroup>
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
                  <input type="number" class="form-control" v-model.number="productForm.price" required min="0">
                </div>
                <div class="col-md-4">
                  <label class="form-label">Giá gốc</label>
                  <input type="number" class="form-control" v-model.number="productForm.originalPrice" min="0">
                </div>
                <div class="col-md-4">
                  <label class="form-label">Giảm giá (%)</label>
                  <input type="number" class="form-control" :value="discountPercent" readonly disabled>
                </div>
                <div class="col-12">
                  <label class="form-label">Mô tả sản phẩm</label>
                  <textarea class="form-control" rows="4" v-model="productForm.description" placeholder="Nhập mô tả chi tiết về sản phẩm..."></textarea>
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

          <!-- Tab 3: Variants -->
          <div v-show="activeTab === 'variants'" class="tab-content">
            <VariantManagerAdmin
              :product-id="productForm.id"
              :product-name="productForm.name"
              :product-sku="productForm.sku"
              :category="getCategoryType(productForm.category)"
              :initial-variants="productForm.variants || []"
              :initial-colors="productForm.variantColors || []"
              :initial-material="productForm.material || ''"
              @update="handleVariantsUpdate"
            />
          </div>

          <!-- Tab 4: SEO & Marketing -->
          <div v-show="activeTab === 'seo'" class="tab-content">
            <form>
              <div class="row g-3">
                <div class="col-12">
                  <label class="form-label">URL Slug</label>
                  <div class="input-group">
                    <span class="input-group-text">/product/</span>
                    <input type="text" class="form-control" v-model="productForm.slug" placeholder="ao-so-mi-nam-cao-cap">
                    <button type="button" class="btn btn-outline-secondary" @click="generateSlug" title="Tạo slug từ tên">
                      <i class="bi bi-arrow-clockwise"></i>
                    </button>
                  </div>
                </div>
                <div class="col-12">
                  <label class="form-label">Meta Title (SEO)</label>
                  <input type="text" class="form-control" v-model="productForm.metaTitle" placeholder="Áo sơ mi nam cao cấp | AURO" maxlength="60">
                  <small class="text-muted">{{ (productForm.metaTitle || '').length }}/60 ký tự</small>
                </div>
                <div class="col-12">
                  <label class="form-label">Meta Description (SEO)</label>
                  <textarea class="form-control" rows="3" v-model="productForm.metaDescription" placeholder="Mô tả ngắn gọn cho Google..." maxlength="160"></textarea>
                  <small class="text-muted">{{ (productForm.metaDescription || '').length }}/160 ký tự</small>
                </div>
                <div class="col-12">
                  <div class="form-check form-switch">
                    <input class="form-check-input" type="checkbox" v-model="productForm.isFeatured" id="featuredCheck">
                    <label class="form-check-label" for="featuredCheck">
                      <i class="bi bi-star me-1"></i>
                      Sản phẩm nổi bật (hiển thị trên trang chủ)
                    </label>
                  </div>
                </div>
                <div class="col-12">
                  <div class="form-check form-switch">
                    <input class="form-check-input" type="checkbox" v-model="productForm.isNew" id="newCheck">
                    <label class="form-check-label" for="newCheck">
                      <i class="bi bi-badge-wc me-1"></i>
                      Sản phẩm mới (badge "NEW")
                    </label>
                  </div>
                </div>
                <div class="col-12">
                  <label class="form-label">Tags (để tìm kiếm dễ hơn)</label>
                  <div class="tags-input">
                    <span v-for="(tag, index) in (productForm.tags || [])" :key="index" class="tag-item">
                      {{ tag }}
                      <button type="button" @click="removeTag(index)" class="tag-remove">
                        <i class="bi bi-x"></i>
                      </button>
                    </span>
                    <input
                      type="text"
                      class="tag-input"
                      placeholder="VD: Bestseller, Sale, Premium..."
                      @keydown.enter.prevent="addTag"
                      v-model="newTag"
                    />
                  </div>
                  <small class="text-muted">Gợi ý: Bestseller, Sale, Premium, Limited, Summer, Formal, Casual</small>
                </div>
              </div>
            </form>
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import VariantManagerAdmin from '@/components/admin/VariantManagerAdmin.vue'
import ImageUploaderAdmin from '@/components/admin/ImageUploaderAdmin.vue'

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

// NEW: Tab management
const activeTab = ref('basic')
const newTag = ref('')

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

const productForm = ref({
  name: '',
  sku: '',
  category: '',
  status: 'active',
  price: 0,
  originalPrice: 0,
  stock: 0,
  description: '',
  image: '',
  // NEW: Advanced fields
  images: [],
  variants: [],
  variantColors: [],
  material: '',
  slug: '',
  metaTitle: '',
  metaDescription: '',
  tags: []
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
    createdAt: new Date('2024-01-15'),
    rating: 4.5,
    reviewCount: 128,
    tags: ['Premium', 'Bestseller'],
    isNew: false,
    isFeatured: true
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
    createdAt: new Date('2024-01-20'),
    rating: 4.2,
    reviewCount: 89,
    tags: ['Sale', 'Premium'],
    isNew: false,
    isFeatured: false
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
    createdAt: new Date('2024-01-25'),
    rating: 4.8,
    reviewCount: 67,
    tags: ['Limited', 'Premium'],
    isNew: false,
    isFeatured: true
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
    createdAt: new Date('2024-02-01'),
    rating: 4.0,
    reviewCount: 203,
    tags: ['Sale', 'New'],
    isNew: true,
    isFeatured: false
  },
  {
    id: 5,
    name: 'Vest nam công sở',
    sku: 'VST005',
    category: 'ao',
    price: 1200000,
    originalPrice: 1500000,
    stock: 8,
    status: 'active',
    description: 'Vest nam cao cấp cho công sở',
    image: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?ixlib=rb-4.0.3&auto=format&fit=crop&w=100&q=80',
    createdAt: new Date('2024-01-10'),
    rating: 4.7,
    reviewCount: 45,
    tags: ['Premium', 'Limited'],
    isNew: false,
    isFeatured: true
  },
  {
    id: 6,
    name: 'Quần jean nam',
    sku: 'QJN006',
    category: 'quan',
    price: 380000,
    originalPrice: 500000,
    stock: 32,
    status: 'active',
    description: 'Quần jean nam phong cách trẻ trung',
    image: 'https://images.unsplash.com/photo-1542272604-787c3835535d?ixlib=rb-4.0.3&auto=format&fit=crop&w=100&q=80',
    createdAt: new Date('2024-01-18'),
    rating: 4.3,
    reviewCount: 156,
    tags: ['Bestseller', 'Sale'],
    isNew: false,
    isFeatured: false
  }
])

// Computed
const filteredProducts = computed(() => {
  let filtered = products.value

  // Search filter
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(product =>
      product.name.toLowerCase().includes(query) ||
      product.sku.toLowerCase().includes(query) ||
      product.description.toLowerCase().includes(query)
    )
  }

  // Category filter
  if (selectedCategory.value) {
    filtered = filtered.filter(product => product.category === selectedCategory.value)
  }

  // Status filter
  if (selectedStatus.value) {
    filtered = filtered.filter(product => product.status === selectedStatus.value)
  }

  // Price range filter
  if (priceRange.value.min !== null && priceRange.value.min !== '') {
    filtered = filtered.filter(product => product.price >= priceRange.value.min)
  }
  if (priceRange.value.max !== null && priceRange.value.max !== '') {
    filtered = filtered.filter(product => product.price <= priceRange.value.max)
  }

  // Stock filter
  if (stockFilter.value) {
    switch (stockFilter.value) {
      case 'in-stock':
        filtered = filtered.filter(product => product.stock > 10)
        break
      case 'low-stock':
        filtered = filtered.filter(product => product.stock > 0 && product.stock <= 10)
        break
      case 'out-of-stock':
        filtered = filtered.filter(product => product.stock === 0)
        break
    }
  }

  // Date filter
  if (createdDate.value) {
    const selectedDate = new Date(createdDate.value)
    filtered = filtered.filter(product => {
      const productDate = new Date(product.createdAt)
      return productDate.toDateString() === selectedDate.toDateString()
    })
  }

  // Tags filter
  if (selectedTags.value.length > 0) {
    filtered = filtered.filter(product =>
      product.tags && product.tags.some(tag => selectedTags.value.includes(tag))
    )
  }

  // Sorting
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
    'ao-so-mi': 'Áo sơ mi',
    'ao-thun': 'Áo thun',
    'ao-khoac': 'Áo khoác',
    'ao-len': 'Áo len/Hoodie',
    'ao-vest': 'Áo vest',
    'quan': 'Quần',
    'quan-au': 'Quần âu',
    'quan-jean': 'Quần jean',
    'quan-kaki': 'Quần kaki',
    'quan-short': 'Quần short',
    'quan-jogger': 'Quần jogger'
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
}

const getSortIcon = (field) => {
  if (tableSort.value.field !== field) return 'bi bi-caret-up-down'
  return tableSort.value.direction === 'asc' ? 'bi bi-caret-up' : 'bi bi-caret-down'
}

const formatTime = (date) => {
  return new Intl.DateTimeFormat('vi-VN', {
    hour: '2-digit',
    minute: '2-digit'
  }).format(date)
}

const duplicateProduct = (product) => {
  const duplicatedProduct = {
    ...product,
    id: Date.now(),
    name: product.name + ' (Copy)',
    sku: product.sku + '-COPY',
    createdAt: new Date(),
    isNew: true,
    isFeatured: false
  }
  products.value.unshift(duplicatedProduct)
}

const toggleFeatured = (product) => {
  product.isFeatured = !product.isFeatured
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
  activeTab.value = 'basic'
  productForm.value = {
    name: '',
    sku: '',
    category: '',
    status: 'active',
    price: 0,
    originalPrice: 0,
    stock: 0,
    description: '',
    image: '',
    images: [],
    variants: [],
    variantColors: [],
    material: '',
    slug: '',
    metaTitle: '',
    metaDescription: '',
    tags: []
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

// NEW: Computed for discount
const discountPercent = computed(() => {
  if (productForm.value.originalPrice && productForm.value.price) {
    return Math.round((1 - productForm.value.price / productForm.value.originalPrice) * 100)
  }
  return 0
})

// NEW: Helper methods
const getCategoryType = (category) => {
  // Convert category to 'ao' or 'quan' for VariantManager
  if (!category) return 'ao'
  if (category.startsWith('ao')) return 'ao'
  if (category.startsWith('quan')) return 'quan'
  return 'ao'
}

const generateSKU = () => {
  const prefix = getCategoryType(productForm.value.category).toUpperCase()
  const timestamp = Date.now().toString().slice(-6)
  productForm.value.sku = `${prefix}-${timestamp}`
}

const generateSlug = () => {
  if (!productForm.value.name) return
  productForm.value.slug = productForm.value.name
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/đ/g, 'd')
    .replace(/[^a-z0-9\s-]/g, '')
    .trim()
    .replace(/\s+/g, '-')
    .replace(/-+/g, '-')
}

const addTag = () => {
  if (newTag.value.trim()) {
    if (!productForm.value.tags) {
      productForm.value.tags = []
    }
    if (!productForm.value.tags.includes(newTag.value.trim())) {
      productForm.value.tags.push(newTag.value.trim())
    }
    newTag.value = ''
  }
}

const removeTag = (index) => {
  productForm.value.tags.splice(index, 1)
}

const handleImagesUpdate = (images) => {
  productForm.value.images = images
  // Set primary image as main product image
  const primary = images.find(img => img.isPrimary)
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

// Lifecycle
onMounted(() => {
  // Initialize products page
})
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

.new-badge, .featured-badge {
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
