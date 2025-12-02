<template>
  <div class="profile">
    <div class="container py-4">
      <!-- Breadcrumb -->
      <nav aria-label="breadcrumb">
        <ol class="breadcrumb">
          <li class="breadcrumb-item">
            <router-link to="/">Trang chủ</router-link>
          </li>
          <li class="breadcrumb-item active" aria-current="page">Thông tin cá nhân</li>
        </ol>
      </nav>

      <div class="row">
        <div class="col-12">
          <h1 class="h2 fw-bold mb-4">Thông tin cá nhân</h1>
        </div>
      </div>

      <div class="row">
        <!-- Profile Sidebar -->
        <div class="col-lg-3 mb-4">
          <div class="card">
            <div class="card-body text-center">
              <div class="profile-avatar mb-3">
                <img
                  :src="getAvatarUrl(user.avatar)"
                  alt="Avatar"
                  class="rounded-circle"
                  width="100"
                  height="100"
                />
              </div>
              <h5 class="mb-1">{{ user.name }}</h5>
              <p class="text-muted small mb-3">{{ user.email }}</p>
              <input
                ref="fileInput"
                type="file"
                accept="image/*"
                style="display: none"
                @change="handleAvatarUpload"
              />
              <button class="btn btn-outline-secondary btn-sm" @click="triggerFileUpload">
                <i class="bi bi-camera me-1"></i>Đổi ảnh
              </button>
            </div>
          </div>

          <!-- Navigation Menu -->
          <div class="card mt-3">
            <div class="list-group list-group-flush">
              <router-link to="/profile" class="list-group-item list-group-item-action active py-3">
                <i class="bi bi-person me-2"></i>Thông tin cá nhân
              </router-link>
              <router-link to="/orders" class="list-group-item list-group-item-action py-3">
                <i class="bi bi-bag me-2"></i>Đơn hàng của tôi
              </router-link>
              <router-link to="/addresses" class="list-group-item list-group-item-action py-3">
                <i class="bi bi-geo-alt me-2"></i>Địa chỉ giao hàng
              </router-link>
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

        <!-- Profile Content -->
        <div class="col-lg-9">
          <div class="card">
            <div class="card-header">
              <h5 class="mb-0"><i class="bi bi-person me-2"></i>Thông tin cá nhân</h5>
            </div>
            <div class="card-body">
              <form @submit.prevent="handleUpdateProfile">
                <div class="row">
                  <!-- Full Name -->
                  <div class="col-md-6 mb-3">
                    <label class="form-label">Họ và tên *</label>
                    <input v-model="form.fullName" type="text" class="form-control" required />
                  </div>

                  <!-- Email -->
                  <div class="col-md-6 mb-3">
                    <label class="form-label">Email *</label>
                    <input v-model="form.email" type="email" class="form-control" disabled />
                  </div>

                  <!-- Phone -->
                  <div class="col-md-6 mb-3">
                    <label class="form-label">Số điện thoại *</label>
                    <input v-model="form.phone" type="tel" class="form-control" disabled />
                  </div>
                </div>

                <!-- Submit Button -->
                <div class="d-flex gap-3">
                  <button type="submit" class="btn btn-warning" :disabled="isSubmitting">
                    <span v-if="isSubmitting" class="spinner-border spinner-border-sm me-2"></span>
                    <i v-else class="bi bi-check-circle me-2"></i>
                    {{ isSubmitting ? 'Đang cập nhật...' : 'Cập nhật thông tin' }}
                  </button>
                </div>
              </form>
            </div>
          </div>

          <!-- Change Password -->
          <div class="card mt-4">
            <div class="card-header">
              <h5 class="mb-0"><i class="bi bi-lock me-2"></i>Đổi mật khẩu</h5>
            </div>
            <div class="card-body">
              <form @submit.prevent="handleChangePassword">
                <div class="row">
                  <!-- Current Password -->
                  <div class="col-md-6 mb-3">
                    <label class="form-label">Mật khẩu hiện tại *</label>
                    <div class="input-group">
                      <input
                        :type="showCurrentPassword ? 'text' : 'password'"
                        v-model="passwordForm.currentPassword"
                        class="form-control"
                        required
                      />
                      <button
                        class="btn btn-outline-secondary"
                        type="button"
                        @click="showCurrentPassword = !showCurrentPassword"
                      >
                        <i :class="showCurrentPassword ? 'bi bi-eye-slash' : 'bi bi-eye'"></i>
                      </button>
                    </div>
                  </div>

                  <!-- New Password -->
                  <div class="col-md-6 mb-3">
                    <label class="form-label">Mật khẩu mới *</label>
                    <div class="input-group">
                      <input
                        :type="showNewPassword ? 'text' : 'password'"
                        v-model="passwordForm.newPassword"
                        class="form-control"
                        required
                      />
                      <button
                        class="btn btn-outline-secondary"
                        type="button"
                        @click="showNewPassword = !showNewPassword"
                      >
                        <i :class="showNewPassword ? 'bi bi-eye-slash' : 'bi bi-eye'"></i>
                      </button>
                    </div>
                  </div>

                  <!-- Confirm New Password -->
                  <div class="col-md-6 mb-3">
                    <label class="form-label">Xác nhận mật khẩu mới *</label>
                    <div class="input-group">
                      <input
                        :type="showConfirmPassword ? 'text' : 'password'"
                        v-model="passwordForm.confirmPassword"
                        class="form-control"
                        required
                      />
                      <button
                        class="btn btn-outline-secondary"
                        type="button"
                        @click="showConfirmPassword = !showConfirmPassword"
                      >
                        <i :class="showConfirmPassword ? 'bi bi-eye-slash' : 'bi bi-eye'"></i>
                      </button>
                    </div>
                  </div>
                </div>

                <!-- Submit Button -->
                <button type="submit" class="btn btn-warning" :disabled="isChangingPassword">
                  <span
                    v-if="isChangingPassword"
                    class="spinner-border spinner-border-sm me-2"
                  ></span>
                  <i v-else class="bi bi-key me-2"></i>
                  {{ isChangingPassword ? 'Đang đổi mật khẩu...' : 'Đổi mật khẩu' }}
                </button>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import apiService from '@/services/api'

const router = useRouter()
const userStore = useUserStore()

// Reactive data
const isSubmitting = ref(false)
const isChangingPassword = ref(false)
const showCurrentPassword = ref(false)
const showNewPassword = ref(false)
const showConfirmPassword = ref(false)
const fileInput = ref(null)

const user = ref({
  id: 1,
  name: 'Nguyễn Văn A',
  email: 'user@example.com',
  phone: '0123456789',
  avatar: null,
})

const form = ref({
  fullName: '',
  email: '',
  phone: '',
  dateOfBirth: '',
  gender: '',
  subscribeNewsletter: false,
})

const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: '',
})

// Methods
const loadUserData = async () => {
  try {
    const response = await apiService.user.getProfile()

    if (response.success && response.data) {
      const data = response.data

      const userData = {
        id: data.id,
        name: data.fullName || data.email?.split('@')[0],
        email: data.email,
        phone: data.phone,
        avatar: data.avatar,
        role: data.role,
      }

      user.value = userData

      localStorage.setItem('auro_user', JSON.stringify(userData))

      form.value = {
        fullName: userData.name || '',
        email: userData.email || '',
        phone: userData.phone || '',
        dateOfBirth: '',
        gender: '',
        subscribeNewsletter: false,
      }
    }
  } catch (error) {
    console.error('Load user data error:', error)
    const storedUser = localStorage.getItem('auro_user')
    if (storedUser) {
      user.value = JSON.parse(storedUser)
      form.value = {
        fullName: user.value.name || '',
        email: user.value.email || '',
        phone: user.value.phone || '',
        dateOfBirth: '',
        gender: '',
        subscribeNewsletter: false,
      }
    }
  }
}

const handleUpdateProfile = async () => {
  isSubmitting.value = true

  try {
    const payload = {
      fullName: form.value.fullName,
    }

    const response = await apiService.user.updateProfile(payload)

    if (response.success && response.data) {
      const data = response.data
      const updatedUser = {
        id: data.id,
        name: data.fullName,
        email: data.email,
        phone: data.phone,
        avatar: data.avatar,
        role: data.role,
      }

      user.value = updatedUser
      localStorage.setItem('auro_user', JSON.stringify(updatedUser))

      alert('Cập nhật thông tin thành công!')
    } else {
      alert(response.message || 'Có lỗi xảy ra khi cập nhật thông tin')
    }
  } catch (error) {
    console.error('Update profile error:', error)
    alert(error.message || 'Có lỗi xảy ra khi cập nhật thông tin')
  } finally {
    isSubmitting.value = false
  }
}

const handleChangePassword = async () => {
  // Validation
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    alert('Mật khẩu xác nhận không khớp')
    return
  }

  if (passwordForm.value.newPassword.length < 6) {
    alert('Mật khẩu mới phải có ít nhất 6 ký tự')
    return
  }

  isChangingPassword.value = true

  try {
    const payload = {
      currentPassword: passwordForm.value.currentPassword,
      newPassword: passwordForm.value.newPassword,
    }

    const response = await apiService.user.changePassword(payload)

    if (response.success) {
      passwordForm.value = {
        currentPassword: '',
        newPassword: '',
        confirmPassword: '',
      }
      alert('Đổi mật khẩu thành công!')
    } else {
      alert(response.message || 'Có lỗi xảy ra khi đổi mật khẩu')
    }
  } catch (error) {
    console.error('Change password error:', error)
    alert(error.message || 'Có lỗi xảy ra khi đổi mật khẩu')
  } finally {
    isChangingPassword.value = false
  }
}

const resetForm = () => {
  loadUserData()
}

// Avatar upload
const triggerFileUpload = () => {
  fileInput.value?.click()
}

const getAvatarUrl = (avatar) => {
  console.log('getAvatarUrl called with:', avatar)
  if (!avatar) {
    return 'https://via.placeholder.com/100x100/6c757d/ffffff?text=Avatar'
  }
  // Nếu avatar đã là URL đầy đủ thì trả về luôn
  if (avatar.startsWith('http')) {
    return avatar
  }
  // Nếu là đường dẫn tương đối thì thêm base URL
  const baseURL = import.meta.env.VITE_API_URL?.replace('/api', '') || 'http://localhost:8080'
  const fullUrl = baseURL + avatar
  console.log('Avatar URL:', fullUrl)
  return fullUrl
}

const handleAvatarUpload = async (event) => {
  const file = event.target.files?.[0]
  if (!file) return

  // Validate file type
  if (!file.type.startsWith('image/')) {
    alert('Vui lòng chọn file ảnh')
    return
  }

  // Validate file size (max 5MB)
  if (file.size > 5 * 1024 * 1024) {
    alert('Kích thước ảnh không được vượt quá 5MB')
    return
  }

  try {
    console.log('Starting upload...')
    // Upload to server
    const response = await apiService.user.uploadAvatar(file)
    console.log('Upload response:', response)
    
    if (response.success && response.data?.avatarUrl) {
      console.log('Upload successful, fetching user info...')
      // Lấy lại thông tin user từ server
      const userResponse = await apiService.auth.me()
      console.log('User info response:', userResponse)
      
      if (userResponse.success && userResponse.data) {
        console.log('Avatar from server:', userResponse.data.avatar)
        // Cập nhật avatar trong user object
        user.value.avatar = userResponse.data.avatar
        
        // Cập nhật localStorage
        const storedUser = JSON.parse(localStorage.getItem('auro_user') || '{}')
        storedUser.avatar = userResponse.data.avatar
        localStorage.setItem('auro_user', JSON.stringify(storedUser))
        
        console.log('Avatar updated, current user.value.avatar:', user.value.avatar)
        alert('Cập nhật ảnh đại diện thành công!')
      }
    } else {
      alert(response.message || 'Có lỗi xảy ra khi tải ảnh lên')
    }
  } catch (error) {
    console.error('Upload avatar error:', error)
    alert('Có lỗi xảy ra khi tải ảnh lên. Vui lòng thử lại!')
  }

  // Reset input
  event.target.value = ''
}

// Logout
const logout = () => {
  if (confirm('Bạn có chắc chắn muốn đăng xuất?')) {
    userStore.logout()
    router.push('/')
  }
}

// Lifecycle
onMounted(() => {
  loadUserData()
})
</script>

<style scoped>
.profile {
  min-height: 100vh;
  background-color: #f8f9fa;
  padding: 6.5rem 0 2rem;
}

@media (max-width: 768px) {
  .profile {
    padding: 5rem 0 1.5rem;
  }
}

@media (max-width: 576px) {
  .profile {
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

.form-control {
  border-radius: 0.5rem;
  border: 1px solid #dee2e6;
}

.form-control:focus {
  border-color: #ffc107;
  box-shadow: 0 0 0 0.2rem rgba(255, 193, 7, 0.25);
}

.btn-warning {
  border-radius: 0.5rem;
  font-weight: 500;
}

.input-group .btn {
  border-color: #dee2e6;
}

.input-group .btn:hover {
  background-color: #e9ecef;
  border-color: #adb5bd;
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
