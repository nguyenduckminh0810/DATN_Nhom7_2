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
          <div v-for="item in paginatedItems" :key="item.itemKey" class="cart-item">
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
                  
                  <!-- Variant Controls -->
                  <div class="variant-controls mb-2">
                    <!-- Color Dropdown -->
                    <div class="dropdown me-2" v-if="getProductVariants(item.productId)">
                      <button class="btn btn-outline-secondary btn-sm dropdown-toggle" 
                              type="button" 
                              :id="`color-dropdown-${item.id}`"
                              data-bs-toggle="dropdown"
                              aria-expanded="false">
                        <span v-if="item.color" class="d-flex align-items-center">
                          <span class="color-dot me-2" :style="{ backgroundColor: getColorHex(item.color) }"></span>
                          <strong>Màu:</strong>&nbsp;{{ item.color }}
                        </span>
                        <span v-else>Chọn màu</span>
                      </button>
                      <ul class="dropdown-menu" :aria-labelledby="`color-dropdown-${item.id}`">
                        <li v-for="color in getAvailableColors(item.productId)" :key="color.name">
                          <a class="dropdown-item" 
                             href="#" 
                             @click.prevent="changeVariant(item, 'color', color.name)"
                             :class="{ 'active': item.color === color.name }">
                            <span class="color-dot me-2" :style="{ backgroundColor: color.hex }"></span>
                            {{ color.name }}
                          </a>
                        </li>
                      </ul>
                    </div>
                    
                    <!-- Size Dropdown -->
                    <div class="dropdown" v-if="getProductVariants(item.productId)">
                      <button class="btn btn-outline-secondary btn-sm dropdown-toggle" 
                              type="button" 
                              :id="`size-dropdown-${item.id}`"
                              data-bs-toggle="dropdown"
                              aria-expanded="false">
                        <strong>Size:</strong>&nbsp;{{ item.size || 'Chọn size' }}
                      </button>
                      <ul class="dropdown-menu" :aria-labelledby="`size-dropdown-${item.id}`">
                        <li v-for="size in getAvailableSizes(item.productId, item.color)" :key="size">
                          <a class="dropdown-item" 
                             href="#" 
                             @click.prevent="changeVariant(item, 'size', size)"
                             :class="{ 'active': item.size === size }">
                            {{ size }}
                          </a>
                        </li>
                      </ul>
                    </div>
                  </div>

                  <!-- Fallback if no variants loaded -->
                  <div v-if="!getProductVariants(item.productId)" class="product-variant mb-2">
                    <span class="variant-text">
                      <strong>Màu:</strong> {{ item.color || 'Không xác định' }} / 
                      <strong>Size:</strong> {{ item.size || 'Không xác định' }}
                    </span>
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
                  <input 
                    type="number" 
                    class="qty-input"
                    v-model.number="item.quantity"
                    @blur="validateAndUpdateQuantity(item)"
                    @keypress="handleQuantityKeyPress"
                    min="1"
                    :max="getVariantStock(item)"
                  />
                  <button 
                    class="qty-btn plus"
                    @click="increaseQuantity(item.itemKey)"
                    :disabled="isUpdating || isAtMaxStock(item)"
                    :title="isUpdating ? 'Đang cập nhật...' : (isAtMaxStock(item) ? `Đã đạt tối đa tồn kho (${getVariantStock(item)})` : 'Click để tăng số lượng')"
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

      <!-- Pagination -->
      <div v-if="totalPages > 1" class="pagination-wrapper mt-3 px-3 pb-3">
        <nav aria-label="Cart pagination">
          <ul class="pagination pagination-sm justify-content-center mb-0">
            <!-- Previous Page -->
            <li class="page-item" :class="{ disabled: currentPage === 1 }">
              <button
                class="page-link"
                @click.prevent="goToPage(currentPage - 1)"
                :disabled="currentPage === 1"
                title="Trang trước"
                type="button"
              >
                <i class="bi bi-chevron-left"></i>
              </button>
            </li>

            <!-- Page Numbers -->
            <li
              v-for="page in visiblePages"
              :key="page"
              class="page-item"
              :class="{ active: page === currentPage }"
            >
              <button v-if="page !== '...'" class="page-link" @click.prevent="goToPage(page)" type="button">
                {{ page }}
              </button>
              <span v-else class="page-link" style="pointer-events: none;">{{ page }}</span>
            </li>

            <!-- Next Page -->
            <li class="page-item" :class="{ disabled: currentPage === totalPages }">
              <button
                class="page-link"
                @click.prevent="goToPage(currentPage + 1)"
                :disabled="currentPage === totalPages"
                title="Trang sau"
                type="button"
              >
                <i class="bi bi-chevron-right"></i>
              </button>
            </li>
          </ul>
        </nav>

        <!-- Pagination Info -->
        <div class="text-center mt-2">
          <small class="text-muted">
            Hiển thị {{ startIndex + 1 }} - {{ endIndex }} trong {{ items.length }} sản phẩm
          </small>
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
import { ref, computed, onMounted, watch } from 'vue'
import { useCart } from '@/composables/useCart'
import { useCartStore } from '@/stores/cart'
import sanPhamService from '@/services/sanPhamService'
import cartService from '@/services/cartService'

const { items, updateQuantity, removeItem, clearCart, formatPrice } = useCart()
const cartStore = useCartStore()

// Store product variants data
const productVariantsMap = ref(new Map())

// Loading state để prevent spam click
const isUpdating = ref(false)

// Pagination state
const currentPage = ref(1)
const itemsPerPage = 3

// 🔍 DEBUG: Log items from cart
console.log('🛒 [CART ITEMS] Total items:', items.value?.length || 0)
if (items.value && items.value.length > 0) {
  items.value.forEach((item, index) => {
    console.log(`📦 [CART ITEM ${index + 1}]:`, {
      id: item.id,
      itemKey: item.itemKey,
      name: item.name,
      color: item.color,
      size: item.size,
      quantity: item.quantity,
      productId: item.productId,
      bienTheId: item.bienTheId,
      variantId: item.variantId,
      selected: item.selected
    })
  })
}

// Ensure all items have selected property - MẶC ĐỊNH LÀ TRUE (đã chọn)
// Sử dụng watch để đảm bảo luôn có selected property khi items thay đổi
const ensureSelectedProperty = () => {
  if (!items.value || items.value.length === 0) return
  
  let hasChanged = false
  items.value.forEach(item => {
    if (item.selected === undefined || item.selected === null) {
      item.selected = true  // Mặc định tất cả sản phẩm được chọn
      hasChanged = true
    }
  })
  
  // Nếu có thay đổi, lưu lại vào store để persist
  if (hasChanged) {
    cartStore.saveToStorage()
  }
}

// Watch items để đảm bảo selected property luôn được set khi items thay đổi
// và lưu state vào localStorage khi có thay đổi
watch(items, () => {
  ensureSelectedProperty()
  
  // Lưu state vào localStorage khi items thay đổi (bao gồm cả selected property)
  cartStore.saveToStorage()
}, { deep: true, immediate: false })

// Load product variants for all cart items
const loadProductVariants = async () => {
  console.log('🔄 [LOAD VARIANTS] Starting to load variants...')
  console.log('🔄 [LOAD VARIANTS] Cart items:', items.value)
  
  // Extract productId from SKU (format: SP{productId}-{size}-{color}-{random})
  const getProductIdFromSku = (sku) => {
    if (!sku) return null
    const match = sku.match(/^SP(\d+)-/)
    return match ? parseInt(match[1]) : null
  }
  
  // Get unique product IDs from cart items
  const productIds = new Set()
  items.value.forEach(item => {
    let productId = item.productId
    
    // If no productId, try to extract from SKU
    if (!productId && item.sku) {
      productId = getProductIdFromSku(item.sku)
      console.log(`📝 [LOAD VARIANTS] Extracted productId ${productId} from SKU: ${item.sku}`)
    }
    
    // If still no productId, try to get from bienTheId (use as fallback)
    if (!productId && item.bienTheId) {
      productId = item.bienTheId
      console.log(`� [LOAD VARIANTS] Using bienTheId ${productId} as productId`)
    }
    
    if (productId) {
      productIds.add(productId)
      // Store productId back to item for future use
      item.productId = productId
    }
  })
  
  const uniqueProductIds = Array.from(productIds)
  console.log('🔄 [LOAD VARIANTS] Unique product IDs:', uniqueProductIds)
  
  if (uniqueProductIds.length === 0) {
    console.warn('⚠️ [LOAD VARIANTS] No product IDs found in cart items')
    return
  }
  
  for (const productId of uniqueProductIds) {
    try {
      console.log(`🔄 [LOAD VARIANTS] Loading variants for product ${productId}...`)
      const productDetail = await sanPhamService.getDetail(productId)
      
      if (productDetail && productDetail.bienThes) {
        console.log(`✅ [LOAD VARIANTS] Loaded ${productDetail.bienThes.length} variants for product ${productId}`)
        productVariantsMap.value.set(productId, productDetail.bienThes)
      } else {
        console.warn(`⚠️ [LOAD VARIANTS] No variants found for product ${productId}`)
      }
    } catch (error) {
      console.error(`❌ [LOAD VARIANTS] Error loading variants for product ${productId}:`, error)
    }
  }
  
  console.log('✅ [LOAD VARIANTS] Finished loading variants')
  console.log('📦 [LOAD VARIANTS] Product variants map:', productVariantsMap.value)
}

// Get variants for a specific product
const getProductVariants = (productId) => {
  if (!productId) return null
  return productVariantsMap.value.get(productId) || null
}

// Get available colors for a product
const getAvailableColors = (productId) => {
  const variants = getProductVariants(productId)
  if (!variants) return []
  
  // Extract unique colors with their hex values
  const colorMap = new Map()
  variants.forEach(variant => {
    if (variant.mauSac && variant.tonKho > 0) {
      if (!colorMap.has(variant.mauSac)) {
        colorMap.set(variant.mauSac, {
          name: variant.mauSac,
          hex: variant.colorHex || '#000000'
        })
      }
    }
  })
  
  return Array.from(colorMap.values())
}

// Get available sizes for a product (filtered by selected color if any)
const getAvailableSizes = (productId, selectedColor) => {
  const variants = getProductVariants(productId)
  if (!variants) return []
  
  let filteredVariants = variants.filter(v => v.tonKho > 0)
  
  // If color is selected, filter by that color
  if (selectedColor) {
    filteredVariants = filteredVariants.filter(v => v.mauSac === selectedColor)
  }
  
  // Extract unique sizes
  const sizes = [...new Set(filteredVariants.map(v => v.kichThuoc).filter(Boolean))]
  return sizes.sort()
}

// Get color hex value by name
const getColorHex = (colorName) => {
  // Search in all loaded variants
  for (const variants of productVariantsMap.value.values()) {
    const variant = variants.find(v => v.mauSac === colorName)
    if (variant && variant.colorHex) {
      return variant.colorHex
    }
  }
  
  // Fallback colors
  const colorMap = {
    'Đen': '#000000',
    'Trắng': '#ffffff',
    'Xám': '#808080',
    'Đỏ': '#dc3545',
    'Xanh navy': '#000080',
    'Xanh dương': '#007bff',
    'Xanh lá': '#28a745',
    'Vàng': '#ffc107',
    'Cam': '#fd7e14',
    'Tím': '#6f42c1',
    'Hồng': '#e83e8c',
    'Nâu': '#8b4513',
    'Be': '#f5f5dc'
  }
  
  return colorMap[colorName] || '#6c757d'
}

// Change variant (color or size)
const changeVariant = async (item, type, value) => {
  try {
    console.log(`🔄 [CHANGE VARIANT] Changing ${type} to ${value} for item:`, item)
    
    const variants = getProductVariants(item.productId)
    if (!variants) {
      if (window.$toast) {
        window.$toast.error('Không tìm thấy thông tin biến thể', 'Lỗi')
      }
      return
    }
    
    // Determine new color and size based on what's being changed
    const newColor = type === 'color' ? value : item.color
    const newSize = type === 'size' ? value : item.size
    
    // Find the matching variant
    const newVariant = variants.find(v => 
      v.mauSac === newColor && v.kichThuoc === newSize && v.tonKho > 0
    )
    
    if (!newVariant) {
      if (window.$toast) {
        window.$toast.error('Không tìm thấy biến thể phù hợp', 'Lỗi')
      }
      return
    }
    
    console.log('✅ [CHANGE VARIANT] Found new variant:', newVariant)
    
    // Step 1: Remove current item from cart
    await cartService.removeFromCart(item.id)
    
    // Step 2: Add new variant to cart
    await cartService.addToCart({
      bienTheId: newVariant.id,
      soLuong: item.quantity
    })
    
    // Step 3: Reload cart from backend
    const { useCartStore } = await import('@/stores/cart')
    const cartStore = useCartStore()
    await cartStore.loadCart()
    
    if (window.$toast) {
      window.$toast.success(
        `Đã đổi sang ${newColor} - ${newSize}`,
        'Thành công'
      )
    }
    
  } catch (error) {
    console.error('❌ [CHANGE VARIANT] Error:', error)
    if (window.$toast) {
      window.$toast.error('Không thể thay đổi biến thể', 'Lỗi')
    }
  }
}

// Load variants when component mounts và đảm bảo selected property
onMounted(async () => {
  // Đảm bảo selected property được set trước
  ensureSelectedProperty()
  // Load product variants
  await loadProductVariants()
})

// Get variant stock for an item - Lấy trực tiếp từ backend
const getVariantStock = (item) => {
  return item.stock || 999
}

// Check if item quantity is at max stock
const isAtMaxStock = (item) => {
  const stock = getVariantStock(item)
  return item.quantity >= stock
}

const increaseQuantity = async (itemKey) => {
  if (isUpdating.value) {
    console.warn('⏳ [INCREASE QTY] Already updating, please wait...')
    return
  }
  
  const item = items.value.find(item => item.itemKey === itemKey)
  if (!item) return
  
  isUpdating.value = true
  
  try {
    // Lấy stock hiện tại từ item (có thể không chính xác)
    let stock = getVariantStock(item)
    const newQuantity = item.quantity + 1
    
    // Nếu stock là giá trị default (999) hoặc undefined, reload cart để lấy stock mới
    if (!stock || stock >= 999 || !item.stock) {
      console.log('🔄 [INCREASE QTY] Stock not available or default, reloading cart...')
      const { useCartStore } = await import('@/stores/cart')
      const cartStore = useCartStore()
      await cartStore.loadCart()
      
      // Lấy lại item sau khi reload
      const refreshedItem = items.value.find(i => i.itemKey === itemKey)
      if (refreshedItem) {
        stock = getVariantStock(refreshedItem)
        console.log('✅ [INCREASE QTY] Reloaded stock:', stock)
      }
    }
    
    console.log('➕ [INCREASE QTY]:', {
      itemName: item.name,
      color: item.color,
      size: item.size,
      current: item.quantity,
      new: newQuantity,
      stock: stock,
      itemStock: item.stock,
      canIncrease: newQuantity <= stock
    })
    
    // KIỂM TRA NGHIÊM NGẶT - Không cho tăng nếu vượt quá stock
    if (!stock || stock === null || stock === undefined || newQuantity > stock) {
      const displayStock = stock || 0
      if (window.$toast) {
        window.$toast.warning(`Chỉ còn ${displayStock} sản phẩm trong kho`, 'Không thể tăng thêm')
      }
      console.warn('⚠️ [INCREASE QTY] BLOCKED - Exceeds stock limit')
      return
    }
    
    await updateQuantity(itemKey, newQuantity)
  } finally {
    isUpdating.value = false
  }
}

const decreaseQuantity = async (itemKey) => {
  const item = items.value.find(item => item.itemKey === itemKey)
  if (!item || item.quantity <= 1) return
  
  const newQuantity = item.quantity - 1
  
  console.log('➖ [DECREASE QTY]:', {
    current: item.quantity,
    new: newQuantity
  })
  
  await updateQuantity(itemKey, newQuantity)
}

// Validate and update quantity when user types directly
const validateAndUpdateQuantity = async (item) => {
  if (isUpdating.value) return
  
  const stock = getVariantStock(item)
  let newQty = item.quantity
  
  // Validate empty or NaN
  if (!newQty || isNaN(newQty)) {
    newQty = 1
  }
  
  // Validate minimum
  if (newQty < 1) {
    newQty = 1
    if (window.$toast) {
      window.$toast.warning('Số lượng tối thiểu là 1', 'Cảnh báo')
    }
  }
  
  // Validate maximum (stock)
  if (stock > 0 && newQty > stock) {
    newQty = stock
    if (window.$toast) {
      window.$toast.warning(`Chỉ còn ${stock} sản phẩm trong kho`, 'Vượt quá tồn kho')
    }
  }
  
  // Update if changed
  if (newQty !== item.quantity) {
    item.quantity = newQty
    await updateQuantity(item.itemKey, newQty)
  }
}

// Prevent non-numeric input
const handleQuantityKeyPress = (event) => {
  // Allow: backspace, delete, tab, escape, enter
  if ([46, 8, 9, 27, 13].includes(event.keyCode) ||
      // Allow: Ctrl+A, Ctrl+C, Ctrl+V, Ctrl+X
      (event.keyCode === 65 && event.ctrlKey === true) ||
      (event.keyCode === 67 && event.ctrlKey === true) ||
      (event.keyCode === 86 && event.ctrlKey === true) ||
      (event.keyCode === 88 && event.ctrlKey === true) ||
      // Allow: home, end, left, right
      (event.keyCode >= 35 && event.keyCode <= 39)) {
    return
  }
  // Ensure that it is a number
  if ((event.shiftKey || (event.keyCode < 48 || event.keyCode > 57)) && (event.keyCode < 96 || event.keyCode > 105)) {
    event.preventDefault()
  }
}

const clearAllItems = async () => {
  if (window.confirm('Bạn có chắc chắn muốn xóa tất cả sản phẩm khỏi giỏ hàng?')) {
    await clearCart()
  }
}

const handleImageError = (event) => {
  event.target.src = 'https://via.placeholder.com/150?text=No+Image'
}

// Logic cho checkbox "Chọn tất cả"
const allItemsSelected = computed(() => {
  if (!items.value || items.value.length === 0) return false
  return items.value.every(item => item.selected !== false)
})

const toggleSelectAll = async () => {
  if (!items.value || items.value.length === 0) return
  
  const shouldSelectAll = !allItemsSelected.value
  items.value.forEach(item => {
    item.selected = shouldSelectAll
  })
  
  // ✅ Lưu state vào localStorage sau khi toggle
  cartStore.saveToStorage()
  
  if (window.$toast) {
    const message = shouldSelectAll ? 'Đã chọn tất cả sản phẩm' : 'Đã bỏ chọn tất cả sản phẩm'
    window.$toast.success(message)
  }
}

// Pagination computed properties
const totalPages = computed(() => {
  if (!items.value || items.value.length === 0) return 0
  return Math.ceil(items.value.length / itemsPerPage)
})

const startIndex = computed(() => {
  return (currentPage.value - 1) * itemsPerPage
})

const endIndex = computed(() => {
  if (!items.value || items.value.length === 0) return 0
  return Math.min(startIndex.value + itemsPerPage, items.value.length)
})

const paginatedItems = computed(() => {
  if (!items.value || items.value.length === 0) return []
  return items.value.slice(startIndex.value, endIndex.value)
})

const visiblePages = computed(() => {
  const pages = []
  const totalPagesVal = totalPages.value
  const current = currentPage.value

  if (totalPagesVal <= 7) {
    // Hiển thị tất cả các trang nếu <= 7
    for (let i = 1; i <= totalPagesVal; i++) {
      pages.push(i)
    }
  } else {
    // Hiển thị với ellipsis
    if (current <= 3) {
      // Đầu danh sách
      for (let i = 1; i <= 4; i++) {
        pages.push(i)
      }
      pages.push('...')
      pages.push(totalPagesVal)
    } else if (current >= totalPagesVal - 2) {
      // Cuối danh sách
      pages.push(1)
      pages.push('...')
      for (let i = totalPagesVal - 3; i <= totalPagesVal; i++) {
        pages.push(i)
      }
    } else {
      // Giữa danh sách
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

// Pagination methods
const goToPage = (page) => {
  if (page >= 1 && page <= totalPages.value && page !== currentPage.value) {
    currentPage.value = page
    // Scroll to top of cart items section
    const cartSection = document.querySelector('.cart-items-section')
    if (cartSection) {
      cartSection.scrollIntoView({ behavior: 'smooth', block: 'start' })
    }
  }
}

// Reset to page 1 when items change
watch(() => items.value?.length, () => {
  if (currentPage.value > totalPages.value) {
    currentPage.value = 1
  }
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
  font-size: 0.85rem;
  color: #495057;
  background: #f8f9fa;
  padding: 0.4rem 0.75rem;
  border-radius: 6px;
  display: inline-block;
  border: 1px solid #e9ecef;
}

.variant-text strong {
  color: #212529;
  font-weight: 600;
}

.variant-info {
  font-size: 0.8rem;
}

.variant-info small {
  color: #6c757d;
  font-style: italic;
}

/* Variant Controls */
.variant-controls {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.variant-controls .dropdown {
  position: relative;
}

.variant-controls .btn {
  min-width: 120px;
  text-align: left;
  border: 1px solid #dee2e6;
  background: white;
  font-size: 0.85rem;
  padding: 0.375rem 0.75rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.variant-controls .btn:hover {
  border-color: #B8860B;
  background: #fffbf0;
}

.variant-controls .btn strong {
  font-weight: 600;
  color: #495057;
}

.variant-controls .dropdown-menu {
  min-width: 160px;
  max-height: 250px;
  overflow-y: auto;
  font-size: 0.875rem;
  box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.variant-controls .dropdown-item {
  display: flex;
  align-items: center;
  padding: 0.5rem 1rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.variant-controls .dropdown-item:hover {
  background-color: #f8f9fa;
}

.variant-controls .dropdown-item.active {
  background-color: #B8860B;
  color: white;
  font-weight: 600;
}

.variant-controls .dropdown-item.active .color-dot {
  border-color: white;
}

.color-dot {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  display: inline-block;
  border: 2px solid #dee2e6;
  flex-shrink: 0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
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
  position: relative;
  z-index: 10;
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
  position: relative;
  z-index: 1;
  pointer-events: auto;
}

.qty-btn:hover:not(:disabled) {
  background: #e9ecef;
  color: #212529;
  transform: scale(1.1);
}

.qty-btn:active:not(:disabled) {
  transform: scale(0.95);
}

.qty-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.qty-input {
  width: 50px;
  text-align: center;
  font-weight: 600;
  font-size: 0.875rem;
  border: 1px solid #dee2e6;
  border-radius: 4px;
  padding: 4px;
  outline: none;
  transition: border-color 0.2s;
}

.qty-input:focus {
  border-color: #ffc107;
  box-shadow: 0 0 0 0.2rem rgba(255, 193, 7, 0.25);
}

.qty-input::-webkit-inner-spin-button,
.qty-input::-webkit-outer-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.qty-input[type=number] {
  -moz-appearance: textfield;
}

.qty-display {
  min-width: 24px;
  text-align: center;
  font-weight: 600;
  font-size: 0.875rem;
}

.stock-info {
  margin-top: 0.25rem;
}

.stock-info small {
  font-size: 0.75rem;
  color: #6c757d;
  font-style: italic;
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

/* Pagination Styles */
.pagination-wrapper {
  border-top: 1px solid #f0f0f0;
  padding-top: 1rem;
}

.pagination {
  margin-bottom: 0;
}

.page-link {
  border: 1px solid #dee2e6;
  color: #6c757d;
  padding: 0.375rem 0.75rem;
  font-size: 0.875rem;
  transition: all 0.3s ease;
  background: white;
}

.page-link:hover:not(:disabled) {
  background: #f8f9fa;
  color: #B8860B;
  border-color: #B8860B;
}

.page-item.active .page-link {
  background: #B8860B;
  border-color: #B8860B;
  color: white;
  font-weight: 600;
}

.page-item.disabled .page-link {
  opacity: 0.5;
  cursor: not-allowed;
  background: #f8f9fa;
}

.pagination-info {
  font-size: 0.85rem;
  color: #6c757d;
}
</style>
