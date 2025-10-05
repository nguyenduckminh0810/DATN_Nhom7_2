import { ref } from 'vue'

// Global error state
const globalError = ref(null)
const isErrorVisible = ref(false)

export const useErrorHandler = () => {
  const showError = (error, options = {}) => {
    const {
      title = 'Có lỗi xảy ra',
      message = 'Vui lòng thử lại sau',
      type = 'error',
      duration = 5000,
      showToast = true
    } = options

    // Set global error
    globalError.value = {
      title,
      message: error?.message || message,
      type,
      timestamp: Date.now()
    }

    // Show error toast if enabled
    if (showToast) {
      // This will be handled by the Toast component
      window.dispatchEvent(new CustomEvent('show-toast', {
        detail: {
          type,
          title,
          message: error?.message || message,
          duration
        }
      }))
    }

    // Auto hide after duration
    if (duration > 0) {
      setTimeout(() => {
        hideError()
      }, duration)
    }

    // Log error to console in development
    if (import.meta.env.DEV) {
      console.error('Error:', error)
    }
  }

  const hideError = () => {
    globalError.value = null
    isErrorVisible.value = false
  }

  const showSuccess = (message, options = {}) => {
    const {
      title = 'Thành công',
      duration = 3000
    } = options

    window.dispatchEvent(new CustomEvent('show-toast', {
      detail: {
        type: 'success',
        title,
        message,
        duration
      }
    }))
  }

  const showWarning = (message, options = {}) => {
    const {
      title = 'Cảnh báo',
      duration = 4000
    } = options

    window.dispatchEvent(new CustomEvent('show-toast', {
      detail: {
        type: 'warning',
        title,
        message,
        duration
      }
    }))
  }

  const showInfo = (message, options = {}) => {
    const {
      title = 'Thông tin',
      duration = 3000
    } = options

    window.dispatchEvent(new CustomEvent('show-toast', {
      detail: {
        type: 'info',
        title,
        message,
        duration
      }
    }))
  }

  // Handle API errors
  const handleApiError = (error, context = '') => {
    let message = 'Có lỗi xảy ra'
    let title = 'Lỗi hệ thống'

    if (error.response) {
      // Server responded with error status
      const { status, data } = error.response
      
      switch (status) {
        case 400:
          title = 'Dữ liệu không hợp lệ'
          message = data.message || 'Vui lòng kiểm tra lại thông tin'
          break
        case 401:
          title = 'Chưa đăng nhập'
          message = 'Vui lòng đăng nhập để tiếp tục'
          break
        case 403:
          title = 'Không có quyền truy cập'
          message = 'Bạn không có quyền thực hiện hành động này'
          break
        case 404:
          title = 'Không tìm thấy'
          message = 'Dữ liệu bạn tìm kiếm không tồn tại'
          break
        case 422:
          title = 'Dữ liệu không hợp lệ'
          message = data.message || 'Vui lòng kiểm tra lại thông tin'
          break
        case 500:
          title = 'Lỗi server'
          message = 'Server đang gặp sự cố, vui lòng thử lại sau'
          break
        default:
          message = data.message || 'Có lỗi xảy ra từ server'
      }
    } else if (error.request) {
      // Request was made but no response received
      title = 'Lỗi kết nối'
      message = 'Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng'
    } else {
      // Something else happened
      message = error.message || 'Có lỗi không xác định'
    }

    showError(error, {
      title: context ? `${context} - ${title}` : title,
      message
    })
  }

  // Handle form validation errors
  const handleValidationError = (errors) => {
    const errorMessages = Object.values(errors).flat()
    const message = errorMessages.join(', ')
    
    showError(new Error(message), {
      title: 'Lỗi xác thực',
      type: 'warning'
    })
  }

  // Handle network errors
  const handleNetworkError = (error) => {
    showError(error, {
      title: 'Lỗi mạng',
      message: 'Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng của bạn.',
      type: 'error'
    })
  }

  // Handle timeout errors
  const handleTimeoutError = (error) => {
    showError(error, {
      title: 'Hết thời gian chờ',
      message: 'Yêu cầu của bạn mất quá nhiều thời gian để xử lý. Vui lòng thử lại.',
      type: 'warning'
    })
  }

  return {
    globalError,
    isErrorVisible,
    showError,
    hideError,
    showSuccess,
    showWarning,
    showInfo,
    handleApiError,
    handleValidationError,
    handleNetworkError,
    handleTimeoutError
  }
}

// Global error handler for unhandled errors
export const setupGlobalErrorHandler = (app) => {
  // Vue error handler
  app.config.errorHandler = (err, vm, info) => {
    console.error('Vue error:', err, info)
    
    // Show user-friendly error message
    window.dispatchEvent(new CustomEvent('show-toast', {
      detail: {
        type: 'error',
        title: 'Lỗi ứng dụng',
        message: 'Có lỗi xảy ra trong ứng dụng. Vui lòng tải lại trang.',
        duration: 5000
      }
    }))
  }

  // Global unhandled promise rejection
  window.addEventListener('unhandledrejection', (event) => {
    console.error('Unhandled promise rejection:', event.reason)
    
    // Show user-friendly error message
    window.dispatchEvent(new CustomEvent('show-toast', {
      detail: {
        type: 'error',
        title: 'Lỗi hệ thống',
        message: 'Có lỗi xảy ra. Vui lòng thử lại sau.',
        duration: 5000
      }
    }))
    
    // Prevent the default behavior
    event.preventDefault()
  })

  // Global error handler
  window.addEventListener('error', (event) => {
    console.error('Global error:', event.error)
    
    // Show user-friendly error message
    window.dispatchEvent(new CustomEvent('show-toast', {
      detail: {
        type: 'error',
        title: 'Lỗi hệ thống',
        message: 'Có lỗi xảy ra. Vui lòng tải lại trang.',
        duration: 5000
      }
    }))
  })
}
