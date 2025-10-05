import { ref } from 'vue'

// Global toast state
const toasts = ref([])
let toastId = 0

export const useToast = () => {
  // Add toast
  const addToast = (toast) => {
    const id = ++toastId
    const newToast = {
      id,
      type: 'info',
      title: '',
      message: '',
      duration: 5000,
      closable: true,
      ...toast
    }

    toasts.value.push(newToast)

    // Auto remove after duration
    if (newToast.duration > 0) {
      setTimeout(() => {
        removeToast(id)
      }, newToast.duration)
    }

    return id
  }

  // Remove toast
  const removeToast = (id) => {
    const index = toasts.value.findIndex(toast => toast.id === id)
    if (index > -1) {
      toasts.value.splice(index, 1)
    }
  }

  // Clear all toasts
  const clearAll = () => {
    toasts.value = []
  }

  // Show success toast
  const success = (message, options = {}) => {
    return addToast({
      type: 'success',
      title: 'Thành công',
      message,
      duration: 3000,
      ...options
    })
  }

  // Show error toast
  const error = (message, options = {}) => {
    return addToast({
      type: 'error',
      title: 'Lỗi',
      message,
      duration: 5000,
      ...options
    })
  }

  // Show warning toast
  const warning = (message, options = {}) => {
    return addToast({
      type: 'warning',
      title: 'Cảnh báo',
      message,
      duration: 4000,
      ...options
    })
  }

  // Show info toast
  const info = (message, options = {}) => {
    return addToast({
      type: 'info',
      title: 'Thông tin',
      message,
      duration: 3000,
      ...options
    })
  }

  // Show loading toast
  const loading = (message, options = {}) => {
    return addToast({
      type: 'loading',
      title: 'Đang xử lý',
      message,
      duration: 0, // Don't auto remove
      closable: false,
      ...options
    })
  }

  // Update toast
  const updateToast = (id, updates) => {
    const toast = toasts.value.find(t => t.id === id)
    if (toast) {
      Object.assign(toast, updates)
    }
  }

  // Promise-based toast for async operations
  const promise = async (promise, messages = {}) => {
    const {
      loading: loadingMessage = 'Đang xử lý...',
      success: successMessage = 'Thành công!',
      error: errorMessage = 'Có lỗi xảy ra!'
    } = messages

    const loadingId = loading(loadingMessage)

    try {
      const result = await promise
      updateToast(loadingId, {
        type: 'success',
        title: 'Thành công',
        message: successMessage,
        duration: 3000,
        closable: true
      })
      return result
    } catch (err) {
      updateToast(loadingId, {
        type: 'error',
        title: 'Lỗi',
        message: errorMessage,
        duration: 5000,
        closable: true
      })
      throw err
    }
  }

  return {
    // State
    toasts,

    // Actions
    addToast,
    removeToast,
    clearAll,
    updateToast,

    // Convenience methods
    success,
    error,
    warning,
    info,
    loading,
    promise
  }
}

// Global toast instance
export const toast = useToast()
