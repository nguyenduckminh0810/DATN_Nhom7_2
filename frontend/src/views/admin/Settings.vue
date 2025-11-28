<template>
  <div class="admin-settings">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">Cài đặt hệ thống</h1>
        <p class="page-subtitle">Cấu hình cửa hàng và tùy chỉnh sản phẩm</p>
      </div>
      <div class="header-right">
        <button class="btn btn-outline-secondary" @click="resetToDefaults">
          <i class="bi bi-arrow-counterclockwise me-2"></i>
          Khôi phục mặc định
        </button>
        <button class="btn btn-success" @click="saveAllSettings">
          <i class="bi bi-check2 me-2"></i>
          Lưu tất cả
        </button>
      </div>
    </div>

    <!-- Settings Tabs -->
    <div class="settings-container">
      <!-- Sidebar Navigation -->
      <div class="settings-sidebar">
        <nav class="settings-nav">
          <button
            v-for="tab in tabs"
            :key="tab.id"
            :class="['nav-item', { active: activeTab === tab.id }]"
            @click="activeTab = tab.id"
          >
            <i :class="tab.icon"></i>
            <span>{{ tab.label }}</span>
            <i v-if="hasUnsavedChanges(tab.id)" class="bi bi-circle-fill unsaved-indicator"></i>
          </button>
        </nav>
      </div>

      <!-- Settings Content -->
      <div class="settings-content">
        <!-- Tab 1: Store Settings -->
        <div v-show="activeTab === 'store'" class="settings-section">
          <div class="section-header">
            <h3 class="section-title">Thông tin cửa hàng</h3>
            <p class="section-subtitle">Cấu hình thông tin cơ bản về cửa hàng AURO</p>
          </div>

          <div class="settings-card">
            <div class="row g-3">
              <div class="col-md-6">
                <label class="form-label">Tên cửa hàng *</label>
                <input type="text" class="form-control" v-model="settings.store.name" placeholder="AURO - Thời trang nam">
              </div>
              <div class="col-md-6">
                <label class="form-label">Slogan</label>
                <input type="text" class="form-control" v-model="settings.store.slogan" placeholder="Nam tính - Lịch lãm - Phong cách">
              </div>
              <div class="col-md-6">
                <label class="form-label">Email liên hệ *</label>
                <input type="email" class="form-control" v-model="settings.store.email" placeholder="contact@auro.vn">
              </div>
              <div class="col-md-6">
                <label class="form-label">Số điện thoại *</label>
                <input type="tel" class="form-control" v-model="settings.store.phone" placeholder="0123 456 789">
              </div>
              <div class="col-12">
                <label class="form-label">Địa chỉ cửa hàng</label>
                <textarea class="form-control" rows="2" v-model="settings.store.address" placeholder="Số nhà, đường, phường, quận, thành phố"></textarea>
              </div>
              <div class="col-md-6">
                <label class="form-label">Facebook</label>
                <input type="url" class="form-control" v-model="settings.store.social.facebook" placeholder="https://facebook.com/auro">
              </div>
              <div class="col-md-6">
                <label class="form-label">Instagram</label>
                <input type="url" class="form-control" v-model="settings.store.social.instagram" placeholder="https://instagram.com/auro">
              </div>
              <div class="col-md-6">
                <label class="form-label">Zalo</label>
                <input type="text" class="form-control" v-model="settings.store.social.zalo" placeholder="0123456789">
              </div>
              <div class="col-md-6">
                <label class="form-label">TikTok</label>
                <input type="url" class="form-control" v-model="settings.store.social.tiktok" placeholder="https://tiktok.com/@auro">
              </div>
            </div>
          </div>
        </div>

        <!-- Tab 2: Product Settings -->
        <div v-show="activeTab === 'products'" class="settings-section">
          <div class="section-header">
            <h3 class="section-title">Cấu hình sản phẩm</h3>
            <p class="section-subtitle">Thiết lập size, màu sắc và chất liệu mặc định cho quần áo nam</p>
          </div>

          <!-- Size Configuration -->
          <div class="settings-card">
            <h5 class="card-title">
              <i class="bi bi-rulers me-2"></i>
              Cấu hình Size
              <span v-if="isLoadingSizes" class="ms-2">
                <i class="bi bi-arrow-repeat spin"></i>
              </span>
            </h5>
            
            <div class="size-config-section">
              <h6 class="subsection-title">Size ÁO (Chữ)</h6>
              <div class="size-tags-group">
                <span
                  v-for="size in sizesAo"
                  :key="size.id"
                  class="size-tag"
                >
                  {{ size.ten }}
                  <button type="button" @click="editSize(size)" class="tag-edit-btn" title="Sửa">
                    <i class="bi bi-pencil"></i>
                  </button>
                  <button type="button" @click="removeSize(size.id, 'ao')" class="tag-remove-btn" title="Xóa">
                    <i class="bi bi-x"></i>
                  </button>
                </span>
                <input
                  type="text"
                  v-model="newSizeAo"
                  @keydown.enter="addSize('ao')"
                  class="size-input"
                  placeholder="Thêm size..."
                  maxlength="10"
                  :disabled="isSavingSize"
                />
                <button v-if="newSizeAo.trim()" @click="addSize('ao')" class="btn btn-sm btn-primary" :disabled="isSavingSize">
                  <i class="bi bi-plus"></i>
                </button>
              </div>
              <small class="text-muted">VD: S, M, L, XL, XXL, 3XL</small>
            </div>

            <div class="size-config-section">
              <h6 class="subsection-title">Size QUẦN (Số - Tiêu chuẩn Việt Nam)</h6>
              <div class="size-tags-group">
                <span
                  v-for="size in sizesQuan"
                  :key="size.id"
                  class="size-tag"
                >
                  {{ size.ten }}
                  <button type="button" @click="editSize(size)" class="tag-edit-btn" title="Sửa">
                    <i class="bi bi-pencil"></i>
                  </button>
                  <button type="button" @click="removeSize(size.id, 'quan')" class="tag-remove-btn" title="Xóa">
                    <i class="bi bi-x"></i>
                  </button>
                </span>
                <input
                  type="text"
                  v-model="newSizeQuan"
                  @keydown.enter="addSize('quan')"
                  class="size-input"
                  placeholder="Thêm size..."
                  maxlength="10"
                  :disabled="isSavingSize"
                />
                <button v-if="newSizeQuan.trim()" @click="addSize('quan')" class="btn btn-sm btn-primary" :disabled="isSavingSize">
                  <i class="bi bi-plus"></i>
                </button>
              </div>
              <small class="text-muted">VD: 28, 29, 30, 31, 32, 33, 34, 36, 38, 40 (vòng eo tính theo cm)</small>
            </div>
          </div>

          <!-- Color Presets -->
          <div class="settings-card">
            <h5 class="card-title">
              <i class="bi bi-palette me-2"></i>
              Màu sắc phổ biến
              <span v-if="isLoadingColors" class="ms-2">
                <i class="bi bi-arrow-repeat spin"></i>
              </span>
            </h5>
            
            <div class="color-presets-grid">
              <div
                v-for="color in colors"
                :key="color.id"
                class="color-preset-card"
              >
                
                <input
                  type="text"
                  v-model="color.ten"
                  @blur="updateColor(color)"
                  class="form-control form-control-sm color-name-input"
                  placeholder="Tên màu"
                  :disabled="isSavingColor === color.id"
                />
                <input
                  type="color"
                  :value="color.ma || '#000000'"
                  @change="updateColorHex(color, $event.target.value)"
                  class="form-control form-control-color color-picker-input"
                  :disabled="isSavingColor === color.id"
                />
                <button 
                  type="button" 
                  @click="removeColor(color.id)" 
                  class="btn btn-sm btn-outline-danger"
                  :disabled="isDeletingColor === color.id"
                >
                  <i v-if="isDeletingColor === color.id" class="bi bi-arrow-repeat spin"></i>
                  <i v-else class="bi bi-trash"></i>
                </button>
              </div>
              
              <div class="color-preset-card add-color-card" @click="addNewColor" :class="{ disabled: isAddingColor }">
                <i v-if="isAddingColor" class="bi bi-arrow-repeat spin"></i>
                <i v-else class="bi bi-plus-circle"></i>
                <span>{{ isAddingColor ? 'Đang thêm...' : 'Thêm màu mới' }}</span>
              </div>
            </div>
          </div>

          <!-- Material Presets -->
          <div class="settings-card">
            <h5 class="card-title">
              <i class="bi bi-tags me-2"></i>
              Chất liệu phổ biến
              <span v-if="isLoadingMaterials" class="ms-2">
                <i class="bi bi-arrow-repeat spin"></i>
              </span>
            </h5>
            
            <div class="material-tags-group">
              <span
                v-for="material in materials"
                :key="material.id"
                class="material-tag"
              >
                {{ material.ten }}
                <button type="button" @click="editMaterial(material)" class="tag-edit-btn" title="Sửa">
                  <i class="bi bi-pencil"></i>
                </button>
                <button 
                  type="button" 
                  @click="removeMaterial(material.id)" 
                  class="tag-remove-btn" 
                  title="Xóa"
                  :disabled="isDeletingMaterial === material.id"
                >
                  <i v-if="isDeletingMaterial === material.id" class="bi bi-arrow-repeat spin"></i>
                  <i v-else class="bi bi-x"></i>
                </button>
              </span>
              <input
                type="text"
                v-model="newMaterial"
                @keydown.enter="addMaterial"
                class="material-input"
                placeholder="Thêm chất liệu..."
                :disabled="isAddingMaterial"
              />
              <button v-if="newMaterial.trim()" @click="addMaterial" class="btn btn-sm btn-primary" :disabled="isAddingMaterial">
                <i class="bi bi-plus"></i>
              </button>
            </div>
          </div>
        </div>

        <!-- Tab 3: Shipping Settings -->
        <div v-show="activeTab === 'shipping'" class="settings-section">
          <div class="section-header">
            <h3 class="section-title">Cấu hình vận chuyển</h3>
            <p class="section-subtitle">Thiết lập phí ship và đối tác vận chuyển</p>
          </div>

          <div class="settings-card">
            <h5 class="card-title">Phí vận chuyển</h5>
            
            <div class="row g-3">
              <div class="col-md-4">
                <label class="form-label">Nội thành Hải Phòng</label>
                <div class="input-group">
                  <input type="number" class="form-control" v-model.number="settings.shipping.fees.local" min="0">
                  <span class="input-group-text">₫</span>
                </div>
              </div>
              <div class="col-md-4">
                <label class="form-label">Ngoại thành</label>
                <div class="input-group">
                  <input type="number" class="form-control" v-model.number="settings.shipping.fees.suburban" min="0">
                  <span class="input-group-text">₫</span>
                </div>
              </div>
              <div class="col-md-4">
                <label class="form-label">Tỉnh thành khác</label>
                <div class="input-group">
                  <input type="number" class="form-control" v-model.number="settings.shipping.fees.national" min="0">
                  <span class="input-group-text">₫</span>
                </div>
              </div>
              <div class="col-md-6">
                <label class="form-label">Miễn phí ship cho đơn hàng từ</label>
                <div class="input-group">
                  <input type="number" class="form-control" v-model.number="settings.shipping.freeShippingThreshold" min="0">
                  <span class="input-group-text">₫</span>
                </div>
              </div>
              <div class="col-md-6">
                <label class="form-label">Thời gian giao hàng dự kiến</label>
                <input type="text" class="form-control" v-model="settings.shipping.estimatedDays" placeholder="2-3 ngày">
              </div>
            </div>
          </div>

          <div class="settings-card">
            <h5 class="card-title">Đối tác vận chuyển</h5>
            
            <div class="shipping-partners">
              <div class="partner-item">
                <div class="form-check form-switch">
                  <input class="form-check-input" type="checkbox" v-model="settings.shipping.partners.ghn" id="ghnCheck">
                  <label class="form-check-label" for="ghnCheck">
                    <strong>Giao Hàng Nhanh (GHN)</strong>
                  </label>
                </div>
              </div>
              
              <div class="partner-item">
                <div class="form-check form-switch">
                  <input class="form-check-input" type="checkbox" v-model="settings.shipping.partners.ghtk" id="ghtkCheck">
                  <label class="form-check-label" for="ghtkCheck">
                    <strong>Giao Hàng Tiết Kiệm (GHTK)</strong>
                  </label>
                </div>
              </div>
              
              <div class="partner-item">
                <div class="form-check form-switch">
                  <input class="form-check-input" type="checkbox" v-model="settings.shipping.partners.viettelPost" id="viettelCheck">
                  <label class="form-check-label" for="viettelCheck">
                    <strong>Viettel Post</strong>
                  </label>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Tab 4: Payment Settings -->
        <div v-show="activeTab === 'payment'" class="settings-section">
          <div class="section-header">
            <h3 class="section-title">Cấu hình thanh toán</h3>
            <p class="section-subtitle">Thiết lập các phương thức thanh toán</p>
          </div>

          <div class="settings-card">
            <h5 class="card-title">Phương thức thanh toán</h5>
            
            <div class="payment-methods">
              <div class="payment-item">
                <div class="payment-header">
                  <div class="form-check form-switch">
                    <input class="form-check-input" type="checkbox" v-model="settings.payment.methods.cod.enabled" id="codCheck">
                    <label class="form-check-label" for="codCheck">
                      <strong>Thanh toán khi nhận hàng (COD)</strong>
                    </label>
                  </div>
                </div>
                <div v-if="settings.payment.methods.cod.enabled" class="payment-config">
                  <div class="row g-3">
                    <div class="col-md-6">
                      <label class="form-label">Phí COD</label>
                      <div class="input-group">
                        <input type="number" class="form-control" v-model.number="settings.payment.methods.cod.fee" min="0">
                        <span class="input-group-text">₫</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="payment-item">
                <div class="payment-header">
                  <div class="form-check form-switch">
                    <input class="form-check-input" type="checkbox" v-model="settings.payment.methods.bankTransfer.enabled" id="bankCheck">
                    <label class="form-check-label" for="bankCheck">
                      <strong>Chuyển khoản ngân hàng</strong>
                    </label>
                  </div>
                </div>
                <div v-if="settings.payment.methods.bankTransfer.enabled" class="payment-config">
                  <div class="row g-3">
                    <div class="col-md-6">
                      <label class="form-label">Tên ngân hàng</label>
                      <input type="text" class="form-control" v-model="settings.payment.methods.bankTransfer.bankName" placeholder="VD: Vietcombank">
                    </div>
                    <div class="col-md-6">
                      <label class="form-label">Số tài khoản</label>
                      <input type="text" class="form-control" v-model="settings.payment.methods.bankTransfer.accountNumber" placeholder="1234567890">
                    </div>
                    <div class="col-md-12">
                      <label class="form-label">Tên chủ tài khoản</label>
                      <input type="text" class="form-control" v-model="settings.payment.methods.bankTransfer.accountName" placeholder="NGUYEN VAN A">
                    </div>
                  </div>
                </div>
              </div>

              <div class="payment-item">
                <div class="payment-header">
                  <div class="form-check form-switch">
                    <input class="form-check-input" type="checkbox" v-model="settings.payment.methods.vnpay.enabled" id="vnpayCheck">
                    <label class="form-check-label" for="vnpayCheck">
                      <strong>VNPay</strong>
                    </label>
                  </div>
                </div>
                <div v-if="settings.payment.methods.vnpay.enabled" class="payment-config">
                  <div class="alert alert-info">
                    <i class="bi bi-info-circle me-2"></i>
                    Cấu hình VNPay cần credentials từ merchant portal (không hiển thị ở đây vì bảo mật)
                  </div>
                </div>
              </div>


            </div>
          </div>
        </div>

        <!-- Tab 5: Policies -->
        <div v-show="activeTab === 'policies'" class="settings-section">
          <div class="section-header">
            <h3 class="section-title">Chính sách cửa hàng</h3>
            <p class="section-subtitle">Thiết lập chính sách đổi trả, bảo hành và chăm sóc</p>
          </div>

          <div class="settings-card">
            <h5 class="card-title">Chính sách đổi trả</h5>
            
            <div class="row g-3">
              <div class="col-md-6">
                <label class="form-label">Thời gian đổi trả</label>
                <select class="form-select" v-model="settings.policies.returnDays">
                  <option :value="7">7 ngày</option>
                  <option :value="14">14 ngày</option>
                  <option :value="30">30 ngày</option>
                </select>
              </div>
              <div class="col-md-6">
                <div class="form-check form-switch mt-4">
                  <input class="form-check-input" type="checkbox" v-model="settings.policies.allowExchange" id="exchangeCheck">
                  <label class="form-check-label" for="exchangeCheck">
                    Cho phép đổi size/màu
                  </label>
                </div>
              </div>
              <div class="col-12">
                <label class="form-label">Điều kiện đổi trả</label>
                <textarea class="form-control" rows="4" v-model="settings.policies.returnConditions" placeholder="VD: Sản phẩm còn nguyên tem, chưa qua sử dụng..."></textarea>
              </div>
            </div>
          </div>

          <div class="settings-card">
            <h5 class="card-title">Hướng dẫn chăm sóc sản phẩm</h5>
            
            <textarea class="form-control" rows="6" v-model="settings.policies.careInstructions" placeholder="Hướng dẫn giặt, bảo quản sản phẩm..."></textarea>
          </div>
        </div>
      </div>
    </div>

    <!-- Save Success Toast -->
    <div v-if="showSaveSuccess" class="save-toast">
      <i class="bi bi-check-circle me-2"></i>
      Đã lưu cài đặt thành công!
    </div>

    <!-- Error Toast -->
    <div v-if="errorMessage" class="error-toast">
      <i class="bi bi-exclamation-circle me-2"></i>
      {{ errorMessage }}
      <button @click="errorMessage = ''" class="ms-2 btn-close-toast">
        <i class="bi bi-x"></i>
      </button>
    </div>

    <!-- Edit Size Modal -->
    <div v-if="editingSize" class="modal-overlay" @click.self="editingSize = null">
      <div class="modal-content">
        <h5>Sửa size</h5>
        <input
          type="text"
          v-model="editingSizeName"
          class="form-control mb-3"
          placeholder="Tên size"
        />
        <div class="modal-actions">
          <button @click="saveSizeEdit" class="btn btn-primary" :disabled="isSavingSize">
            Lưu
          </button>
          <button @click="editingSize = null" class="btn btn-secondary">Hủy</button>
        </div>
      </div>
    </div>

    <!-- Edit Material Modal -->
    <div v-if="editingMaterial" class="modal-overlay" @click.self="editingMaterial = null">
      <div class="modal-content">
        <h5>Sửa chất liệu</h5>
        <input
          type="text"
          v-model="editingMaterialName"
          class="form-control mb-3"
          placeholder="Tên chất liệu"
        />
        <div class="modal-actions">
          <button @click="saveMaterialEdit" class="btn btn-primary" :disabled="isSavingMaterial">
            Lưu
          </button>
          <button @click="editingMaterial = null" class="btn btn-secondary">Hủy</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import colorService from '@/services/colorService'
import materialService from '@/services/materialService'
import sizeService from '@/services/sizeService'

// Tabs configuration
const tabs = [
  { id: 'store', label: 'Cửa hàng', icon: 'bi bi-shop' },
  { id: 'products', label: 'Sản phẩm', icon: 'bi bi-box' },
  { id: 'shipping', label: 'Vận chuyển', icon: 'bi bi-truck' },
  { id: 'payment', label: 'Thanh toán', icon: 'bi bi-credit-card' },
  { id: 'policies', label: 'Chính sách', icon: 'bi bi-file-text' }
]

const activeTab = ref('store')
const showSaveSuccess = ref(false)
const errorMessage = ref('')

// Loading states
const isLoadingColors = ref(false)
const isLoadingMaterials = ref(false)
const isLoadingSizes = ref(false)
const isSavingColor = ref(null)
const isAddingColor = ref(false)
const isDeletingColor = ref(null)
const isAddingMaterial = ref(false)
const isSavingMaterial = ref(false)
const isDeletingMaterial = ref(null)
const isSavingSize = ref(false)

// Data from DB
const colors = ref([])
const materials = ref([])
const allSizes = ref([])

// New inputs
const newSizeAo = ref('')
const newSizeQuan = ref('')
const newMaterial = ref('')

// Edit states
const editingSize = ref(null)
const editingSizeName = ref('')
const editingMaterial = ref(null)
const editingMaterialName = ref('')

// Computed: Filter sizes by type
const sizesAo = computed(() => {
  return allSizes.value.filter(size => {
    const ten = size.ten?.toUpperCase().trim()
    // Size áo thường là chữ: S, M, L, XL, XXL, 3XL, etc.
    return /^[A-Z0-9]+$/.test(ten) && !/^\d+$/.test(ten)
  }).sort((a, b) => {
    // Sort by thuTu if available, otherwise by name
    if (a.thuTu != null && b.thuTu != null) return a.thuTu - b.thuTu
    return (a.ten || '').localeCompare(b.ten || '')
  })
})

const sizesQuan = computed(() => {
  return allSizes.value.filter(size => {
    const ten = size.ten?.trim()
    // Size quần thường là số: 28, 29, 30, etc.
    return /^\d+$/.test(ten)
  }).sort((a, b) => {
    // Sort by thuTu if available, otherwise by numeric value
    if (a.thuTu != null && b.thuTu != null) return a.thuTu - b.thuTu
    const numA = parseInt(a.ten) || 0
    const numB = parseInt(b.ten) || 0
    return numA - numB
  })
})

// Settings data
const settings = ref({
  store: {
    name: 'AURO',
    slogan: 'Thời trang nam cao cấp',
    email: 'contact@auro.vn',
    phone: '0123 456 789',
    address: 'Hải Phòng, Việt Nam',
    social: {
      facebook: '',
      instagram: '',
      zalo: '',
      tiktok: ''
    }
  },
  products: {
    // Dữ liệu size, color, material giờ được load từ DB
  },
  shipping: {
    fees: {
      local: 30000,
      suburban: 50000,
      national: 30000
    },
    freeShippingThreshold: 500000,
    estimatedDays: '2-3 ngày',
    partners: {
      ghn: true,
      ghtk: true,
      viettelPost: false
    }
  },
  payment: {
    methods: {
      cod: {
        enabled: true,
        fee: 20000
      },
      bankTransfer: {
        enabled: true,
        bankName: 'Vietcombank',
        accountNumber: '',
        accountName: ''
      },
      vnpay: {
        enabled: false
      }
    }
  },
  policies: {
    returnDays: 7,
    allowExchange: true,
    returnConditions: 'Sản phẩm còn nguyên tem, chưa qua sử dụng, không bị lỗi do người dùng.',
    careInstructions: 'Giặt máy ở nhiệt độ thường, không tẩy, phơi nơi thoáng mát.'
  }
})

// Load data from API
const loadColors = async () => {
  isLoadingColors.value = true
  try {
    const data = await colorService.getAll()
    colors.value = Array.isArray(data) ? data : []
  } catch (error) {
    console.error('Error loading colors:', error)
    showError('Không thể tải danh sách màu sắc')
  } finally {
    isLoadingColors.value = false
  }
}

const loadMaterials = async () => {
  isLoadingMaterials.value = true
  try {
    const data = await materialService.getAll()
    materials.value = Array.isArray(data) ? data : []
  } catch (error) {
    console.error('Error loading materials:', error)
    showError('Không thể tải danh sách chất liệu')
  } finally {
    isLoadingMaterials.value = false
  }
}

const loadSizes = async () => {
  isLoadingSizes.value = true
  try {
    const data = await sizeService.getAll()
    allSizes.value = Array.isArray(data) ? data : []
  } catch (error) {
    console.error('Error loading sizes:', error)
    showError('Không thể tải danh sách size')
  } finally {
    isLoadingSizes.value = false
  }
}

// Color methods
const addNewColor = async () => {
  if (isAddingColor.value) return
  isAddingColor.value = true
  try {
    const newColor = {
      ten: 'Màu mới',
      ma: '#000000'
    }
    const created = await colorService.create(newColor)
    colors.value.push(created)
    showSuccess('Đã thêm màu mới')
  } catch (error) {
    console.error('Error adding color:', error)
    showError('Không thể thêm màu mới')
  } finally {
    isAddingColor.value = false
  }
}

const updateColor = async (color) => {
  if (isSavingColor.value === color.id) return
  isSavingColor.value = color.id
  try {
    await colorService.update(color.id, {
      ten: color.ten,
      ma: color.ma
    })
    showSuccess('Đã cập nhật màu')
  } catch (error) {
    console.error('Error updating color:', error)
    showError('Không thể cập nhật màu')
    // Reload to get correct data
    await loadColors()
  } finally {
    isSavingColor.value = null
  }
}

const updateColorHex = async (color, hex) => {
  color.ma = hex
  await updateColor(color)
}

const removeColor = async (id) => {
  if (!confirm('Bạn có chắc muốn xóa màu này?')) return
  if (isDeletingColor.value === id) return
  isDeletingColor.value = id
  try {
    await colorService.delete(id)
    colors.value = colors.value.filter(c => c.id !== id)
    showSuccess('Đã xóa màu')
  } catch (error) {
    console.error('Error deleting color:', error)
    showError('Không thể xóa màu. Có thể màu đang được sử dụng.')
  } finally {
    isDeletingColor.value = null
  }
}

// Material methods
const addMaterial = async () => {
  if (!newMaterial.value.trim() || isAddingMaterial.value) return
  isAddingMaterial.value = true
  try {
    const created = await materialService.create({ ten: newMaterial.value.trim() })
    materials.value.push(created)
    newMaterial.value = ''
    showSuccess('Đã thêm chất liệu')
  } catch (error) {
    console.error('Error adding material:', error)
    const errorMsg = error.response?.data?.message || 'Không thể thêm chất liệu'
    showError(errorMsg)
  } finally {
    isAddingMaterial.value = false
  }
}

const editMaterial = (material) => {
  editingMaterial.value = material
  editingMaterialName.value = material.ten
}

const saveMaterialEdit = async () => {
  if (!editingMaterialName.value.trim() || isSavingMaterial.value) return
  isSavingMaterial.value = true
  try {
    const updated = await materialService.update(editingMaterial.value.id, {
      ten: editingMaterialName.value.trim()
    })
    const index = materials.value.findIndex(m => m.id === editingMaterial.value.id)
    if (index !== -1) {
      materials.value[index] = updated
    }
    editingMaterial.value = null
    editingMaterialName.value = ''
    showSuccess('Đã cập nhật chất liệu')
  } catch (error) {
    console.error('Error updating material:', error)
    const errorMsg = error.response?.data?.message || 'Không thể cập nhật chất liệu'
    showError(errorMsg)
  } finally {
    isSavingMaterial.value = false
  }
}

const removeMaterial = async (id) => {
  if (!confirm('Bạn có chắc muốn xóa chất liệu này?')) return
  if (isDeletingMaterial.value === id) return
  isDeletingMaterial.value = id
  try {
    await materialService.delete(id)
    materials.value = materials.value.filter(m => m.id !== id)
    showSuccess('Đã xóa chất liệu')
  } catch (error) {
    console.error('Error deleting material:', error)
    const errorMsg = error.response?.data?.message || 'Không thể xóa chất liệu. Có thể chất liệu đang được sử dụng.'
    showError(errorMsg)
  } finally {
    isDeletingMaterial.value = null
  }
}

// Size methods
const addSize = async (type) => {
  const sizeName = type === 'ao' ? newSizeAo.value.trim() : newSizeQuan.value.trim()
  if (!sizeName || isSavingSize.value) return
  
  isSavingSize.value = true
  try {
    // Find max thuTu to add new size at the end
    const maxThuTu = allSizes.value.length > 0 
      ? Math.max(...allSizes.value.map(s => s.thuTu || 0))
      : 0
    
    const created = await sizeService.create({
      ten: sizeName,
      thuTu: maxThuTu + 1
    })
    allSizes.value.push(created)
    
    if (type === 'ao') {
      newSizeAo.value = ''
    } else {
      newSizeQuan.value = ''
    }
    showSuccess('Đã thêm size')
  } catch (error) {
    console.error('Error adding size:', error)
    const errorMsg = error.response?.data?.message || 'Không thể thêm size'
    showError(errorMsg)
  } finally {
    isSavingSize.value = false
  }
}

const editSize = (size) => {
  editingSize.value = size
  editingSizeName.value = size.ten
}

const saveSizeEdit = async () => {
  if (!editingSizeName.value.trim() || isSavingSize.value) return
  isSavingSize.value = true
  try {
    const updated = await sizeService.update(editingSize.value.id, {
      ten: editingSizeName.value.trim(),
      thuTu: editingSize.value.thuTu
    })
    const index = allSizes.value.findIndex(s => s.id === editingSize.value.id)
    if (index !== -1) {
      allSizes.value[index] = updated
    }
    editingSize.value = null
    editingSizeName.value = ''
    showSuccess('Đã cập nhật size')
  } catch (error) {
    console.error('Error updating size:', error)
    const errorMsg = error.response?.data?.message || 'Không thể cập nhật size'
    showError(errorMsg)
  } finally {
    isSavingSize.value = false
  }
}

const removeSize = async (id, type) => {
  if (!confirm('Bạn có chắc muốn xóa size này?')) return
  if (isSavingSize.value) return
  isSavingSize.value = true
  try {
    await sizeService.delete(id)
    allSizes.value = allSizes.value.filter(s => s.id !== id)
    showSuccess('Đã xóa size')
  } catch (error) {
    console.error('Error deleting size:', error)
    const errorMsg = error.response?.data?.message || 'Không thể xóa size. Có thể size đang được sử dụng.'
    showError(errorMsg)
  } finally {
    isSavingSize.value = false
  }
}

// Helper methods
const showSuccess = (message) => {
  showSaveSuccess.value = true
  setTimeout(() => {
    showSaveSuccess.value = false
  }, 3000)
}

const showError = (message) => {
  errorMessage.value = message
  setTimeout(() => {
    errorMessage.value = ''
  }, 5000)
}

const hasUnsavedChanges = (tabId) => {
  // Logic để check unsaved changes
  return false
}

const saveAllSettings = () => {
  // Lưu settings vào localStorage hoặc API
  localStorage.setItem('auro_admin_settings', JSON.stringify(settings.value))
  
  showSaveSuccess.value = true
  setTimeout(() => {
    showSaveSuccess.value = false
  }, 3000)
  
  console.log('Settings saved:', settings.value)
}

const resetToDefaults = () => {
  if (confirm('Bạn có chắc muốn khôi phục cài đặt mặc định?')) {
    // Reset logic here
    location.reload()
  }
}

// Load settings from localStorage
const loadSettings = () => {
  const saved = localStorage.getItem('auro_admin_settings')
  if (saved) {
    try {
      const parsed = JSON.parse(saved)
      settings.value = { ...settings.value, ...parsed }
    } catch (e) {
      console.error('Error loading settings:', e)
    }
  }
}

// Initialize
onMounted(async () => {
  await Promise.all([
    loadColors(),
    loadMaterials(),
    loadSizes()
  ])
  loadSettings()
})
</script>

<style scoped>
.admin-settings {
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

/* Settings Container */
.settings-container {
  display: grid;
  grid-template-columns: 250px 1fr;
  gap: 2rem;
}

/* Sidebar Navigation */
.settings-sidebar {
  background: white;
  border-radius: 12px;
  padding: 1rem;
  border: 1px solid #e2e8f0;
  height: fit-content;
  position: sticky;
  top: 2rem;
}

.settings-nav {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  background: none;
  border: none;
  border-radius: 8px;
  color: #64748b;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: left;
  position: relative;
}

.nav-item:hover {
  background: #f1f5f9;
  color: #475569;
}

.nav-item.active {
  background: #eef2ff;
  color: #6366f1;
  font-weight: 600;
}

.nav-item i:first-child {
  font-size: 1.1rem;
}

.unsaved-indicator {
  font-size: 0.5rem;
  color: #f59e0b;
  margin-left: auto;
}

/* Settings Content */
.settings-content {
  background: white;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.settings-section {
  padding: 2rem;
}

.section-header {
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #e2e8f0;
}

.section-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 0.5rem;
}

.section-subtitle {
  color: #64748b;
  font-size: 0.95rem;
  margin: 0;
}

/* Settings Card */
.settings-card {
  background: #f8fafb;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
}

.card-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 1.25rem;
  display: flex;
  align-items: center;
}

.subsection-title {
  font-size: 0.95rem;
  font-weight: 600;
  color: #475569;
  margin-bottom: 0.75rem;
}

/* Size Configuration */
.size-config-section {
  margin-bottom: 1.5rem;
}

.size-tags-group {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  padding: 0.75rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: white;
  margin-bottom: 0.5rem;
  min-height: 52px;
}

.size-tag {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.4rem 0.75rem;
  background: #eef2ff;
  border: 1px solid #c7d2fe;
  border-radius: 6px;
  color: #4338ca;
  font-weight: 600;
  font-size: 0.9rem;
}

.tag-remove-btn {
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

.tag-remove-btn:hover {
  color: #ef4444;
}

.size-input {
  border: none;
  outline: none;
  padding: 0.4rem;
  font-size: 0.9rem;
  font-weight: 600;
  min-width: 80px;
  text-transform: uppercase;
}

/* Color Presets */
.color-presets-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1rem;
}

.color-preset-card {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  transition: all 0.2s;
  flex-wrap: nowrap;
}

.color-preset-card:hover {
  border-color: #6366f1;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.1);
}

.color-swatch {
  width: 60px;
  height: 60px;
  min-width: 60px;
  min-height: 60px;
  border-radius: 8px;
  border: 2px solid #e2e8f0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
}

.color-name-input {
  flex: 1;
  min-width: 150px;
  width: 100%;
  white-space: normal;
  word-wrap: break-word;
  overflow-wrap: break-word;
}

.color-picker-input {
  width: 50px;
  height: 50px;
  min-width: 50px;
  min-height: 50px;
  padding: 0;
  border: 2px solid #e2e8f0;
  border-radius: 6px;
  cursor: pointer;
  flex-shrink: 0;
}

.add-color-card {
  flex-direction: column;
  justify-content: center;
  cursor: pointer;
  color: #64748b;
}

.add-color-card:hover {
  color: #6366f1;
  border-color: #6366f1;
}

.add-color-card i {
  font-size: 2rem;
}

/* Material Tags */
.material-tags-group {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  padding: 0.75rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: white;
  min-height: 52px;
}

.material-tag {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.4rem 0.75rem;
  background: #dcfce7;
  border: 1px solid #86efac;
  border-radius: 6px;
  color: #166534;
  font-weight: 500;
  font-size: 0.85rem;
}

.material-input {
  border: none;
  outline: none;
  padding: 0.4rem;
  font-size: 0.85rem;
  min-width: 150px;
}

/* Shipping & Payment */
.shipping-partners,
.payment-methods {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.partner-item,
.payment-item {
  padding: 1rem;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
}

.payment-header {
  margin-bottom: 0.75rem;
}

.payment-config {
  padding: 1rem;
  background: #f8fafb;
  border-radius: 6px;
  margin-top: 0.75rem;
}

/* Form Controls */
.form-check-input:checked {
  background-color: #6366f1;
  border-color: #6366f1;
}

/* Save Toast */
.save-toast {
  position: fixed;
  bottom: 2rem;
  right: 2rem;
  background: #10b981;
  color: white;
  padding: 1rem 1.5rem;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(16, 185, 129, 0.3);
  display: flex;
  align-items: center;
  font-weight: 600;
  z-index: 1000;
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from {
    transform: translateY(100px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

/* Responsive */
@media (max-width: 992px) {
  .settings-container {
    grid-template-columns: 1fr;
  }

  .settings-sidebar {
    position: static;
  }

  .settings-nav {
    flex-direction: row;
    overflow-x: auto;
  }

  .nav-item {
    white-space: nowrap;
  }
}

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

  .settings-section {
    padding: 1.5rem;
  }

  .color-presets-grid {
    grid-template-columns: 1fr;
  }
}

/* Loading spinner */
.spin {
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

/* Edit button */
.tag-edit-btn {
  background: none;
  border: none;
  color: #6366f1;
  cursor: pointer;
  padding: 0;
  display: flex;
  align-items: center;
  font-size: 0.85rem;
  transition: color 0.2s;
  margin-right: 0.25rem;
}

.tag-edit-btn:hover {
  color: #4338ca;
}

/* Disabled state */
.disabled {
  opacity: 0.6;
  cursor: not-allowed;
  pointer-events: none;
}

/* Error Toast */
.error-toast {
  position: fixed;
  bottom: 2rem;
  right: 2rem;
  background: #ef4444;
  color: white;
  padding: 1rem 1.5rem;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(239, 68, 68, 0.3);
  display: flex;
  align-items: center;
  font-weight: 600;
  z-index: 1001;
  animation: slideIn 0.3s ease;
  max-width: 400px;
}

.btn-close-toast {
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  padding: 0;
  margin-left: 0.5rem;
  font-size: 1.2rem;
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
  padding: 2rem;
  border-radius: 12px;
  min-width: 400px;
  max-width: 90%;
}

.modal-content h5 {
  margin-bottom: 1.5rem;
  font-weight: 600;
}

.modal-actions {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
}
</style>

