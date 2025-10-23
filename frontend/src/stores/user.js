import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import apiService from '../services/api'
import { handleApiError, handleAuthError } from '../utils/errorHandler'

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
    console.log('User object:', user.value)
    console.log('Role from backend:', role)

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
      return handleError(err, 'Login')
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
      console.error('Logout error:', err)
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
      console.error('Token refresh error:', err)
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
      return handleError(err, 'Login')
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
      return handleError(err, 'Login')
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
        user.value = JSON.parse(storedUser)
        return true
      }
    } catch (err) {
      console.error('Error loading user from storage:', err)
      localStorage.removeItem('auro_user')
      localStorage.removeItem('auro_token')
    }
    return false
  }

  const clearError = () => {
    error.value = null
  }

  // Standardized error handling
  const handleError = (err, context = 'User') => {
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
