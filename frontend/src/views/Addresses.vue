<template>
  <div class="addresses">
    <div class="container py-4">
      <!-- Breadcrumb -->
      <nav aria-label="breadcrumb">
        <ol class="breadcrumb">
          <li class="breadcrumb-item">
            <router-link to="/">Trang chủ</router-link>
          </li>
          <li class="breadcrumb-item active" aria-current="page">Địa chỉ giao hàng</li>
        </ol>
      </nav>

      <div class="row">
        <div class="col-12">
          <h1 class="h2 fw-bold mb-4">Địa chỉ giao hàng</h1>
        </div>
      </div>

      <div class="row">
        <!-- Profile Sidebar -->
        <div class="col-lg-3 mb-4">
          <div class="card">
            <div class="card-body text-center">
              <div class="profile-avatar mb-3">
                <img
                  src="https://via.placeholder.com/100x100/f8f9fa/6c757d?text=User"
                  alt="Avatar"
                  class="rounded-circle"
                  width="100"
                  height="100"
                />
              </div>
              <h5 class="mb-1">{{ user?.name || 'User' }}</h5>
              <p class="text-muted small mb-0">{{ user?.email || '' }}</p>
            </div>
          </div>

          <!-- Navigation Menu -->
          <div class="card mt-3">
            <div class="list-group list-group-flush">
              <router-link to="/profile" class="list-group-item list-group-item-action py-3">
                <i class="bi bi-person me-2"></i>Thông tin cá nhân
              </router-link>
              <router-link to="/orders" class="list-group-item list-group-item-action py-3">
                <i class="bi bi-bag me-2"></i>Đơn hàng của tôi
              </router-link>
              <router-link
                to="/addresses"
                class="list-group-item list-group-item-action active py-3"
              >
                <i class="bi bi-geo-alt me-2"></i>Địa chỉ giao hàng
              </router-link>
              <a href="#" class="list-group-item list-group-item-action py-3">
                <i class="bi bi-bell me-2"></i>Thông báo
              </a>
              <a
                href="#"
                class="list-group-item list-group-item-action text-danger py-3"
                @click.prevent="logout"
              >
                <i class="bi bi-box-arrow-right me-2"></i>Đăng xuất
              </a>
            </div>
          </div>
        </div>

        <!-- Addresses Content -->
        <div class="col-lg-9">
          <div class="card mt-3">
            <div class="card-header d-flex justify-content-between align-items-center">
              <h5 class="mb-0"><i class="bi bi-geo-alt me-2"></i>Danh sách địa chỉ</h5>
              <button class="btn btn-warning btn-sm shadow-sm" @click="showAddressForm()">
                <i class="bi bi-plus-lg me-1"></i>Thêm địa chỉ mới
              </button>
            </div>

            <div class="card-body">
              <!-- Loading State -->
              <div v-if="isLoading" class="text-center py-5">
                <div class="spinner-border text-warning" role="status">
                  <span class="visually-hidden">Đang tải...</span>
                </div>
              </div>

              <!-- Empty State -->
              <div v-else-if="addresses.length === 0" class="text-center py-5">
                <i class="bi bi-geo-alt text-muted" style="font-size: 3rem"></i>
                <p class="text-muted mt-3">Bạn chưa có địa chỉ giao hàng nào</p>
                <button class="btn btn-warning shadow-sm" @click="showAddressForm()">
                  <i class="bi bi-plus-lg me-1"></i>Thêm địa chỉ mới
                </button>
              </div>

              <!-- Address List -->
              <div v-else class="row g-3">
                <div v-for="address in addresses" :key="address.id" class="col-12 col-md-6">
                  <div class="card address-card" :class="{ 'border-warning': address.macDinh }">
                    <div class="card-body">
                      <div class="d-flex justify-content-between align-items-start mb-2">
                        <h6 class="fw-bold mb-0">
                          {{ address.hoTen }}
                          <span v-if="address.macDinh" class="badge bg-warning text-dark ms-2">
                            Mặc định
                          </span>
                        </h6>
                        <div class="btn-group btn-group-sm">
                          <button
                            class="btn btn-outline-primary"
                            type="button"
                            :disabled="isLoading || isSaving"
                            @click="editAddress(address)"
                            title="Chỉnh sửa"
                          >
                            <i class="bi bi-pencil"></i>
                          </button>
                          <button
                            v-if="!address.macDinh"
                            class="btn btn-outline-warning"
                            type="button"
                            :disabled="isLoading || isSaving"
                            @click="setAsDefault(address.id)"
                            title="Đặt làm mặc định"
                          >
                            <i class="bi bi-star"></i>
                          </button>
                          <button
                            class="btn btn-outline-danger"
                            type="button"
                            :disabled="isLoading || isSaving"
                            @click="deleteAddress(address.id)"
                            title="Xóa"
                          >
                            <i class="bi bi-trash"></i>
                          </button>
                        </div>
                      </div>
                      <p class="text-muted small mb-1">
                        <i class="bi bi-telephone me-1"></i>{{ address.soDienThoai }}
                      </p>
                      <p class="text-muted small mb-0">
                        <i class="bi bi-geo-alt me-1"></i>
                        {{ formatFullAddress(address) }}
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Address Form Modal -->
    <div
      class="modal fade"
      id="addressModal"
      tabindex="-1"
      aria-labelledby="addressModalLabel"
      aria-hidden="true"
    >
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="addressModalLabel">
              {{ isEditMode ? 'Chỉnh sửa địa chỉ' : 'Thêm địa chỉ mới' }}
            </h5>
            <button
              type="button"
              class="btn-close"
              data-bs-dismiss="modal"
              aria-label="Close"
            ></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="saveAddress" id="addressForm">
              <div class="mb-3">
                <label class="form-label">Họ và tên *</label>
                <input
                  v-model.trim="addressForm.hoTen"
                  type="text"
                  class="form-control"
                  autocomplete="name"
                  maxlength="100"
                  required
                />
              </div>

              <div class="mb-3">
                <label class="form-label">Số điện thoại *</label>
                <input
                  v-model.trim="addressForm.soDienThoai"
                  type="tel"
                  class="form-control"
                  inputmode="tel"
                  autocomplete="tel"
                  pattern="^(0|\+84)[0-9]{9,10}$"
                  title="Số điện thoại Việt Nam hợp lệ (bắt đầu 0 hoặc +84)"
                  required
                />
              </div>

              <div class="mb-3">
                <label class="form-label">Tỉnh/Thành phố *</label>
                <select
                  v-model="selectedProvinceId"
                  class="form-select"
                  @change="onProvinceChange"
                  :disabled="loadingGHN.provinces"
                  required
                >
                  <option :value="null">Chọn tỉnh/thành phố</option>
                  <option
                    v-for="province in ghnProvinces"
                    :key="province.ProvinceID"
                    :value="province.ProvinceID"
                  >
                    {{ province.ProvinceName }}
                  </option>
                </select>
                <small v-if="loadingGHN.provinces" class="text-muted">Đang tải...</small>
              </div>

              <div class="mb-3">
                <label class="form-label">Quận/Huyện *</label>
                <select
                  v-model="selectedDistrictId"
                  class="form-select"
                  @change="onDistrictChange"
                  :disabled="loadingGHN.districts || !selectedProvinceId"
                  required
                >
                  <option :value="null">Chọn quận/huyện</option>
                  <option
                    v-for="district in ghnDistricts"
                    :key="district.DistrictID"
                    :value="district.DistrictID"
                  >
                    {{ district.DistrictName }}
                  </option>
                </select>
                <small v-if="loadingGHN.districts" class="text-muted">Đang tải...</small>
              </div>

              <div class="mb-3">
                <label class="form-label">Phường/Xã *</label>
                <select
                  v-model="selectedWardCode"
                  class="form-select"
                  @change="onWardChange"
                  :disabled="loadingGHN.wards || !selectedDistrictId"
                  required
                >
                  <option :value="null">Chọn phường/xã</option>
                  <option
                    v-for="ward in ghnWards"
                    :key="ward.WardCode"
                    :value="ward.WardCode"
                  >
                    {{ ward.WardName }}
                  </option>
                </select>
                <small v-if="loadingGHN.wards" class="text-muted">Đang tải...</small>
              </div>

              <div class="mb-3">
                <label class="form-label">Địa chỉ cụ thể *</label>
                <textarea
                  v-model.trim="addressForm.diaChi1"
                  class="form-control"
                  rows="3"
                  autocomplete="address-line1"
                  placeholder="Số nhà, tên đường, quận/huyện..."
                  maxlength="255"
                  required
                ></textarea>
              </div>

              <div class="mb-3 form-check">
                <input
                  v-model="addressForm.macDinh"
                  type="checkbox"
                  class="form-check-input"
                  id="defaultAddress"
                />
                <label class="form-check-label" for="defaultAddress">
                  Đặt làm địa chỉ mặc định
                </label>
              </div>

              <div class="d-flex gap-2">
                <button
                  type="submit"
                  class="btn btn-warning flex-fill"
                  :disabled="isSaving"
                  @click="handleSaveClick"
                >
                  <span v-if="isSaving" class="spinner-border spinner-border-sm me-2"></span>
                  {{ isSaving ? 'Đang lưu...' : 'Lưu địa chỉ' }}
                </button>
                <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">
                  Hủy
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import addressService from '@/services/addressService'
import shippingService from '@/services/shippingService'
import provinces from '@/data/provinces'
import { Modal } from 'bootstrap'
import { useToast } from '@/composables/useToast'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const { success, error, warning, info } = useToast()

const user = ref(null)
const addresses = ref([])
const isLoading = ref(false)
const isSaving = ref(false)
const isEditMode = ref(false)
const editingAddressId = ref(null)

const addressForm = ref({
  hoTen: '',
  soDienThoai: '',
  diaChi1: '',
  phuongXa: '',
  quanHuyen: 'N/A', // Giá trị mặc định vì backend vẫn yêu cầu
  tinhThanh: '',
  macDinh: false,
})

// GHN API dropdowns
const ghnProvinces = ref([])
const ghnDistricts = ref([])
const ghnWards = ref([])

const selectedProvinceId = ref(null)
const selectedDistrictId = ref(null)
const selectedWardCode = ref(null)

const loadingGHN = ref({
  provinces: false,
  districts: false,
  wards: false,
})

// Provinces filter for autocomplete (legacy)
const provinceSearchText = ref('')
const filteredProvinces = computed(() => {
  if (!provinceSearchText.value) {
    return provinces
  }
  const search = provinceSearchText.value.toLowerCase()
  return provinces.filter((p) => p.toLowerCase().includes(search))
})

const filterProvinces = (event) => {
  provinceSearchText.value = event.target.value
}

// Format full address
const formatFullAddress = (address) => {
  const parts = [
    address.diaChi1,
    address.phuongXa,
    address.quanHuyen && address.quanHuyen !== 'N/A' ? address.quanHuyen : null,
    address.tinhThanh,
  ].filter(Boolean) // Remove empty/null values

  return parts.join(', ')
}

let addressModal = null

// Load user data
const loadUserData = () => {
  const storedUser = localStorage.getItem('auro_user')
  if (storedUser) {
    user.value = JSON.parse(storedUser)
    const userRole = user.value.vaiTroMa || user.value.vaiTro || user.value.role
    console.log('👤 [Addresses] User role:', userRole)
  }
}

// Load addresses
const loadAddresses = async () => {
  console.log('🔄 [Addresses] loadAddresses called')
  isLoading.value = true
  try {
    const token = localStorage.getItem('auro_token')
    const storedUser = localStorage.getItem('auro_user')
    console.log('🔑 [Addresses] Token exists:', !!token)
    console.log('👤 [Addresses] User:', storedUser ? JSON.parse(storedUser) : null)
    
    console.log('📞 [Addresses] Calling addressService.getAllAddresses()')
    const response = await addressService.getAllAddresses()
    console.log('✅ [Addresses] getAllAddresses response:', response)

    if (response.success) {
      addresses.value = response.data || []
      console.log('📦 [Addresses] Loaded addresses:', addresses.value.length)
    } else {
      addresses.value = []
      console.log('⚠️ [Addresses] Response not successful:', response.message)
    }
  } catch (err) {
    console.error('❌ [Addresses] Error loading addresses:', err)
    console.error('📊 [Addresses] Error status:', err.response?.status)
    console.error('📊 [Addresses] Error data:', err.response?.data)

    // Hiển thị lỗi cụ thể
    if (err.response?.status === 403) {
      console.warn('⛔ [Addresses] 403 Forbidden')
      error('Không có quyền truy cập: ' + (err.response?.data?.message || 'Bạn không có quyền quản lý địa chỉ'))
    } else {
      error('Có lỗi khi tải danh sách địa chỉ: ' + (err.response?.data?.message || err.message))
    }
    addresses.value = []
  } finally {
    isLoading.value = false
  }
}

// Load GHN provinces
const loadGHNProvinces = async () => {
  loadingGHN.value.provinces = true
  try {
    ghnProvinces.value = await shippingService.getProvinces()
    console.log('✅ Loaded GHN provinces:', ghnProvinces.value.length)
  } catch (err) {
    console.error('Error loading GHN provinces:', err)
    error('Không thể tải danh sách tỉnh/thành phố')
  } finally {
    loadingGHN.value.provinces = false
  }
}

// Load GHN districts
const loadGHNDistricts = async (provinceId) => {
  if (!provinceId) {
    ghnDistricts.value = []
    ghnWards.value = []
    selectedDistrictId.value = null
    selectedWardCode.value = null
    return
  }

  loadingGHN.value.districts = true
  try {
    ghnDistricts.value = await shippingService.getDistricts(provinceId)
    console.log('✅ Loaded GHN districts:', ghnDistricts.value.length)
  } catch (err) {
    console.error('Error loading GHN districts:', err)
    error('Không thể tải danh sách quận/huyện')
  } finally {
    loadingGHN.value.districts = false
  }
}

// Load GHN wards
const loadGHNWards = async (districtId) => {
  if (!districtId) {
    ghnWards.value = []
    selectedWardCode.value = null
    return
  }

  loadingGHN.value.wards = true
  try {
    ghnWards.value = await shippingService.getWards(districtId)
    console.log('✅ Loaded GHN wards:', ghnWards.value.length)
  } catch (err) {
    console.error('Error loading GHN wards:', err)
    error('Không thể tải danh sách phường/xã')
  } finally {
    loadingGHN.value.wards = false
  }
}

// Handle province change
const onProvinceChange = async () => {
  // Update addressForm with selected province name
  const province = ghnProvinces.value.find((p) => p.ProvinceID === selectedProvinceId.value)
  if (province) {
    addressForm.value.tinhThanh = province.ProvinceName
  }

  // Reset lower levels
  selectedDistrictId.value = null
  selectedWardCode.value = null
  addressForm.value.quanHuyen = 'N/A'
  addressForm.value.phuongXa = ''

  // Load districts
  await loadGHNDistricts(selectedProvinceId.value)
}

// Handle district change
const onDistrictChange = async () => {
  // Update addressForm with selected district name
  const district = ghnDistricts.value.find((d) => d.DistrictID === selectedDistrictId.value)
  if (district) {
    addressForm.value.quanHuyen = district.DistrictName
  }

  // Reset ward
  selectedWardCode.value = null
  addressForm.value.phuongXa = ''

  // Load wards
  await loadGHNWards(selectedDistrictId.value)
}

// Handle ward change
const onWardChange = () => {
  // Update addressForm with selected ward name
  const ward = ghnWards.value.find((w) => w.WardCode === selectedWardCode.value)
  if (ward) {
    addressForm.value.phuongXa = ward.WardName
  }
}

// Show address form
const showAddressForm = async (address = null) => {
  console.log('showAddressForm called with:', address) // Debug log

  if (address) {
    isEditMode.value = true
    editingAddressId.value = address.id
    // Use Object.assign to keep proxy
    Object.assign(addressForm.value, {
      hoTen: address.hoTen,
      soDienThoai: address.soDienThoai,
      diaChi1: address.diaChi1,
      phuongXa: address.phuongXa || '',
      quanHuyen: address.quanHuyen || 'N/A',
      tinhThanh: address.tinhThanh,
      macDinh: address.macDinh,
    })

    // Try to find and select in GHN dropdowns
    const province = addressService.findProvinceInGHN(address.tinhThanh, ghnProvinces.value)
    if (province) {
      selectedProvinceId.value = province.ProvinceID
      await loadGHNDistricts(province.ProvinceID)

      const district = addressService.findDistrictInGHN(address.quanHuyen, ghnDistricts.value)
      if (district) {
        selectedDistrictId.value = district.DistrictID
        await loadGHNWards(district.DistrictID)

        const ward = addressService.findWardInGHN(address.phuongXa, ghnWards.value)
        if (ward) {
          selectedWardCode.value = ward.WardCode
        }
      }
    }
  } else {
    isEditMode.value = false
    editingAddressId.value = null
    // Use Object.assign to keep proxy
    Object.assign(addressForm.value, {
      hoTen: '',
      soDienThoai: '',
      diaChi1: '',
      phuongXa: '',
      quanHuyen: 'N/A',
      tinhThanh: '',
      macDinh: false,
    })

    // Reset GHN dropdowns
    selectedProvinceId.value = null
    selectedDistrictId.value = null
    selectedWardCode.value = null
    ghnDistricts.value = []
    ghnWards.value = []
  }
  provinceSearchText.value = ''

  if (addressModal) {
    addressModal.show()
    console.log('Modal shown') // Debug log
  } else {
    console.error('Modal not initialized!') // Debug log
  }
}

// Edit address
const editAddress = (address) => {
  showAddressForm(address)
}

// Handle save button click for debugging
const handleSaveClick = (event) => {
  console.log('Save button clicked!', event) // Debug log
  console.log('Event type:', event.type) // Debug log

  // Check if form is valid
  const form = document.getElementById('addressForm')
  if (form && !form.checkValidity()) {
    console.log('Form is invalid!') // Debug log
    form.reportValidity()
    event.preventDefault()
    event.stopPropagation()
    return false
  }

  console.log('Form is valid, proceeding to save') // Debug log
}

// Save address
const saveAddress = async () => {
  // Guard against double submit
  if (isSaving.value) return

  console.log('saveAddress called') // Debug log
  console.log('Form data:', addressForm.value) // Debug log

  // Check token and user info (renamed to avoid shadowing)
  const token = localStorage.getItem('auro_token')
  const storedUserStr = localStorage.getItem('auro_user')
  console.log('Token exists:', !!token)
  console.log('User info:', storedUserStr ? JSON.parse(storedUserStr) : null)

  if (!token) {
    warning('Bạn chưa đăng nhập. Vui lòng đăng nhập để lưu địa chỉ.')
    return
  }

  isSaving.value = true
  try {
    let response
    if (isEditMode.value) {
      console.log('Updating address:', editingAddressId.value) // Debug log
      response = await addressService.updateAddress(editingAddressId.value, addressForm.value)
    } else {
      console.log('Creating new address') // Debug log
      response = await addressService.createAddress(addressForm.value)
    }

    console.log('Response:', response) // Debug log

    if (response.success) {
      addressModal.hide()

      // Reload addresses list
      console.log('Reloading addresses list...') // Debug log
      await loadAddresses()

      // Show success after reload
      success(isEditMode.value ? 'Cập nhật địa chỉ thành công!' : 'Thêm địa chỉ thành công!')
    } else {
      console.error('Response not successful:', response) // Debug log
      error('Có lỗi: ' + (response.message || 'Unknown error'))
    }
  } catch (error) {
    console.error('Error saving address:', error)
    console.error('Error response:', error.response) // Debug log
    console.error('Error response data:', error.response?.data) // Debug log
    console.error('Error status:', error.response?.status) // Debug log

    let errorMessage = error.response?.data?.message || error.message || 'Lỗi không xác định'

    // Handle 403 Forbidden - Not a customer
    if (error.response?.status === 403) {
      errorMessage =
        error.response?.data?.message ||
        'Chỉ tài khoản khách hàng mới có thể quản lý địa chỉ giao hàng. Vui lòng đăng nhập bằng tài khoản khách hàng.'
    }

    error('Có lỗi khi lưu địa chỉ: ' + errorMessage)
  } finally {
    isSaving.value = false
  }
}

// Set as default
const setAsDefault = async (id) => {
  if (!confirm('Bạn có muốn đặt địa chỉ này làm mặc định?')) return

  try {
    const response = await addressService.setDefaultAddress(id)
    console.log('setDefaultAddress response:', response) // Debug log

    if (response && response.success) {
      await loadAddresses()
      success('Đã đặt địa chỉ làm mặc định')
    } else {
      error('Có lỗi: ' + (response?.message || 'Không thể đặt địa chỉ mặc định'))
    }
  } catch (err) {
    console.error('Error setting default address:', err)
    error('Có lỗi khi đặt địa chỉ mặc định: ' + (err.response?.data?.message || err.message))
  }
}

// Delete address
const deleteAddress = async (id) => {
  if (!confirm('Bạn có chắc muốn xóa địa chỉ này?')) return

  try {
    const response = await addressService.deleteAddress(id)
    console.log('deleteAddress response:', response) // Debug log

    if (response && response.success) {
      await loadAddresses()
      success('Xóa địa chỉ thành công!')
    } else {
      error('Có lỗi: ' + (response?.message || 'Không thể xóa địa chỉ'))
    }
  } catch (err) {
    console.error('Error deleting address:', err)
    error('Có lỗi khi xóa địa chỉ: ' + (err.response?.data?.message || err.message))
  }
}

// Logout
const logout = () => {
  if (confirm('Bạn có chắc chắn muốn đăng xuất?')) {
    userStore.logout()
    router.push('/')
  }
}

onMounted(async () => {
  console.log('🚀 [Addresses] Component mounted!')
  loadUserData()
  await loadAddresses()

  // Load GHN provinces
  await loadGHNProvinces()

  // Khởi tạo modal
  const modalElement = document.getElementById('addressModal')
  if (modalElement) {
    addressModal = new Modal(modalElement)
    console.log('✅ [Addresses] Address modal initialized') // Debug log

    // Reset form when modal is hidden
    modalElement.addEventListener('hidden.bs.modal', () => {
      isEditMode.value = false
      editingAddressId.value = null
      Object.assign(addressForm.value, {
        hoTen: '',
        soDienThoai: '',
        diaChi1: '',
        phuongXa: '',
        quanHuyen: 'N/A',
        tinhThanh: '',
        macDinh: false,
      })
      provinceSearchText.value = ''
      
      // Reset GHN dropdowns
      selectedProvinceId.value = null
      selectedDistrictId.value = null
      selectedWardCode.value = null
      ghnDistricts.value = []
      ghnWards.value = []
    })
  } else {
    console.error('❌ [Addresses] Modal element not found!') // Debug log
  }
})
</script>

<style scoped>
.addresses {
  min-height: 100vh;
  background-color: #f8f9fa;
  padding: 6.5rem 0 2rem;
}

@media (max-width: 768px) {
  .addresses {
    padding: 5rem 0 1.5rem;
  }
}

@media (max-width: 576px) {
  .addresses {
    padding: 4.5rem 0 1.5rem;
  }
}

.profile-avatar img {
  object-fit: cover;
  border: 2px solid #ffc107;
}

.list-group-item-action.active {
  background-color: #ffc107;
  border-color: #ffc107;
  color: #000;
  font-weight: 500;
}

.list-group-item-action:hover {
  background-color: #f8f9fa;
}

.address-card {
  transition: all 0.3s ease;
  border-radius: 12px;
}

.address-card:hover {
  box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.address-card.border-warning {
  border-width: 2px;
}

.badge.bg-warning {
  font-weight: 600;
  color: #000;
}

.breadcrumb {
  background: none;
  padding: 0;
}

.breadcrumb-item a {
  text-decoration: none;
  color: #6c757d;
}

.breadcrumb-item a:hover {
  color: #ffc107;
}
</style>
