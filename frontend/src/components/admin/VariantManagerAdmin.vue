<template>
  <div class="variant-manager-admin">
    <!-- Header -->
    <div class="manager-header">
      <h4 class="manager-title">Quản lý biến thể sản phẩm</h4>
      <div class="quick-stats">
        <span class="stat-item">
          <i class="bi bi-palette"></i>
          {{ selectedColors.length }} màu
        </span>
        <span class="stat-item">
          <i class="bi bi-rulers"></i>
          {{ availableSizes.length }} size
        </span>
        <span class="stat-item">
          <i class="bi bi-box"></i>
          {{ totalVariants }} biến thể
        </span>
        <span class="stat-item" :class="totalStockClass">
          <i class="bi bi-archive"></i>
          {{ totalStock }} sản phẩm
        </span>
      </div>
    </div>

    <!-- Category Detection -->
    <div class="category-section">
      <div class="alert alert-info">
        <i class="bi bi-info-circle me-2"></i>
        <strong>Category:</strong> {{ categoryName }}
        → Size type: <strong>{{ sizeType === 'letter' ? 'Chữ (S,M,L...)' : 'Số (28,29,30...)' }}</strong>
      </div>
    </div>

    <!-- Color Management -->
    <div class="section-card">
      <div class="section-header">
        <h5 class="section-title">
          <i class="bi bi-palette me-2"></i>
          Màu sắc
        </h5>
        <button type="button" class="btn btn-sm btn-outline-primary" @click="showColorPicker = !showColorPicker">
          <i class="bi bi-plus me-1"></i>
          Thêm màu
        </button>
      </div>

      <!-- Color Presets -->
      <div class="color-presets">
        <div
          v-for="color in colorPresets"
          :key="color.hex"
          class="color-preset-item"
          :class="{ selected: isColorSelected(color.hex) }"
          @click="toggleColor(color)"
        >
          <div class="color-swatch" :style="{ backgroundColor: color.hex }">
            <i v-if="isColorSelected(color.hex)" class="bi bi-check color-check"></i>
          </div>
          <span class="color-name">{{ color.name }}</span>
        </div>
      </div>

      <!-- Custom Color Picker -->
      <div v-if="showColorPicker" class="custom-color-picker">
        <div class="input-group">
          <input
            type="text"
            v-model="customColorName"
            class="form-control"
            placeholder="Tên màu (VD: Xanh mint)"
          />
          <input
            type="color"
            v-model="customColorHex"
            class="form-control form-control-color"
          />
          <button type="button" class="btn btn-primary" @click="addCustomColor">
            <i class="bi bi-plus"></i>
          </button>
        </div>
      </div>

      <!-- Selected Colors -->
      <div v-if="selectedColors.length > 0" class="selected-colors">
        <h6 class="selected-title">Màu đã chọn:</h6>
        <div class="selected-colors-list">
          <div
            v-for="color in selectedColors"
            :key="color.hex"
            class="selected-color-badge"
          >
            <div class="color-dot" :style="{ backgroundColor: color.hex }"></div>
            <span>{{ color.name }}</span>
            <button type="button" class="remove-btn" @click="removeColor(color.hex)">
              <i class="bi bi-x"></i>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Material Selection -->
    <div class="section-card">
      <div class="section-header">
        <h5 class="section-title">
          <i class="bi bi-tags me-2"></i>
          Chất liệu
        </h5>
      </div>
      <div class="material-grid">
        <label
          v-for="material in materialOptions"
          :key="material"
          class="material-option"
          :class="{ selected: selectedMaterial === material }"
        >
          <input
            type="radio"
            :value="material"
            v-model="selectedMaterial"
            class="material-radio"
          />
          <span class="material-label">{{ material }}</span>
        </label>
      </div>
    </div>

    <!-- Variant Matrix -->
    <div v-if="selectedColors.length > 0" class="section-card variant-matrix-section">
      <div class="section-header">
        <h5 class="section-title">
          <i class="bi bi-grid-3x3 me-2"></i>
          Ma trận biến thể ({{ sizeType === 'letter' ? 'Size Chữ' : 'Size Số VN' }})
        </h5>
        <div class="matrix-actions">
          <button type="button" class="btn btn-sm btn-outline-secondary" @click="fillAllStock">
            <i class="bi bi-arrow-down-circle me-1"></i>
            Điền tất cả
          </button>
          <button type="button" class="btn btn-sm btn-outline-info" @click="copyFromVariant">
            <i class="bi bi-files me-1"></i>
            Copy từ biến thể
          </button>
          <button type="button" class="btn btn-sm btn-outline-success" @click="exportVariants">
            <i class="bi bi-download me-1"></i>
            Xuất Excel
          </button>
        </div>
      </div>

      <!-- Matrix Table -->
      <div class="matrix-table-wrapper">
        <table class="matrix-table">
          <thead>
            <tr>
              <th class="size-header">Size / Màu</th>
              <th
                v-for="color in selectedColors"
                :key="color.hex"
                class="color-header"
              >
                <div class="color-header-content">
                  <div class="color-dot" :style="{ backgroundColor: color.hex }"></div>
                  <span>{{ color.name }}</span>
                </div>
              </th>
              <th class="total-header">Tổng</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="size in availableSizes" :key="size" class="matrix-row">
              <td class="size-cell">
                <strong>{{ size }}</strong>
              </td>
              <td
                v-for="color in selectedColors"
                :key="color.hex"
                class="stock-cell"
              >
                <div class="stock-input-group">
                  <input
                    type="number"
                    :value="getVariantStock(size, color.hex)"
                    @input="updateVariantStock(size, color.hex, $event.target.value)"
                    class="stock-input"
                    :class="getStockClass(size, color.hex)"
                    min="0"
                    placeholder="0"
                  />
                  <div class="stock-indicator" :class="getStockIndicatorClass(size, color.hex)"></div>
                  <div class="variant-sku-preview">{{ generateSKU(size, color) }}</div>
                </div>
              </td>
              <td class="total-cell">
                <strong>{{ getSizeTotal(size) }}</strong>
              </td>
            </tr>
            <tr class="total-row">
              <td><strong>Tổng</strong></td>
              <td v-for="color in selectedColors" :key="color.hex" class="total-cell">
                <strong>{{ getColorTotal(color.hex) }}</strong>
              </td>
              <td class="grand-total">
                <strong>{{ totalStock }}</strong>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Stock Legend -->
      <div class="stock-legend">
        <div class="legend-item">
          <div class="legend-indicator green"></div>
          <span>Còn hàng (>20)</span>
        </div>
        <div class="legend-item">
          <div class="legend-indicator yellow"></div>
          <span>Sắp hết (5-20)</span>
        </div>
        <div class="legend-item">
          <div class="legend-indicator red"></div>
          <span>Gần hết (<5)</span>
        </div>
        <div class="legend-item">
          <div class="legend-indicator gray"></div>
          <span>Hết hàng (0)</span>
        </div>
      </div>

      <!-- Variant Details: Price & Image -->
      <div class="variant-details-section">
        <h6 class="details-title">
          <i class="bi bi-currency-dollar me-2"></i>
          Chi tiết giá và hình ảnh (tùy chọn)
        </h6>
        <p class="text-muted small mb-3">
          Để trống giá nếu muốn dùng giá chung của sản phẩm. Upload ảnh cho từng biến thể để hiển thị khi khách chọn.
        </p>
        <div class="details-grid">
          <div 
            v-for="color in selectedColors" 
            :key="color.hex"
            class="color-group"
          >
            <div class="color-group-header">
              <div class="color-dot" :style="{ backgroundColor: color.hex }"></div>
              <strong>{{ color.name }}</strong>
            </div>
            <div class="size-details">
              <div 
                v-for="size in availableSizes" 
                :key="size"
                class="size-detail-row"
              >
                <div class="size-label">{{ size }}</div>
                <input
                  type="number"
                  :value="getVariantPrice(size, color.hex)"
                  @input="updateVariantPrice(size, color.hex, $event.target.value)"
                  class="price-input form-control form-control-sm"
                  placeholder="Giá (VNĐ)"
                  min="0"
                  step="1000"
                />
                <VariantImageUploader
                  v-if="productId"
                  :product-id="productId"
                  :size="size"
                  :color="color.name"
                  :image-url="getVariantImage(size, color.hex)"
                  @uploaded="(url) => updateVariantImage(size, color.hex, url)"
                  @removed="() => updateVariantImage(size, color.hex, null)"
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Variant Summary -->
    <div v-if="selectedColors.length > 0" class="section-card summary-section">
      <h5 class="section-title">
        <i class="bi bi-clipboard-data me-2"></i>
        Tóm tắt biến thể
      </h5>
      <div class="summary-grid">
        <div class="summary-item">
          <span class="summary-label">Tổng biến thể:</span>
          <span class="summary-value">{{ totalVariants }}</span>
        </div>
        <div class="summary-item">
          <span class="summary-label">Biến thể có hàng:</span>
          <span class="summary-value text-success">{{ variantsInStock }}</span>
        </div>
        <div class="summary-item">
          <span class="summary-label">Biến thể hết hàng:</span>
          <span class="summary-value text-danger">{{ variantsOutOfStock }}</span>
        </div>
        <div class="summary-item">
          <span class="summary-label">Tổng tồn kho:</span>
          <span class="summary-value text-primary">{{ totalStock }} sản phẩm</span>
        </div>
      </div>

      <!-- Low Stock Warnings -->
      <div v-if="lowStockVariants.length > 0" class="low-stock-warnings">
        <div class="warning-header">
          <i class="bi bi-exclamation-triangle text-warning me-2"></i>
          <strong>Cảnh báo sắp hết hàng ({{ lowStockVariants.length }} biến thể):</strong>
        </div>
        <div class="warning-list">
          <span
            v-for="variant in lowStockVariants"
            :key="variant.sku"
            class="warning-badge"
          >
            {{ variant.size }} - {{ variant.colorName }}: {{ variant.stock }}
          </span>
        </div>
      </div>
    </div>

    <!-- Actions -->
    <div class="manager-actions">
      <button type="button" class="btn btn-secondary" @click="handleCancel" :disabled="isSaving">
        <i class="bi bi-x me-2"></i>
        Hủy
      </button>
      <button type="button" class="btn btn-success" @click="handleSave" :disabled="isSaving || isLoading">
        <i v-if="isSaving" class="bi bi-hourglass-split me-2 spinner"></i>
        <i v-else class="bi bi-check me-2"></i>
        {{ isSaving ? 'Đang lưu...' : `Lưu biến thể (${totalVariants} biến thể)` }}
      </button>
    </div>

    <!-- Fill All Stock Modal -->
    <div v-if="showFillModal" class="modal-overlay" @click="showFillModal = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h5>Điền tồn kho hàng loạt</h5>
          <button class="btn-close" @click="showFillModal = false">
            <i class="bi bi-x"></i>
          </button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>Số lượng tồn kho cho tất cả biến thể:</label>
            <input
              type="number"
              v-model.number="bulkStockValue"
              class="form-control"
              min="0"
              placeholder="Nhập số lượng"
            />
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="showFillModal = false">Hủy</button>
          <button class="btn btn-primary" @click="applyBulkStock">Áp dụng</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import variantService from '@/services/variantService'
import VariantImageUploader from './VariantImageUploader.vue'

const props = defineProps({
  productId: {
    type: [String, Number],
    default: null
  },
  productName: {
    type: String,
    default: ''
  },
  productSku: {
    type: String,
    default: ''
  },
  category: {
    type: String,
    required: true,
    validator: (value) => ['ao', 'quan'].includes(value)
  },
  initialVariants: {
    type: Array,
    default: () => []
  },
  initialColors: {
    type: Array,
    default: () => []
  },
  initialMaterial: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['save', 'cancel', 'update'])

// Reactive state
const selectedColors = ref([...props.initialColors])
const selectedMaterial = ref(props.initialMaterial || '')
const variantStocks = ref({})
const variantPrices = ref({}) // Lưu giá riêng cho từng biến thể
const variantImages = ref({}) // Lưu ảnh riêng cho từng biến thể
const showColorPicker = ref(false)
const customColorName = ref('')
const customColorHex = ref('#000000')
const showFillModal = ref(false)
const bulkStockValue = ref(0)
const isLoading = ref(false)
const isSaving = ref(false)

// Size configurations
const sizeConfigs = {
  ao: {
    type: 'letter',
    sizes: ['S', 'M', 'L', 'XL', 'XXL', '3XL'],
    name: 'Áo'
  },
  quan: {
    type: 'number',
    sizes: ['28', '29', '30', '31', '32', '33', '34', '36', '38', '40'],
    name: 'Quần'
  }
}

// Color presets for men's clothing
const colorPresets = [
  { name: 'Đen', hex: '#000000' },
  { name: 'Trắng', hex: '#FFFFFF' },
  { name: 'Xám', hex: '#808080' },
  { name: 'Xám đậm', hex: '#4A4A4A' },
  { name: 'Xanh navy', hex: '#000080' },
  { name: 'Xanh dương', hex: '#0066CC' },
  { name: 'Xanh denim', hex: '#1560BD' },
  { name: 'Nâu', hex: '#8B4513' },
  { name: 'Be', hex: '#F5F5DC' },
  { name: 'Camel', hex: '#C19A6B' },
  { name: 'Xanh rêu', hex: '#556B2F' },
  { name: 'Xanh lá đậm', hex: '#2D5016' },
  { name: 'Đỏ đô', hex: '#8B0000' },
  { name: 'Kaki', hex: '#C3B091' }
]

// Material options for men's clothing
const materialOptions = [
  'Cotton 100%',
  'Cotton Polyester (80/20)',
  'Cotton Polyester (60/40)',
  'Linen 100%',
  'Cotton Linen blend',
  'Kaki',
  'Jean/Denim',
  'Vải Wool',
  'Vải Bamboo',
  'Vải Modal',
  'Custom'
]

// Computed
const sizeType = computed(() => sizeConfigs[props.category]?.type || 'letter')
const availableSizes = computed(() => sizeConfigs[props.category]?.sizes || [])
const categoryName = computed(() => sizeConfigs[props.category]?.name || props.category)

const totalVariants = computed(() => {
  // Chỉ đếm các biến thể có stock > 0 (đã nhập số lượng)
  let count = 0
  selectedColors.value.forEach(color => {
    availableSizes.value.forEach(size => {
      const stock = getVariantStock(size, color.hex)
      // Kiểm tra stock là số và > 0
      if (stock !== '' && !isNaN(stock) && parseInt(stock) > 0) {
        count++
      }
    })
  })
  return count
})

const totalStock = computed(() => {
  let total = 0
  Object.values(variantStocks.value).forEach(stock => {
    total += parseInt(stock) || 0
  })
  return total
})

const totalStockClass = computed(() => {
  if (totalStock.value === 0) return 'text-danger'
  if (totalStock.value < 50) return 'text-warning'
  return 'text-success'
})

const variantsInStock = computed(() => {
  return Object.values(variantStocks.value).filter(stock => parseInt(stock) > 0).length
})

const variantsOutOfStock = computed(() => {
  // Tổng số tổ hợp có thể có - số biến thể thực tế đã tạo
  const possibleCombinations = selectedColors.value.length * availableSizes.value.length
  return possibleCombinations - totalVariants.value
})

const lowStockVariants = computed(() => {
  const lowStock = []
  selectedColors.value.forEach(color => {
    availableSizes.value.forEach(size => {
      const stock = getVariantStock(size, color.hex)
      // Chỉ thêm vào warning nếu có stock và stock > 0 và < 5
      if (stock !== '' && !isNaN(stock) && stock > 0 && stock < 5) {
        lowStock.push({
          size,
          colorName: color.name,
          colorHex: color.hex,
          stock,
          sku: generateSKU(size, color)
        })
      }
    })
  })
  return lowStock
})

// Methods
const getVariantKey = (size, colorHex) => {
  return `${size}-${colorHex}`
}

const getVariantStock = (size, colorHex) => {
  const key = getVariantKey(size, colorHex)
  const stock = variantStocks.value[key]
  // Chỉ trả về giá trị nếu đã được set, ngược lại trả về ''
  return stock !== undefined && stock !== null ? parseInt(stock) : ''
}

const updateVariantStock = (size, colorHex, value) => {
  const key = getVariantKey(size, colorHex)
  
  // Nếu input trống hoặc không hợp lệ, xóa key
  if (value === '' || value === null || value === undefined) {
    delete variantStocks.value[key]
  } else {
    const numValue = parseInt(value)
    if (!isNaN(numValue) && numValue > 0) {
      variantStocks.value[key] = numValue
    } else {
      delete variantStocks.value[key]
    }
  }
  
  emitUpdate()
}

const getVariantPrice = (size, colorHex) => {
  const key = getVariantKey(size, colorHex)
  return variantPrices.value[key] || null
}

const updateVariantPrice = (size, colorHex, value) => {
  const key = getVariantKey(size, colorHex)
  variantPrices.value[key] = value ? parseFloat(value) : null
  emitUpdate()
}

const getVariantImage = (size, colorHex) => {
  const key = getVariantKey(size, colorHex)
  return variantImages.value[key] || null
}

const updateVariantImage = (size, colorHex, imageUrl) => {
  const key = getVariantKey(size, colorHex)
  variantImages.value[key] = imageUrl
  emitUpdate()
}

const getStockClass = (size, colorHex) => {
  const stock = getVariantStock(size, colorHex)
  if (stock === '' || stock === 0) return 'stock-zero'
  if (stock < 5) return 'stock-low'
  if (stock < 20) return 'stock-medium'
  return 'stock-high'
}

const getStockIndicatorClass = (size, colorHex) => {
  const stock = getVariantStock(size, colorHex)
  if (stock === '' || stock === 0) return 'indicator-gray'
  if (stock < 5) return 'indicator-red'
  if (stock < 20) return 'indicator-yellow'
  return 'indicator-green'
}

const getSizeTotal = (size) => {
  let total = 0
  selectedColors.value.forEach(color => {
    const stock = getVariantStock(size, color.hex)
    // Chỉ cộng nếu có giá trị (không phải '')
    if (stock !== '' && !isNaN(stock)) {
      total += parseInt(stock)
    }
  })
  return total
}

const getColorTotal = (colorHex) => {
  let total = 0
  availableSizes.value.forEach(size => {
    const stock = getVariantStock(size, colorHex)
    // Chỉ cộng nếu có giá trị (không phải '')
    if (stock !== '' && !isNaN(stock)) {
      total += parseInt(stock)
    }
  })
  return total
}

const generateSKU = (size, color) => {
  const baseSku = props.productSku || 'SKU'
  const colorCode = color.name.substring(0, 3).toUpperCase()
  return `${baseSku}-${size}-${colorCode}`
}

const isColorSelected = (hex) => {
  return selectedColors.value.some(c => c.hex === hex)
}

const toggleColor = (color) => {
  const index = selectedColors.value.findIndex(c => c.hex === color.hex)
  if (index > -1) {
    // Remove color và xóa stock data của color đó
    selectedColors.value.splice(index, 1)
    // Remove stock data for this color
    availableSizes.value.forEach(size => {
      const key = getVariantKey(size, color.hex)
      delete variantStocks.value[key]
    })
  } else {
    selectedColors.value.push(color)
  }
  emitUpdate()
}

const addCustomColor = () => {
  if (!customColorName.value.trim()) {
    alert('Vui lòng nhập tên màu')
    return
  }
  
  const newColor = {
    name: customColorName.value.trim(),
    hex: customColorHex.value
  }
  
  if (!isColorSelected(newColor.hex)) {
    selectedColors.value.push(newColor)
  }
  
  customColorName.value = ''
  customColorHex.value = '#000000'
  showColorPicker.value = false
  emitUpdate()
}

const removeColor = (hex) => {
  const index = selectedColors.value.findIndex(c => c.hex === hex)
  if (index > -1) {
    selectedColors.value.splice(index, 1)
    // Remove stock data
    availableSizes.value.forEach(size => {
      const key = getVariantKey(size, hex)
      delete variantStocks.value[key]
    })
    emitUpdate()
  }
}

const fillAllStock = () => {
  showFillModal.value = true
}

const applyBulkStock = () => {
  if (bulkStockValue.value >= 0) {
    selectedColors.value.forEach(color => {
      availableSizes.value.forEach(size => {
        const key = getVariantKey(size, color.hex)
        variantStocks.value[key] = bulkStockValue.value
      })
    })
    showFillModal.value = false
    bulkStockValue.value = 0
    emitUpdate()
  }
}

const copyFromVariant = () => {
  alert('Tính năng copy từ biến thể khác - Coming soon!')
}

const exportVariants = () => {
  // Export to CSV/Excel
  const data = []
  availableSizes.value.forEach(size => {
    selectedColors.value.forEach(color => {
      data.push({
        Size: size,
        'Màu sắc': color.name,
        'Mã màu': color.hex,
        'Tồn kho': getVariantStock(size, color.hex),
        SKU: generateSKU(size, color)
      })
    })
  })
  
  console.log('Export data:', data)
  alert('Tính năng xuất Excel - Coming soon!\nDữ liệu đã log ra console.')
}

const emitUpdate = () => {
  const variantsData = buildVariantsData()
  emit('update', variantsData)
}

const buildVariantsData = () => {
  const variants = []
  
  selectedColors.value.forEach(color => {
    availableSizes.value.forEach(size => {
      const stock = getVariantStock(size, color.hex)
      
      // Chỉ thêm biến thể nếu có stock là số và > 0
      if (stock !== '' && !isNaN(stock) && parseInt(stock) > 0) {
        const key = getVariantKey(size, color.hex)
        variants.push({
          size,
          color: color.name,
          colorHex: color.hex,
          material: selectedMaterial.value,
          stock: parseInt(stock),
          sku: generateSKU(size, color),
          price: variantPrices.value[key] || null, // Giá riêng
          imageUrl: variantImages.value[key] || null, // Ảnh riêng
          available: true
        })
      }
    })
  })
  
  return {
    variants,
    colors: selectedColors.value,
    material: selectedMaterial.value,
    totalStock: totalStock.value,
    totalVariants: variants.length // Cập nhật để đếm đúng số biến thể thực tế
  }
}

const handleSave = async () => {
  if (selectedColors.value.length === 0) {
    alert('Vui lòng chọn ít nhất 1 màu sắc!')
    return
  }
  
  if (!props.productId) {
    alert('Không tìm thấy ID sản phẩm. Vui lòng lưu sản phẩm trước.')
    return
  }

  // Kiểm tra xem có biến thể nào được tạo không
  if (totalVariants.value === 0) {
    alert('Vui lòng nhập số lượng tồn kho cho ít nhất 1 biến thể!')
    return
  }

  isSaving.value = true
  
  try {
    const variantsData = buildVariantsData()
    
    // Gọi API để lưu biến thể
    const response = await variantService.upsertVariants(props.productId, {
      material: selectedMaterial.value,
      variants: variantsData.variants.map(v => ({
        size: v.size,
        color: v.color,
        colorHex: v.colorHex,
        sku: v.sku,
        stock: v.stock,
        price: v.price,
        imageUrl: v.imageUrl
      }))
    })
    
    alert(`Đã lưu thành công ${response.length} biến thể!`)
    emit('save', variantsData)
  } catch (error) {
    console.error('Lỗi khi lưu biến thể:', error)
    alert('Có lỗi xảy ra khi lưu biến thể: ' + (error.response?.data?.message || error.message))
  } finally {
    isSaving.value = false
  }
}

const handleCancel = () => {
  emit('cancel')
}

// Load existing variants when component is mounted
const loadExistingVariants = async () => {
  if (!props.productId) return
  
  isLoading.value = true
  try {
    const variants = await variantService.getBySanPham(props.productId)
    
    // Map variants to our data structure
    variants.forEach(variant => {
      if (variant.size && variant.colorHex) {
        const key = getVariantKey(variant.size, variant.colorHex)
        variantStocks.value[key] = variant.stock || 0
        
        // Load price and image if available
        if (variant.price) {
          variantPrices.value[key] = variant.price
        }
        if (variant.imageUrl) {
          variantImages.value[key] = variant.imageUrl
        }
        
        // Add color to selected colors if not exists
        if (!selectedColors.value.some(c => c.hex === variant.colorHex)) {
          selectedColors.value.push({
            name: variant.color,
            hex: variant.colorHex
          })
        }
      }
      
      // Set material if exists
      if (variant.material && !selectedMaterial.value) {
        selectedMaterial.value = variant.material
      }
    })
  } catch (error) {
    console.error('Lỗi khi tải biến thể:', error)
  } finally {
    isLoading.value = false
  }
}

// Initialize from props
const initializeVariants = () => {
  if (props.initialVariants && props.initialVariants.length > 0) {
    props.initialVariants.forEach(variant => {
      const key = getVariantKey(variant.size, variant.colorHex)
      variantStocks.value[key] = variant.stock || 0
    })
  }
}

// Lifecycle
onMounted(async () => {
  // Load existing variants from API if productId exists
  if (props.productId) {
    await loadExistingVariants()
  } else {
    // Otherwise initialize from props
    initializeVariants()
  }
})

watch(() => props.initialVariants, () => {
  if (!props.productId) {
    initializeVariants()
  }
}, { immediate: false })

watch(() => props.category, () => {
  // Reset khi đổi category
  variantStocks.value = {}
  emitUpdate()
})
</script>

<style scoped>
.variant-manager-admin {
  background: #f8fafb;
  border-radius: 12px;
  padding: 1.5rem;
}

/* Header */
.manager-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #e2e8f0;
}

.manager-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.quick-stats {
  display: flex;
  gap: 1.5rem;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
  color: #64748b;
  font-weight: 500;
}

.stat-item i {
  color: #6366f1;
}

/* Category Section */
.category-section {
  margin-bottom: 1.5rem;
}

.alert {
  padding: 1rem;
  border-radius: 8px;
  border: 1px solid #bfdbfe;
  background: #eff6ff;
  color: #1e40af;
  font-size: 0.9rem;
}

/* Section Card */
.section-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.25rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid #e2e8f0;
}

.section-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
  display: flex;
  align-items: center;
}

/* Color Management */
.color-presets {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 1rem;
}

.color-preset-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.color-preset-item:hover {
  border-color: #6366f1;
  background: #f8fafb;
  transform: translateY(-2px);
}

.color-preset-item.selected {
  border-color: #6366f1;
  background: #eef2ff;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.color-swatch {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border: 3px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.color-preset-item.selected .color-swatch {
  border-color: #6366f1;
}

.color-check {
  color: white;
  font-size: 1.5rem;
  font-weight: bold;
  text-shadow: 0 0 4px rgba(0, 0, 0, 0.5);
}

.color-name {
  font-size: 0.85rem;
  color: #475569;
  font-weight: 500;
  text-align: center;
}

/* Custom Color Picker */
.custom-color-picker {
  margin-top: 1rem;
  padding: 1rem;
  background: #f8fafb;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.form-control-color {
  width: 60px;
  height: 44px;
  padding: 4px;
  border-radius: 6px;
}

/* Selected Colors */
.selected-colors {
  margin-top: 1.5rem;
  padding-top: 1rem;
  border-top: 1px solid #e2e8f0;
}

.selected-title {
  font-size: 0.9rem;
  font-weight: 600;
  color: #64748b;
  margin-bottom: 0.75rem;
}

.selected-colors-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.selected-color-badge {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0.75rem;
  background: #eef2ff;
  border: 1px solid #c7d2fe;
  border-radius: 20px;
  font-size: 0.85rem;
  color: #4338ca;
}

.color-dot {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid white;
  box-shadow: 0 0 0 1px #cbd5e1;
}

.remove-btn {
  background: none;
  border: none;
  color: #64748b;
  cursor: pointer;
  padding: 0;
  display: flex;
  align-items: center;
  transition: color 0.2s;
}

.remove-btn:hover {
  color: #ef4444;
}

/* Material Selection */
.material-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 0.75rem;
}

.material-option {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: white;
}

.material-option:hover {
  border-color: #6366f1;
  background: #f8fafb;
}

.material-option.selected {
  border-color: #6366f1;
  background: #eef2ff;
  color: #4338ca;
  font-weight: 600;
}

.material-radio {
  accent-color: #6366f1;
  cursor: pointer;
}

.material-label {
  cursor: pointer;
  flex: 1;
}

/* Variant Matrix */
.variant-matrix-section {
  background: linear-gradient(135deg, #ffffff 0%, #f8fafb 100%);
}

.matrix-actions {
  display: flex;
  gap: 0.5rem;
}

.matrix-table-wrapper {
  overflow-x: auto;
  margin: 1rem 0;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.matrix-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  font-size: 0.9rem;
}

.matrix-table thead {
  background: linear-gradient(135deg, #eef2ff 0%, #e0e7ff 100%);
  position: sticky;
  top: 0;
  z-index: 10;
}

.matrix-table th {
  padding: 1rem 0.75rem;
  text-align: center;
  font-weight: 600;
  color: #4338ca;
  border: 1px solid #c7d2fe;
}

.size-header {
  background: #6366f1;
  color: white;
  font-weight: 700;
  min-width: 100px;
}

.color-header-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.total-header {
  background: #10b981;
  color: white;
  font-weight: 700;
}

.matrix-row:nth-child(even) {
  background: #f8fafb;
}

.matrix-row:hover {
  background: #f1f5f9;
}

.size-cell {
  padding: 0.75rem 1rem;
  text-align: center;
  font-weight: 600;
  color: #1e293b;
  background: #eef2ff;
  border: 1px solid #e2e8f0;
}

.stock-cell {
  padding: 0.5rem;
  text-align: center;
  border: 1px solid #e2e8f0;
  position: relative;
}

.stock-input-group {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.25rem;
}

.stock-input {
  width: 80px;
  padding: 0.5rem;
  text-align: center;
  border: 2px solid #e2e8f0;
  border-radius: 6px;
  font-weight: 600;
  font-size: 0.95rem;
  transition: all 0.2s ease;
}

.stock-input:focus {
  outline: none;
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.stock-input.stock-zero {
  border-color: #cbd5e1;
  color: #94a3b8;
  background: #f1f5f9;
}

.stock-input.stock-low {
  border-color: #fca5a5;
  background: #fef2f2;
  color: #dc2626;
}

.stock-input.stock-medium {
  border-color: #fde047;
  background: #fefce8;
  color: #ca8a04;
}

.stock-input.stock-high {
  border-color: #86efac;
  background: #f0fdf4;
  color: #16a34a;
}

.stock-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-top: -4px;
}

.indicator-gray {
  background: #cbd5e1;
}

.indicator-red {
  background: #ef4444;
  box-shadow: 0 0 8px rgba(239, 68, 68, 0.5);
}

.indicator-yellow {
  background: #f59e0b;
  box-shadow: 0 0 8px rgba(245, 158, 11, 0.5);
}

.indicator-green {
  background: #10b981;
  box-shadow: 0 0 8px rgba(16, 185, 129, 0.5);
}

.variant-sku-preview {
  font-size: 0.7rem;
  color: #94a3b8;
  font-family: monospace;
}

.total-cell {
  padding: 0.75rem;
  text-align: center;
  font-weight: 700;
  color: #1e293b;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
}

.total-row {
  background: linear-gradient(135deg, #eef2ff 0%, #e0e7ff 100%);
  font-weight: 700;
}

.grand-total {
  background: #10b981;
  color: white;
  font-size: 1.1rem;
}

/* Stock Legend */
.stock-legend {
  display: flex;
  gap: 1.5rem;
  justify-content: center;
  margin-top: 1rem;
  padding: 1rem;
  background: #f8fafb;
  border-radius: 8px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.85rem;
  color: #64748b;
}

.legend-indicator {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.legend-indicator.green {
  background: #10b981;
}

.legend-indicator.yellow {
  background: #f59e0b;
}

.legend-indicator.red {
  background: #ef4444;
}

.legend-indicator.gray {
  background: #cbd5e1;
}

/* Summary Section */
.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  margin-bottom: 1rem;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  background: #f8fafb;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.summary-label {
  color: #64748b;
  font-size: 0.9rem;
}

.summary-value {
  font-weight: 700;
  font-size: 1.1rem;
  color: #1e293b;
}

/* Low Stock Warnings */
.low-stock-warnings {
  padding: 1rem;
  background: #fffbeb;
  border: 1px solid #fde047;
  border-radius: 8px;
  margin-top: 1rem;
}

.warning-header {
  display: flex;
  align-items: center;
  margin-bottom: 0.75rem;
  color: #854d0e;
}

.warning-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.warning-badge {
  padding: 0.4rem 0.75rem;
  background: white;
  border: 1px solid #fde047;
  border-radius: 6px;
  font-size: 0.8rem;
  color: #854d0e;
  font-weight: 500;
}

/* Actions */
.manager-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 2px solid #e2e8f0;
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
  max-width: 400px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.modal-header {
  padding: 1.5rem;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h5 {
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
  padding: 0;
  transition: color 0.2s;
}

.btn-close:hover {
  color: #1e293b;
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

/* Responsive */
@media (max-width: 768px) {
  .manager-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .quick-stats {
    flex-wrap: wrap;
    gap: 1rem;
  }

  .color-presets {
    grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
  }

  .matrix-table {
    font-size: 0.8rem;
  }

  .stock-input {
    width: 60px;
    padding: 0.4rem;
    font-size: 0.85rem;
  }

  .summary-grid {
    grid-template-columns: 1fr;
  }
}

/* Loading Spinner */
.spinner {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* Variant Details Section */
.variant-details-section {
  margin-top: 2rem;
  padding-top: 2rem;
  border-top: 2px solid #e2e8f0;
}

.details-title {
  font-size: 1rem;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 0.5rem;
}

.details-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
}

.color-group {
  background: #f8fafb;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 1rem;
}

.color-group-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid #e2e8f0;
}

.size-details {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.size-detail-row {
  display: grid;
  grid-template-columns: 40px 120px 100px;
  gap: 0.75rem;
  align-items: center;
}

.size-label {
  font-weight: 600;
  font-size: 0.9rem;
  color: #475569;
}

.price-input {
  font-size: 0.85rem;
}
</style>

