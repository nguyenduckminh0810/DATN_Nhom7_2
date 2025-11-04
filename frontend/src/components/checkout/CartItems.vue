<template>
  <div class="cart-items-section">
    <div class="section-card">
      <!-- Header -->
      <div class="section-header">
        <div class="cart-header-layout">
          <div class="cart-header-left">
            <div class="d-flex align-items-center">
              <h5 class="mb-0 me-3">
                <i class="bi bi-cart3 me-2"></i>Giỏ hàng
              </h5>
              <div class="form-check me-3">
                <input class="form-check-input" 
                       type="checkbox" 
                       id="selectAll"
                       :checked="allItemsSelected"
                       @change="toggleSelectAll">
                <label class="form-check-label text-white" for="selectAll">
                  Chọn tất cả
                </label>
              </div>
              <span class="badge bg-white text-warning">
                {{ items ? items.length : 0 }} sản phẩm
              </span>
            </div>
          </div>
          
          <div class="cart-header-right">
            <button v-if="items && items.length > 0" 
                    class="btn btn-danger btn-sm clear-all-btn"
                    @click="clearAllItems"
                    title="Xóa tất cả sản phẩm">
              <i class="bi bi-trash-fill me-1"></i>Xóa tất cả
            </button>
          </div>
        </div>
      </div>

      <!-- Cart Items List -->
      <div class="section-body p-0">
        <div v-if="!items || items.length === 0" class="empty-cart">
          <div class="text-center py-5">
            <i class="bi bi-cart-x display-4 text-muted mb-3"></i>
            <h6 class="text-muted">Giỏ hàng trống</h6>
            <p class="text-muted small">Hãy thêm sản phẩm vào giỏ hàng</p>
          </div>
        </div>

        <div v-else>
          <div v-for="item in items" :key="item.itemKey" class="cart-item">
            <div class="row align-items-center p-3">
              <!-- Checkbox - Always visible -->
              <div class="col-1">
                <div class="form-check">
                  <input class="form-check-input cart-checkbox" 
                         type="checkbox" 
                         v-model="item.selected"
                         :id="'item-' + item.id"
                         style="display: block !important; visibility: visible !important; opacity: 1 !important;">
                  <label class="form-check-label" :for="'item-' + item.id">
                    <!-- Empty label for spacing -->
                  </label>
                </div>
              </div>

              <!-- Product Image -->
              <div class="col-2">
                <div class="cart-product-image">
                  <img :src="item.image || 'https://via.placeholder.com/150'" 
                       :alt="item.name"
                       class="img-fluid rounded"
                       @error="handleImageError">
                </div>
              </div>

              <!-- Product Info -->
              <div class="col-5">
                <div class="cart-product-info">
                  <h6 class="mb-1 fw-bold">{{ item.name }}</h6>
                  <div class="product-variant mb-2">
                    <span class="variant-text">{{ item.color || 'N/A' }} / {{ item.size || 'N/A' }}</span>
                  </div>
                  
                  <!-- Variant Controls -->
                  <div class="variant-controls mb-2">
                    <div class="dropdown me-2">
                      <button class="btn btn-outline-secondary btn-sm dropdown-toggle" 
                              type="button" 
                              @click="toggleDropdown"
                              :class="{ 'text-muted': !item.color }">
                        <span v-if="item.color" class="d-flex align-items-center">
                          <span class="color-swatch me-2" :style="{ backgroundColor: getColorValue(item.color) }"></span>
                          {{ item.color }}
                        </span>
                        <span v-else>Chọn màu</span>
                      </button>
                      <ul class="dropdown-menu">
                        <li><a class="dropdown-item" href="#" @click.prevent="updateVariant(item.itemKey, 'color', 'Đen')">
                          <span class="color-swatch me-2" style="background-color: #000000"></span>Đen
                        </a></li>
                        <li><a class="dropdown-item" href="#" @click.prevent="updateVariant(item.itemKey, 'color', 'Trắng')">
                          <span class="color-swatch me-2" style="background-color: #ffffff; border: 1px solid #ccc"></span>Trắng
                        </a></li>
                        <li><a class="dropdown-item" href="#" @click.prevent="updateVariant(item.itemKey, 'color', 'Xám')">
                          <span class="color-swatch me-2" style="background-color: #808080"></span>Xám
                        </a></li>
                        <li><a class="dropdown-item" href="#" @click.prevent="updateVariant(item.itemKey, 'color', 'Đỏ')">
                          <span class="color-swatch me-2" style="background-color: #ff0000"></span>Đỏ
                        </a></li>
                        <li><a class="dropdown-item" href="#" @click.prevent="updateVariant(item.itemKey, 'color', 'Xanh')">
                          <span class="color-swatch me-2" style="background-color: #007bff"></span>Xanh
                        </a></li>
                        <li><a class="dropdown-item" href="#" @click.prevent="updateVariant(item.itemKey, 'color', 'Vàng')">
                          <span class="color-swatch me-2" style="background-color: #ffc107"></span>Vàng
                        </a></li>
                      </ul>
                    </div>
                    
                    <div class="dropdown">
                      <button class="btn btn-outline-secondary btn-sm dropdown-toggle" 
                              type="button" 
                              @click="toggleDropdown"
                              :class="{ 'text-muted': !item.size }">
                        {{ item.size || 'Chọn size' }}
                      </button>
                      <ul class="dropdown-menu">
                        <li><a class="dropdown-item" href="#" @click.prevent="updateVariant(item.itemKey, 'size', 'S')">S</a></li>
                        <li><a class="dropdown-item" href="#" @click.prevent="updateVariant(item.itemKey, 'size', 'M')">M</a></li>
                        <li><a class="dropdown-item" href="#" @click.prevent="updateVariant(item.itemKey, 'size', 'L')">L</a></li>
                        <li><a class="dropdown-item" href="#" @click.prevent="updateVariant(item.itemKey, 'size', 'XL')">XL</a></li>
                        <li><a class="dropdown-item" href="#" @click.prevent="updateVariant(item.itemKey, 'size', 'XXL')">XXL</a></li>
                      </ul>
                    </div>
                  </div>

                  <!-- Delete Button -->
                  <button class="btn btn-link text-danger p-0 delete-btn" 
                          @click="removeItem(item.itemKey)">
                    <i class="bi bi-trash me-1"></i>Xóa
                  </button>
                </div>
              </div>

              <!-- Quantity -->
              <div class="col-2">
                <div class="quantity-controls">
                  <button 
                    class="qty-btn minus"
                    @click="decreaseQuantity(item.itemKey)"
                    :disabled="item.quantity <= 1"
                  >
                    <i class="bi bi-dash"></i>
                  </button>
                  <span class="qty-display">{{ item.quantity }}</span>
                  <button 
                    class="qty-btn plus"
                    @click="increaseQuantity(item.itemKey)"
                  >
                    <i class="bi bi-plus"></i>
                  </button>
                </div>
              </div>

              <!-- Price -->
              <div class="col-2">
                <div class="cart-price text-end">
                  <div class="current-price fw-bold">
                    {{ formatPrice(item.price * item.quantity) }}
                  </div>
                  <div v-if="item.originalPrice && item.originalPrice > item.price" 
                       class="original-price text-muted small">
                    {{ formatPrice(item.originalPrice * item.quantity) }}
                  </div>
                  <!-- Demo: Hiển thị giá gốc cho demo (có thể xóa sau) -->
                  <div v-if="!item.originalPrice || item.originalPrice <= item.price" 
                       class="original-price text-muted small">
                    {{ formatPrice((item.price * 1.3) * item.quantity) }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Continue Shopping -->
    <div class="mt-3">
      <button class="btn btn-outline-secondary w-100 continue-btn">
        <i class="bi bi-arrow-left me-2"></i>Tiếp tục mua sắm
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted } from 'vue'
import { useCart } from '@/composables/useCart'

const { items, updateQuantity, removeItem, clearCart, formatPrice } = useCart()

// Ensure all items have selected property - MẶC ĐỊNH LÀ TRUE (đã chọn)
items.value.forEach(item => {
  if (item.selected === undefined) {
    item.selected = true  // Mặc định tất cả sản phẩm được chọn
  }
})

const increaseQuantity = (itemKey) => {
  const item = items.value.find(item => item.itemKey === itemKey)
  if (item) {
    updateQuantity(itemKey, item.quantity + 1)
  }
}

const decreaseQuantity = (itemKey) => {
  const item = items.value.find(item => item.itemKey === itemKey)
  if (item && item.quantity > 1) {
    updateQuantity(itemKey, item.quantity - 1)
  }
}

const clearAllItems = () => {
  if (window.confirm('Bạn có chắc chắn muốn xóa tất cả sản phẩm khỏi giỏ hàng?')) {
    clearCart()
    if (window.$toast) {
      window.$toast.success('Đã xóa tất cả sản phẩm khỏi giỏ hàng')
    }
  }
}

const updateVariant = (itemKey, type, value) => {
  const item = items.value.find(item => item.itemKey === itemKey)
  if (item) {
    if (type === 'color') {
      item.color = value
    } else if (type === 'size') {
      item.size = value
    }
    
    if (window.$toast) {
      window.$toast.success(`Đã chọn ${type === 'color' ? 'màu' : 'size'} ${value}`)
    }
  }
}

const getColorValue = (colorName) => {
  const colorMap = {
    'Đen': '#000000',
    'Trắng': '#ffffff',
    'Xám': '#808080',
    'Đỏ': '#ff0000',
    'Xanh': '#007bff',
    'Vàng': '#ffc107'
  }
  return colorMap[colorName] || colorName
}

const handleImageError = (event) => {
  event.target.src = 'https://via.placeholder.com/150?text=No+Image'
}



// Logic cho checkbox "Chọn tất cả"
const allItemsSelected = computed(() => {
  if (!items.value || items.value.length === 0) return false
  return items.value.every(item => item.selected !== false)
})

const toggleSelectAll = () => {
  if (!items.value || items.value.length === 0) return
  
  const shouldSelectAll = !allItemsSelected.value
  items.value.forEach(item => {
    item.selected = shouldSelectAll
  })
  
  if (window.$toast) {
    const message = shouldSelectAll ? 'Đã chọn tất cả sản phẩm' : 'Đã bỏ chọn tất cả sản phẩm'
    window.$toast.success(message)
  }
}

// Handle dropdown toggle manually
const toggleDropdown = (event) => {
  event.preventDefault()
  const dropdown = event.target.closest('.dropdown')
  const cartItem = dropdown.closest('.cart-item')
  const isOpen = dropdown.classList.contains('show')
  
  // Close all other dropdowns
  document.querySelectorAll('.dropdown.show').forEach(d => {
    if (d !== dropdown) {
      d.classList.remove('show')
      const menu = d.querySelector('.dropdown-menu')
      const item = d.closest('.cart-item')
      if (menu) {
        menu.style.top = 'auto'
        menu.style.left = 'auto'
        menu.style.bottom = 'auto'
        menu.style.transform = 'none'
      }
      if (item) {
        item.classList.remove('show-dropdown')
      }
    }
  })
  
  // Toggle current dropdown
  if (isOpen) {
    dropdown.classList.remove('show')
    cartItem.classList.remove('show-dropdown')
    const dropdownMenu = dropdown.querySelector('.dropdown-menu')
    if (dropdownMenu) {
      dropdownMenu.style.top = 'auto'
      dropdownMenu.style.left = 'auto'
      dropdownMenu.style.bottom = 'auto'
      dropdownMenu.style.transform = 'none'
    }
  } else {
    dropdown.classList.add('show')
    cartItem.classList.add('show-dropdown')
    
    // Calculate position for fixed dropdown
    const dropdownMenu = dropdown.querySelector('.dropdown-menu')
    const button = dropdown.querySelector('.dropdown-toggle')
    const buttonRect = button.getBoundingClientRect()
    const viewportHeight = window.innerHeight
    const viewportWidth = window.innerWidth
    
    // Calculate position
    let top = buttonRect.bottom + window.scrollY + 4
    let left = buttonRect.left + window.scrollX
    
    // Check if dropdown should open upward (for last items)
    const dropdownHeight = 200 // Estimated height
    if (top + dropdownHeight > viewportHeight + window.scrollY - 50) {
      top = buttonRect.top + window.scrollY - dropdownHeight - 4
    }
    
    // Check if dropdown goes beyond right edge
    const dropdownWidth = 160 // min-width
    if (left + dropdownWidth > viewportWidth - 10) {
      left = viewportWidth - dropdownWidth - 10
    }
    
    // Set position
    dropdownMenu.style.top = top + 'px'
    dropdownMenu.style.left = left + 'px'
  }
}

// Close dropdown when clicking outside
const handleOutsideClick = (event) => {
  if (!event.target.closest('.dropdown')) {
    document.querySelectorAll('.dropdown.show').forEach(dropdown => {
      dropdown.classList.remove('show')
      const menu = dropdown.querySelector('.dropdown-menu')
      const item = dropdown.closest('.cart-item')
      if (menu) {
        menu.style.top = 'auto'
        menu.style.left = 'auto'
        menu.style.bottom = 'auto'
        menu.style.transform = 'none'
      }
      if (item) {
        item.classList.remove('show-dropdown')
      }
    })
  }
}

// Close dropdown when scrolling
const handleScroll = () => {
  document.querySelectorAll('.dropdown.show').forEach(dropdown => {
    dropdown.classList.remove('show')
    const menu = dropdown.querySelector('.dropdown-menu')
    const item = dropdown.closest('.cart-item')
    if (menu) {
      menu.style.top = 'auto'
      menu.style.left = 'auto'
      menu.style.bottom = 'auto'
      menu.style.transform = 'none'
    }
    if (item) {
      item.classList.remove('show-dropdown')
    }
  })
}

// Close dropdown when window resizes
const handleResize = () => {
  document.querySelectorAll('.dropdown.show').forEach(dropdown => {
    dropdown.classList.remove('show')
    const menu = dropdown.querySelector('.dropdown-menu')
    const item = dropdown.closest('.cart-item')
    if (menu) {
      menu.style.top = 'auto'
      menu.style.left = 'auto'
      menu.style.bottom = 'auto'
      menu.style.transform = 'none'
    }
    if (item) {
      item.classList.remove('show-dropdown')
    }
  })
}

// Add event listeners with proper cleanup
onMounted(() => {
  document.addEventListener('click', handleOutsideClick)
  document.addEventListener('scroll', handleScroll, true)
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  document.removeEventListener('click', handleOutsideClick)
  document.removeEventListener('scroll', handleScroll, true)
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.cart-items-section {
  margin-bottom: 2rem;
}

.section-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow: visible !important;
}

.section-header {
  background: linear-gradient(135deg, #B8860B 0%, #DAA520 100%);
  color: white;
  padding: 1rem 1.5rem;
  border: none;
  position: relative;
  overflow: hidden;
  border-radius: 16px 16px 0 0 !important;
}

.section-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(45deg, rgba(255, 255, 255, 0.1) 0%, transparent 100%);
  pointer-events: none;
}

.section-body {
  overflow: visible !important;
  position: relative;
  z-index: 5;
}

.section-header h5 {
  color: white !important;
  font-weight: 700;
  margin: 0;
  font-size: 1.1rem;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.section-header h5 i {
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.3));
}

.cart-header-layout {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.cart-header-left {
  display: flex;
  align-items: center;
  flex: 1;
}

.cart-header-right {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  margin-left: 1rem;
}

.badge {
  font-size: 0.8rem;
  padding: 0.4rem 0.8rem;
  border-radius: 20px;
  font-weight: 600;
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

/* Select All Checkbox Styling */
.form-check-input {
  background-color: rgba(255, 255, 255, 0.9);
  border: 2px solid rgba(255, 255, 255, 0.5);
  border-radius: 4px;
  width: 18px;
  height: 18px;
}

.form-check-input:checked {
  background-color: #28a745;
  border-color: #28a745;
}

.form-check-input:focus {
  box-shadow: 0 0 0 0.2rem rgba(255, 255, 255, 0.25);
}

.form-check-label {
  font-size: 0.9rem;
  font-weight: 600;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
  cursor: pointer;
  user-select: none;
}

.cart-item {
  border-bottom: 1px solid #f0f0f0;
  overflow: visible !important;
  position: relative;
  z-index: 10;
}

.cart-item:last-child {
  border-bottom: none;
  overflow: visible !important;
  margin-bottom: 2rem !important;
}

/* Checkbox styling - Always visible */
.cart-checkbox {
  display: block !important;
  visibility: visible !important;
  opacity: 1 !important;
  width: 20px !important;
  height: 20px !important;
  border: 2px solid #6c757d !important;
  border-radius: 4px !important;
  background-color: white !important;
  cursor: pointer !important;
}

.cart-checkbox:checked {
  background-color: #28a745 !important;
  border-color: #28a745 !important;
}

.cart-checkbox:focus {
  box-shadow: 0 0 0 0.2rem rgba(40, 167, 69, 0.25) !important;
}

.form-check {
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  min-height: 20px !important;
}

.cart-product-image img {
  width: 60px;
  height: 60px;
  object-fit: cover;
}

.cart-product-info h6 {
  font-size: 0.95rem;
  color: #333;
}

.variant-text {
  font-size: 0.8rem;
  color: #666;
  background: #f8f9fa;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
}

.variant-controls {
  overflow: visible !important;
  position: relative;
}

.variant-controls .dropdown {
  display: inline-block;
  position: relative;
  overflow: visible !important;
}

.variant-controls .dropdown-menu {
  display: none;
  position: fixed !important;
  top: auto !important;
  left: auto !important;
  z-index: 9999999 !important;
  min-width: 160px;
  padding: 0.5rem 0;
  margin: 0.125rem 0 0;
  background-color: #fff;
  border: 1px solid rgba(0, 0, 0, 0.15);
  border-radius: 0.375rem;
  box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.175);
  max-height: 300px;
  overflow-y: auto;
  overflow-x: visible !important;
}

.variant-controls .dropdown.show .dropdown-menu {
  display: block !important;
  opacity: 1 !important;
  visibility: visible !important;
}

.variant-controls .dropdown-toggle::after {
  display: inline-block;
  margin-left: 0.255em;
  vertical-align: 0.255em;
  content: "";
  border-top: 0.3em solid;
  border-right: 0.3em solid transparent;
  border-bottom: 0;
  border-left: 0.3em solid transparent;
}

.variant-controls .dropdown .btn {
  min-width: 100px;
  text-align: left;
  position: relative;
  border: 2px solid #e9ecef !important;
  background-color: white !important;
  transition: all 0.2s ease;
}

.variant-controls .dropdown .btn:hover {
  border-color: #B8860B !important;
  background-color: white !important;
}

.variant-controls .dropdown .btn:focus {
  border-color: #B8860B !important;
  background-color: white !important;
  box-shadow: 0 0 0 3px rgba(184, 134, 11, 0.1) !important;
  outline: none !important;
}

.variant-controls .dropdown .btn:active {
  border-color: #B8860B !important;
  background-color: white !important;
  box-shadow: 0 0 0 3px rgba(184, 134, 11, 0.1) !important;
}

.variant-controls .dropdown.show .btn {
  border-color: #B8860B !important;
  background-color: white !important;
  box-shadow: 0 0 0 3px rgba(184, 134, 11, 0.1) !important;
}

.variant-controls .dropdown .btn.text-muted {
  color: #6c757d !important;
  font-style: italic;
  border: 2px solid #e9ecef !important;
}

/* Force border visibility for all button states */
.variant-controls .dropdown .btn:not(:focus):not(:hover):not(.show) {
  border: 2px solid #e9ecef !important;
  background-color: white !important;
}

/* Override any conflicting styles */
.variant-controls .dropdown .btn,
.variant-controls .dropdown .btn:focus,
.variant-controls .dropdown .btn:hover,
.variant-controls .dropdown .btn:active,
.variant-controls .dropdown .btn:visited {
  border: 2px solid #e9ecef !important;
  background-color: white !important;
  outline: none !important;
}

.variant-controls .dropdown .btn:focus,
.variant-controls .dropdown .btn:hover,
.variant-controls .dropdown .btn:active,
.variant-controls .dropdown.show .btn {
  border-color: #B8860B !important;
  box-shadow: 0 0 0 3px rgba(184, 134, 11, 0.1) !important;
}

.variant-controls .dropdown .btn::after {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
}

/* Color swatch styling */
.color-swatch {
  width: 16px;
  height: 16px;
  border-radius: 3px;
  display: inline-block;
  border: 1px solid #ddd;
  flex-shrink: 0;
}

.dropdown-item {
  display: flex;
  align-items: center;
  padding: 0.5rem 1rem;
}

.dropdown-item:hover {
  background-color: #f8f9fa;
}

.dropdown-item:hover .color-swatch {
  transform: scale(1.1);
  transition: transform 0.2s ease;
}

/* Support for dropdown opening upward */
.variant-controls .dropdown-menu.dropdown-upward {
  top: auto !important;
  bottom: 100% !important;
  transform: translateY(-0.5rem) !important;
}

/* Reset Bootstrap button styles that might interfere */
.variant-controls .btn-outline-secondary {
  border: 2px solid #e9ecef !important;
  background-color: white !important;
  color: #495057 !important;
}

.variant-controls .btn-outline-secondary:hover {
  border-color: #B8860B !important;
  background-color: white !important;
  color: #495057 !important;
}

.variant-controls .btn-outline-secondary:focus {
  border-color: #B8860B !important;
  background-color: white !important;
  color: #495057 !important;
  box-shadow: 0 0 0 3px rgba(184, 134, 11, 0.1) !important;
}

.variant-controls .btn-outline-secondary:active {
  border-color: #B8860B !important;
  background-color: white !important;
  color: #495057 !important;
  box-shadow: 0 0 0 3px rgba(184, 134, 11, 0.1) !important;
}

.variant-controls .btn-outline-secondary:not(:disabled):not(.disabled):active {
  border-color: #B8860B !important;
  background-color: white !important;
  color: #495057 !important;
  box-shadow: 0 0 0 3px rgba(184, 134, 11, 0.1) !important;
}

.delete-btn {
  font-size: 0.8rem;
  text-decoration: none;
}

.delete-btn:hover {
  color: #dc3545 !important;
  text-decoration: underline;
}

.quantity-controls {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: #f8f9fa;
  border-radius: 8px;
  padding: 0.25rem;
  width: fit-content;
}

.qty-btn {
  width: 28px;
  height: 28px;
  border: none;
  background: white;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.75rem;
  color: #6c757d;
}

.qty-btn:hover:not(:disabled) {
  background: #e9ecef;
  color: #212529;
}

.qty-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.qty-display {
  min-width: 24px;
  text-align: center;
  font-weight: 600;
  font-size: 0.875rem;
}

.current-price {
  color: #333;
  font-size: 0.95rem;
  line-height: 1.2;
}

.original-price {
  text-decoration: line-through !important;
  color: #999 !important;
  font-size: 0.85rem !important;
  line-height: 1.2;
  margin-top: 2px;
  display: block;
  opacity: 0.8;
  font-weight: 400;
}

/* Price container styling */
.cart-price {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
}

.cart-price .current-price {
  color: #e74c3c !important;
  font-weight: 700 !important;
}

.cart-price .original-price {
  color: #95a5a6 !important;
  font-size: 0.8rem !important;
  font-weight: 400 !important;
}

.continue-btn {
  border-radius: 8px;
  padding: 0.75rem;
  font-weight: 500;
}

.clear-all-btn {
  border-radius: 8px;
  padding: 0.4rem 0.8rem;
  font-size: 0.85rem;
  font-weight: 600;
  transition: all 0.3s ease;
  border: 2px solid rgba(255, 255, 255, 0.8);
  color: white;
  background: rgba(220, 53, 69, 0.9);
  box-shadow: 0 2px 8px rgba(220, 53, 69, 0.3);
}

.clear-all-btn:hover {
  background: rgba(220, 53, 69, 1);
  border-color: white;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(220, 53, 69, 0.4);
}

.clear-all-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 6px rgba(220, 53, 69, 0.3);
}

.clear-all-btn i {
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.3));
}

@media (max-width: 768px) {
  .cart-header-layout {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.75rem;
  }
  
  .cart-header-left {
    width: 100%;
    justify-content: space-between;
  }
  
  .cart-header-right {
    width: 100%;
    justify-content: flex-end;
    margin-left: 0;
  }
  
  .clear-all-btn {
    font-size: 0.8rem;
    padding: 0.3rem 0.6rem;
  }
  
  .badge {
    font-size: 0.75rem;
    padding: 0.3rem 0.6rem;
  }
  
  .form-check-label {
    font-size: 0.8rem;
  }
  
  .form-check-input {
    width: 16px;
    height: 16px;
  }
  
  .cart-item .row > div {
    margin-bottom: 0.5rem;
  }
  
  .cart-item .col-1,
  .cart-item .col-2,
  .cart-item .col-5,
  .cart-item .col-2 {
    flex: 0 0 100%;
    max-width: 100%;
  }
  
  .cart-item .col-1,
  .cart-item .col-2 {
    flex: 0 0 auto;
    max-width: none;
  }
  
  .quantity-controls {
    justify-content: center;
    margin: 0 auto;
  }
}

/* Fix dropdown z-index issues - Override Bootstrap CSS */
.variant-controls .dropdown-menu {
  pointer-events: auto !important;
  isolation: isolate !important;
  contain: layout style !important;
  z-index: 9999999 !important;
  position: fixed !important;
}

/* Override Bootstrap dropdown z-index completely */
.dropdown-menu {
  z-index: 9999999 !important;
}

.variant-controls .dropdown.show .dropdown-menu {
  z-index: 9999999 !important;
  position: fixed !important;
  display: block !important;
  opacity: 1 !important;
  visibility: visible !important;
}

/* Prevent any stacking context issues */
.cart-item {
  isolation: isolate !important;
  position: relative;
  z-index: 1;
}

.variant-controls {
  isolation: isolate !important;
  position: relative;
  z-index: 10;
}

.variant-controls .dropdown {
  isolation: isolate !important;
  position: relative;
  z-index: 1000;
}

.variant-controls .dropdown.show {
  z-index: 1001;
}

/* Nuclear option - Override ALL possible z-index conflicts */
.cart-items-section * {
  position: relative;
}

.cart-items-section .variant-controls .dropdown-menu {
  position: fixed !important;
  z-index: 9999999 !important;
  transform: translateZ(0) !important;
  will-change: transform !important;
}

/* Force all cart items to lower z-index */
.cart-item:not(.show-dropdown) {
  z-index: 1 !important;
}

.cart-item.show-dropdown {
  z-index: 9999998 !important;
}

/* Ensure no element can interfere */
.cart-items-section .row,
.cart-items-section .col-1,
.cart-items-section .col-2,
.cart-items-section .col-5 {
  z-index: 1 !important;
  position: relative !important;
}

.cart-items-section .variant-controls {
  z-index: 9999998 !important;
  position: relative !important;
}

/* Final nuclear option - Override Bootstrap completely */
.cart-items-section .dropdown-menu {
  z-index: 9999999 !important;
  position: fixed !important;
  display: none;
}

.cart-items-section .dropdown.show .dropdown-menu {
  z-index: 9999999 !important;
  position: fixed !important;
  display: block !important;
  opacity: 1 !important;
  visibility: visible !important;
}

/* Force hardware acceleration */
.cart-items-section .dropdown-menu {
  transform: translateZ(0) !important;
  will-change: transform, top, left !important;
  backface-visibility: hidden !important;
  -webkit-backface-visibility: hidden !important;
}
</style>
