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
            </h5>
            
            <div class="size-config-section">
              <h6 class="subsection-title">Size ÁO (Chữ)</h6>
              <div class="size-tags-group">
                <span
                  v-for="(size, index) in settings.products.sizes.ao"
                  :key="index"
                  class="size-tag"
                >
                  {{ size }}
                  <button type="button" @click="removeSize('ao', index)" class="tag-remove-btn">
                    <i class="bi bi-x"></i>
                  </button>
                </span>
                <input
                  type="text"
                  v-model="newSizeAo"
                  @keydown.enter="addSize('ao')"
                  class="size-input"
                  placeholder="Thêm size..."
                  maxlength="4"
                />
              </div>
              <small class="text-muted">VD: S, M, L, XL, XXL, 3XL</small>
            </div>

            <div class="size-config-section">
              <h6 class="subsection-title">Size QUẦN (Số - Tiêu chuẩn Việt Nam)</h6>
              <div class="size-tags-group">
                <span
                  v-for="(size, index) in settings.products.sizes.quan"
                  :key="index"
                  class="size-tag"
                >
                  {{ size }}
                  <button type="button" @click="removeSize('quan', index)" class="tag-remove-btn">
                    <i class="bi bi-x"></i>
                  </button>
                </span>
                <input
                  type="text"
                  v-model="newSizeQuan"
                  @keydown.enter="addSize('quan')"
                  class="size-input"
                  placeholder="Thêm size..."
                  maxlength="2"
                />
              </div>
              <small class="text-muted">VD: 28, 29, 30, 31, 32, 33, 34, 36, 38, 40 (vòng eo tính theo cm)</small>
            </div>
          </div>

          <!-- Color Presets -->
          <div class="settings-card">
            <h5 class="card-title">
              <i class="bi bi-palette me-2"></i>
              Màu sắc phổ biến
            </h5>
            
            <div class="color-presets-grid">
              <div
                v-for="(color, index) in settings.products.colors"
                :key="index"
                class="color-preset-card"
              >
                <div class="color-swatch" :style="{ backgroundColor: color.hex }"></div>
                <input
                  type="text"
                  v-model="color.name"
                  class="form-control form-control-sm"
                  placeholder="Tên màu"
                />
                <input
                  type="color"
                  v-model="color.hex"
                  class="form-control form-control-color form-control-sm"
                />
                <button type="button" @click="removeColor(index)" class="btn btn-sm btn-outline-danger">
                  <i class="bi bi-trash"></i>
                </button>
              </div>
              
              <div class="color-preset-card add-color-card" @click="addNewColor">
                <i class="bi bi-plus-circle"></i>
                <span>Thêm màu mới</span>
              </div>
            </div>
          </div>

          <!-- Material Presets -->
          <div class="settings-card">
            <h5 class="card-title">
              <i class="bi bi-tags me-2"></i>
              Chất liệu phổ biến
            </h5>
            
            <div class="material-tags-group">
              <span
                v-for="(material, index) in settings.products.materials"
                :key="index"
                class="material-tag"
              >
                {{ material }}
                <button type="button" @click="removeMaterial(index)" class="tag-remove-btn">
                  <i class="bi bi-x"></i>
                </button>
              </span>
              <input
                type="text"
                v-model="newMaterial"
                @keydown.enter="addMaterial"
                class="material-input"
                placeholder="Thêm chất liệu..."
              />
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
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

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

// New inputs
const newSizeAo = ref('')
const newSizeQuan = ref('')
const newMaterial = ref('')

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
    sizes: {
      ao: ['S', 'M', 'L', 'XL', 'XXL', '3XL'],
      quan: ['28', '29', '30', '31', '32', '33', '34', '36', '38', '40']
    },
    colors: [
      { name: 'Đen', hex: '#000000' },
      { name: 'Trắng', hex: '#FFFFFF' },
      { name: 'Xám', hex: '#808080' },
      { name: 'Xanh navy', hex: '#000080' },
      { name: 'Nâu', hex: '#8B4513' },
      { name: 'Be', hex: '#F5F5DC' }
    ],
    materials: [
      'Cotton 100%',
      'Cotton Polyester (80/20)',
      'Linen',
      'Kaki',
      'Jean/Denim',
      'Vải Wool'
    ]
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

// Methods
const addSize = (type) => {
  if (type === 'ao' && newSizeAo.value.trim()) {
    if (!settings.value.products.sizes.ao.includes(newSizeAo.value.trim().toUpperCase())) {
      settings.value.products.sizes.ao.push(newSizeAo.value.trim().toUpperCase())
    }
    newSizeAo.value = ''
  } else if (type === 'quan' && newSizeQuan.value.trim()) {
    if (!settings.value.products.sizes.quan.includes(newSizeQuan.value.trim())) {
      settings.value.products.sizes.quan.push(newSizeQuan.value.trim())
    }
    newSizeQuan.value = ''
  }
}

const removeSize = (type, index) => {
  settings.value.products.sizes[type].splice(index, 1)
}

const addNewColor = () => {
  settings.value.products.colors.push({
    name: 'Màu mới',
    hex: '#000000'
  })
}

const removeColor = (index) => {
  if (settings.value.products.colors.length > 1) {
    settings.value.products.colors.splice(index, 1)
  } else {
    alert('Cần ít nhất 1 màu!')
  }
}

const addMaterial = () => {
  if (newMaterial.value.trim()) {
    if (!settings.value.products.materials.includes(newMaterial.value.trim())) {
      settings.value.products.materials.push(newMaterial.value.trim())
    }
    newMaterial.value = ''
  }
}

const removeMaterial = (index) => {
  settings.value.products.materials.splice(index, 1)
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
loadSettings()
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
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
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
}

.color-preset-card:hover {
  border-color: #6366f1;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.1);
}

.color-swatch {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  border: 2px solid #e2e8f0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
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
</style>

