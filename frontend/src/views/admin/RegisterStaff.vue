<template>
  <div class="register-staff">
    <div class="container py-4">
      <nav aria-label="breadcrumb">
        <ol class="breadcrumb">
          <li class="breadcrumb-item">
            <router-link to="/admin">Admin</router-link>
          </li>
          <li class="breadcrumb-item active">Đăng ký nhân viên</li>
        </ol>
      </nav>

      <div class="row justify-content-center">
        <div class="col-lg-6">
          <div class="card">
            <div class="card-header bg-warning">
              <h4 class="mb-0">
                <i class="bi bi-person-plus me-2"></i>Đăng ký tài khoản nhân viên
              </h4>
            </div>
            <div class="card-body">
              <div v-if="successMessage" class="alert alert-success">
                <div class="d-flex align-items-start">
                  <i class="bi bi-check-circle-fill fs-3 me-3 text-success"></i>
                  <div class="flex-grow-1">
                    <h5 class="alert-heading mb-2">
                      <i class="bi bi-check-circle me-2"></i>{{ successMessage }}
                    </h5>
                    <hr />
                    <div v-if="createdStaff" class="mt-3">
                      <h6 class="mb-3"><strong>Thông tin nhân viên:</strong></h6>
                      <table class="table table-sm table-bordered mb-3">
                        <tbody>
                          <tr>
                            <td class="fw-bold" style="width: 150px">Họ tên:</td>
                            <td>{{ createdStaff.hoTen }}</td>
                          </tr>
                          <tr>
                            <td class="fw-bold">Email:</td>
                            <td>{{ createdStaff.email }}</td>
                          </tr>
                          <tr v-if="createdStaff.soDienThoai">
                            <td class="fw-bold">Số điện thoại:</td>
                            <td>{{ createdStaff.soDienThoai }}</td>
                          </tr>
                          <tr>
                            <td class="fw-bold">Vai trò:</td>
                            <td>
                              <span
                                class="badge"
                                :class="
                                  createdStaff.vaiTro === 'ADM' ? 'bg-danger' : 'bg-primary'
                                "
                              >
                                {{ createdStaff.vaiTro === 'ADM' ? 'Quản trị viên' : 'Nhân viên' }}
                              </span>
                            </td>
                          </tr>
                          <tr>
                            <td class="fw-bold">ID:</td>
                            <td><code>{{ createdStaff.id }}</code></td>
                          </tr>
                        </tbody>
                      </table>
                      <div class="d-flex gap-2">
                        <button type="button" class="btn btn-success" @click="createAnother">
                          <i class="bi bi-plus-circle me-2"></i>Tạo nhân viên khác
                        </button>
                        <router-link to="/admin/users" class="btn btn-outline-primary">
                          <i class="bi bi-people me-2"></i>Xem danh sách người dùng
                        </router-link>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div v-if="errorMessage" class="alert alert-danger alert-dismissible fade show">
                <i class="bi bi-exclamation-triangle me-2"></i>{{ errorMessage }}
                <button type="button" class="btn-close" @click="errorMessage = null"></button>
              </div>

              <form v-if="!successMessage" @submit.prevent="handleSubmit">
                <!-- Họ tên -->
                <div class="mb-3">
                  <label class="form-label">
                    Họ và tên <span class="text-danger">*</span>
                  </label>
                  <input
                    v-model="form.hoTen"
                    type="text"
                    class="form-control"
                    placeholder="Nhập họ tên"
                    required
                  />
                </div>

                <!-- Email -->
                <div class="mb-3">
                  <label class="form-label">
                    Email <span class="text-danger">*</span>
                  </label>
                  <input
                    v-model="form.email"
                    type="email"
                    class="form-control"
                    placeholder="email@example.com"
                    required
                  />
                </div>

                <!-- Số điện thoại -->
                <div class="mb-3">
                  <label class="form-label">Số điện thoại</label>
                  <input
                    v-model="form.soDienThoai"
                    type="tel"
                    class="form-control"
                    placeholder="0123456789"
                    pattern="[0-9]{10,11}"
                  />
                  <small class="text-muted">10-11 chữ số</small>
                </div>

                <!-- Vai trò -->
                <div class="mb-3">
                  <label class="form-label">
                    Vai trò <span class="text-danger">*</span>
                  </label>
                  <select v-model="form.loaiTaiKhoan" class="form-select" required>
                    <option value="">-- Chọn vai trò --</option>
                    <option value="STF">Nhân viên (Staff)</option>
                    <option value="ADM">Quản trị viên (Admin)</option>
                  </select>
                </div>

                <!-- Mật khẩu -->
                <div class="mb-3">
                  <label class="form-label">
                    Mật khẩu <span class="text-danger">*</span>
                  </label>
                  <div class="input-group">
                    <input
                      v-model="form.matKhau"
                      :type="showPassword ? 'text' : 'password'"
                      class="form-control"
                      placeholder="Ít nhất 6 ký tự"
                      minlength="6"
                      required
                    />
                    <button
                      class="btn btn-outline-secondary"
                      type="button"
                      @click="showPassword = !showPassword"
                    >
                      <i :class="showPassword ? 'bi bi-eye-slash' : 'bi bi-eye'"></i>
                    </button>
                  </div>
                </div>

                <!-- Xác nhận mật khẩu -->
                <div class="mb-3">
                  <label class="form-label">
                    Xác nhận mật khẩu <span class="text-danger">*</span>
                  </label>
                  <div class="input-group">
                    <input
                      v-model="form.xacNhanMatKhau"
                      :type="showConfirmPassword ? 'text' : 'password'"
                      class="form-control"
                      placeholder="Nhập lại mật khẩu"
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

                <!-- Buttons -->
                <div class="d-flex gap-2">
                  <button type="submit" class="btn btn-warning" :disabled="isSubmitting">
                    <span v-if="isSubmitting" class="spinner-border spinner-border-sm me-2"></span>
                    <i v-else class="bi bi-check-circle me-2"></i>
                    {{ isSubmitting ? 'Đang xử lý...' : 'Đăng ký' }}
                  </button>
                  <button type="button" class="btn btn-outline-secondary" @click="resetForm">
                    <i class="bi bi-arrow-clockwise me-2"></i>Đặt lại
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'

const form = ref({
  hoTen: '',
  email: '',
  soDienThoai: '',
  loaiTaiKhoan: '',
  matKhau: '',
  xacNhanMatKhau: '',
})

const showPassword = ref(false)
const showConfirmPassword = ref(false)
const isSubmitting = ref(false)
const successMessage = ref(null)
const errorMessage = ref(null)
const createdStaff = ref(null)

const resetForm = () => {
  form.value = {
    hoTen: '',
    email: '',
    soDienThoai: '',
    loaiTaiKhoan: '',
    matKhau: '',
    xacNhanMatKhau: '',
  }
}

const createAnother = () => {
  successMessage.value = null
  errorMessage.value = null
  createdStaff.value = null
  resetForm()
}

const handleSubmit = async () => {
  // Validate
  if (form.value.matKhau !== form.value.xacNhanMatKhau) {
    errorMessage.value = 'Mật khẩu xác nhận không khớp'
    return
  }

  if (form.value.matKhau.length < 6) {
    errorMessage.value = 'Mật khẩu phải có ít nhất 6 ký tự'
    return
  }

  isSubmitting.value = true
  errorMessage.value = null
  successMessage.value = null

  try {
    const token = localStorage.getItem('auro_token')
    if (!token) {
      errorMessage.value = 'Bạn chưa đăng nhập'
      return
    }

    const baseURL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

    const response = await axios.post(
      `${baseURL}/admin/register-staff`,
      {
        hoTen: form.value.hoTen,
        email: form.value.email,
        soDienThoai: form.value.soDienThoai || null,
        loaiTaiKhoan: form.value.loaiTaiKhoan,
        matKhau: form.value.matKhau,
      },
      {
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      }
    )

    if (response.data.success) {
      successMessage.value = response.data.message || 'Đăng ký nhân viên thành công!'
      
      // Lưu thông tin nhân viên vừa tạo
      createdStaff.value = {
        id: response.data.data?.id,
        hoTen: response.data.data?.hoTen || form.value.hoTen,
        email: response.data.data?.email || form.value.email,
        soDienThoai: response.data.data?.soDienThoai || form.value.soDienThoai,
        vaiTro: response.data.data?.vaiTro || form.value.loaiTaiKhoan,
      }
      
      // Chỉ reset form, không reset success message và createdStaff
      resetForm()
    } else {
      errorMessage.value = response.data.message || 'Đăng ký thất bại'
    }
  } catch (error) {
    console.error('Register staff error:', error)
    errorMessage.value =
      error.response?.data?.message ||
      error.response?.data ||
      'Có lỗi xảy ra khi đăng ký. Vui lòng thử lại!'
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
.register-staff {
  min-height: 100vh;
  background-color: #f8f9fa;
  padding-top: 6.5rem;
}

.card {
  border: none;
  border-radius: 0.5rem;
  box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
}

.card-header {
  border-radius: 0.5rem 0.5rem 0 0 !important;
  color: #000;
  font-weight: 500;
}

.form-control:focus,
.form-select:focus {
  border-color: #ffc107;
  box-shadow: 0 0 0 0.2rem rgba(255, 193, 7, 0.25);
}

.btn-warning {
  font-weight: 500;
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
