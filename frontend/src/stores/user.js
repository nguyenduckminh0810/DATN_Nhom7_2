import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import apiService from '../services/api'
import { handleApiError, handleAuthError } from '../utils/errorHandler'
import { validateToken } from '../utils/tokenValidator'

export const useUserStore = defineStore('user', () => {
  // State
  const user = ref(null)
  const isLoading = ref(false)
  const error = ref(null)

  // Getters
  const isAuthenticated = computed(() => !!user.value)
  const userRole = computed(() => {
    if (!user.value) return 'guest'

    // Backend trả về vaiTro với các giá trị: ADM, STF, CUS, GST
    const role = user.value.vaiTroMa || user.value.role || user.value.vaiTro || 'guest'

    // Map các giá trị từ backend sang frontend
    const roleMap = {
      ADM: 'admin',
      STF: 'staff',
      CUS: 'customer',
      GST: 'guest',
    }

    return roleMap[role] || role.toLowerCase()
  })
  const isAdmin = computed(() => userRole.value === 'admin')
  const userName = computed(() => user.value?.name || user.value?.hoTen || '')
  const userEmail = computed(() => user.value?.email || '')
  const userPhone = computed(() => user.value?.soDienThoai || user.value?.phone || user.value?.dienThoai || '')
  const userAvatar = computed(() => user.value?.avatar || null)

  // Actions
  const setUser = (userData) => {
    user.value = userData
    if (userData) {
      localStorage.setItem('auro_user', JSON.stringify(userData))
    } else {
      localStorage.removeItem('auro_user')
    }
  }

  const setToken = (token) => {
    if (token) {
      localStorage.setItem('auro_token', token)
    } else {
      localStorage.removeItem('auro_token')
    }
  }

  const login = async (credentials) => {
    try {
      isLoading.value = true
      error.value = null

      const response = await apiService.auth.login(credentials)

      if (response.success) {
        setToken(response.data.accessToken)
        setUser(response.data.user)
        return { success: true, data: response.data }
      } else {
        error.value = response.message
        return { success: false, message: response.message }
      }
    } catch (err) {
      // Không hiển thị toast khi login - để LoginPopup tự xử lý hiển thị lỗi
      return handleError(err, 'Login', false)
    } finally {
      isLoading.value = false
    }
  }

  const register = async (userData) => {
    try {
      isLoading.value = true
      error.value = null

      const response = await apiService.auth.register(userData)

      if (response.success) {
        setToken(response.data.accessToken)
        setUser(response.data.user)
        return { success: true, data: response.data }
      } else {
        error.value = response.message
        return { success: false, message: response.message }
      }
    } catch (err) {
      return handleError(err, 'Login')
    } finally {
      isLoading.value = false
    }
  }

  const logout = async () => {
    try {
      await apiService.auth.logout()
    } catch (err) {
      
    } finally {
      setUser(null)
      setToken(null)
      localStorage.removeItem('auro_redirect')
    }
  }

  const refreshToken = async () => {
    try {
      const response = await apiService.auth.refresh()
      if (response.success) {
        setToken(response.data.accessToken)
        return true
      }
      return false
    } catch (err) {
      
      logout()
      return false
    }
  }

  const updateProfile = async (profileData) => {
    try {
      isLoading.value = true
      error.value = null

      const response = await apiService.user.updateProfile(profileData)

      if (response.success) {
        setUser(response.data.user)
        return { success: true, data: response.data }
      } else {
        error.value = response.message
        return { success: false, message: response.message }
      }
    } catch (err) {
      return handleError(err, 'Login')
    } finally {
      isLoading.value = false
    }
  }

  const changePassword = async (passwordData) => {
    try {
      isLoading.value = true
      error.value = null

      const response = await apiService.user.changePassword(passwordData)

      if (response.success) {
        return { success: true, message: response.message }
      } else {
        error.value = response.message
        return { success: false, message: response.message }
      }
    } catch (err) {
      return handleError(err, 'Login')
    } finally {
      isLoading.value = false
    }
  }

  const uploadAvatar = async (file, onProgress) => {
    try {
      isLoading.value = true
      error.value = null

      const response = await apiService.user.uploadAvatar(file, onProgress)

      if (response.success) {
        setUser({ ...user.value, avatar: response.data.avatar })
        return { success: true, data: response.data }
      } else {
        error.value = response.message
        return { success: false, message: response.message }
      }
    } catch (err) {
      return handleError(err, 'Login')
    } finally {
      isLoading.value = false
    }
  }

  const forgotPassword = async (email) => {
    try {
      isLoading.value = true
      error.value = null

      const response = await apiService.auth.forgotPassword(email)

      if (response.success) {
        return { success: true, message: response.message }
      } else {
        error.value = response.message
        return { success: false, message: response.message }
      }
    } catch (err) {
      // Không hiển thị toast khi forgot password - để component tự xử lý
      return handleError(err, 'Quên mật khẩu', false)
    } finally {
      isLoading.value = false
    }
  }

  const resetPassword = async (token, password) => {
    try {
      isLoading.value = true
      error.value = null

      const response = await apiService.auth.resetPassword(token, password)

      if (response.success) {
        return { success: true, message: response.message }
      } else {
        error.value = response.message
        return { success: false, message: response.message }
      }
    } catch (err) {
      // Không hiển thị toast khi reset password - để component tự xử lý
      return handleError(err, 'Đặt lại mật khẩu', false)
    } finally {
      isLoading.value = false
    }
  }

  const verifyEmail = async (token) => {
    try {
      isLoading.value = true
      error.value = null

      const response = await apiService.auth.verifyEmail(token)

      if (response.success) {
        setUser(response.data.user)
        return { success: true, message: response.message }
      } else {
        error.value = response.message
        return { success: false, message: response.message }
      }
    } catch (err) {
      return handleError(err, 'Login')
    } finally {
      isLoading.value = false
    }
  }
  const getCurrentUser = async () => {
    try {
      isLoading.value = true
      error.value = null

      const response = await apiService.auth.me()

      if (response.success) {
        setUser(response.data)
        return { success: true, data: response.data }
      } else {
        error.value = response.message
        return { success: false, message: response.message }
      }
    } catch (err) {
      return handleError(err, 'Get Current User')
    } finally {
      isLoading.value = false
    }
  }

  const loadUserFromStorage = () => {
    try {
      const storedUser = localStorage.getItem('auro_user')
      const storedToken = localStorage.getItem('auro_token')

      if (storedUser && storedToken) {
        // 🔒 Validate token using utility
        const validation = validateToken(storedToken)
        
        if (!validation.valid) {
      
          localStorage.removeItem('auro_token')
          localStorage.removeItem('auro_user')
          return false
        }
        
        user.value = JSON.parse(storedUser)
        return true
      } else {
        
      }
    } catch (err) {
      
      localStorage.removeItem('auro_user')
      localStorage.removeItem('auro_token')
    }
    return false
  }

  const clearError = () => {
    error.value = null
  }

  // Standardized error handling
  const handleError = (err, context = 'User', showToast = true) => {
    // Nếu không muốn hiển thị toast (ví dụ khi đang login), chỉ lấy message
    if (!showToast) {
      let message = 'Có lỗi xảy ra'
      if (err.response) {
        const { status, data } = err.response
        if (status === 401 || status === 403) {
          message = data?.message || 'Tài khoản hoặc mật khẩu không đúng'
        } else if (status === 400 || status === 422) {
          message = data?.message || 'Thông tin không hợp lệ'
        } else {
          message = data?.message || 'Có lỗi xảy ra'
        }
      } else if (err.request) {
        message = 'Không thể kết nối đến server'
      } else {
        message = err.message || 'Có lỗi xảy ra'
      }
      error.value = message
      return { success: false, message }
    }
    
    // Hiển thị toast như bình thường
    const errorResult = handleApiError(err, context)
    error.value = errorResult.message
    return { success: false, message: errorResult.message }
  }

  // Initialize
  loadUserFromStorage()

  return {
    // State
    user,
    isLoading,
    error,

    // Getters
    isAuthenticated,
    userRole,
    isAdmin,
    userName,
    userEmail,
    userPhone,
    userAvatar,

    // Actions
    setUser,
    setToken,
    login,
    register,
    logout,
    refreshToken,
    updateProfile,
    changePassword,
    uploadAvatar,
    forgotPassword,
    resetPassword,
    verifyEmail,
    loadUserFromStorage,
    getCurrentUser,
    clearError,
  }
})
