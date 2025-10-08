import { ERROR_MESSAGES, ERROR_TITLES, ERROR_CATEGORIES, getErrorMessage, getErrorTitle } from './errorMessages.js'

/**
 * Standardized error handler for consistent error handling across the application
 */
export class StandardErrorHandler {
  constructor(toast = null) {
    this.toast = toast
  }

  /**
   * Handle API errors with standardized messages
   */
  handleApiError(error, context = '') {
    let category = ERROR_CATEGORIES.GENERIC
    let message = ERROR_MESSAGES.UNKNOWN_ERROR
    let title = ERROR_TITLES.GENERIC

    if (error.response) {
      // Server responded with error status
      const { status, data } = error.response
      
      switch (status) {
        case 400:
          category = ERROR_CATEGORIES.VALIDATION
          title = ERROR_TITLES.VALIDATION
          message = data?.message || ERROR_MESSAGES.BAD_REQUEST
          break
        case 401:
          category = ERROR_CATEGORIES.AUTH
          title = ERROR_TITLES.AUTH
          message = ERROR_MESSAGES.UNAUTHORIZED
          break
        case 403:
          category = ERROR_CATEGORIES.PERMISSION
          title = ERROR_TITLES.PERMISSION
          message = ERROR_MESSAGES.FORBIDDEN
          break
        case 404:
          category = ERROR_CATEGORIES.NOT_FOUND
          title = ERROR_TITLES.NOT_FOUND
          message = data?.message || ERROR_MESSAGES.NOT_FOUND
          break
        case 422:
          category = ERROR_CATEGORIES.VALIDATION
          title = ERROR_TITLES.VALIDATION
          message = data?.message || ERROR_MESSAGES.VALIDATION_ERROR
          break
        case 500:
        case 502:
        case 503:
        case 504:
          category = ERROR_CATEGORIES.SERVER
          title = ERROR_TITLES.SERVER
          message = ERROR_MESSAGES.SERVER_ERROR
          break
        default:
          message = data?.message || ERROR_MESSAGES.API_ERROR
      }
    } else if (error.request) {
      // Request was made but no response received
      category = ERROR_CATEGORIES.NETWORK
      title = ERROR_TITLES.NETWORK
      message = ERROR_MESSAGES.NETWORK_ERROR
    } else if (error.code === 'ECONNABORTED') {
      // Timeout error
      category = ERROR_CATEGORIES.NETWORK
      title = ERROR_TITLES.NETWORK
      message = ERROR_MESSAGES.TIMEOUT_ERROR
    } else {
      // Something else happened
      message = error.message || ERROR_MESSAGES.UNKNOWN_ERROR
    }

    return this.showError({
      title: context ? `${context} - ${title}` : title,
      message,
      category,
      originalError: error
    })
  }

  /**
   * Handle network errors
   */
  handleNetworkError(error) {
    return this.showError({
      title: ERROR_TITLES.NETWORK,
      message: ERROR_MESSAGES.NETWORK_ERROR,
      category: ERROR_CATEGORIES.NETWORK,
      originalError: error
    })
  }

  /**
   * Handle validation errors
   */
  handleValidationError(errors, customMessage = null) {
    let message = customMessage || ERROR_MESSAGES.VALIDATION_ERROR
    
    if (Array.isArray(errors)) {
      message = errors.join(', ')
    } else if (typeof errors === 'object') {
      const errorMessages = Object.values(errors).flat()
      message = errorMessages.join(', ')
    }
    
    return this.showError({
      title: ERROR_TITLES.VALIDATION,
      message,
      category: ERROR_CATEGORIES.VALIDATION,
      type: 'warning'
    })
  }

  /**
   * Handle authentication errors
   */
  handleAuthError(error, customMessage = null) {
    return this.showError({
      title: ERROR_TITLES.AUTH,
      message: customMessage || ERROR_MESSAGES.UNAUTHORIZED,
      category: ERROR_CATEGORIES.AUTH,
      originalError: error
    })
  }

  /**
   * Handle permission errors
   */
  handlePermissionError(error, customMessage = null) {
    return this.showError({
      title: ERROR_TITLES.PERMISSION,
      message: customMessage || ERROR_MESSAGES.FORBIDDEN,
      category: ERROR_CATEGORIES.PERMISSION,
      originalError: error
    })
  }

  /**
   * Handle not found errors
   */
  handleNotFoundError(error, customMessage = null) {
    return this.showError({
      title: ERROR_TITLES.NOT_FOUND,
      message: customMessage || ERROR_MESSAGES.NOT_FOUND,
      category: ERROR_CATEGORIES.NOT_FOUND,
      originalError: error
    })
  }

  /**
   * Handle generic errors
   */
  handleGenericError(error, customMessage = null) {
    return this.showError({
      title: ERROR_TITLES.GENERIC,
      message: customMessage || ERROR_MESSAGES.UNKNOWN_ERROR,
      category: ERROR_CATEGORIES.GENERIC,
      originalError: error
    })
  }

  /**
   * Show error with toast notification
   */
  showError({ title, message, category, type = 'error', duration = 5000, originalError = null }) {
    // Log error in development
    if (import.meta.env.DEV && originalError) {
      console.error('Error:', originalError)
    }

    // Show toast notification
    if (this.toast) {
      this.toast.error(message, {
        title,
        duration
      })
    } else {
      // Fallback to custom event
      window.dispatchEvent(new CustomEvent('show-toast', {
        detail: {
          type,
          title,
          message,
          duration
        }
      }))
    }

    return {
      title,
      message,
      category,
      type,
      originalError
    }
  }

  /**
   * Show success message
   */
  showSuccess(message, title = 'Thành công', duration = 3000) {
    if (this.toast) {
      this.toast.success(message, { title, duration })
    } else {
      window.dispatchEvent(new CustomEvent('show-toast', {
        detail: {
          type: 'success',
          title,
          message,
          duration
        }
      }))
    }
  }

  /**
   * Show warning message
   */
  showWarning(message, title = 'Cảnh báo', duration = 4000) {
    if (this.toast) {
      this.toast.warning(message, { title, duration })
    } else {
      window.dispatchEvent(new CustomEvent('show-toast', {
        detail: {
          type: 'warning',
          title,
          message,
          duration
        }
      }))
    }
  }

  /**
   * Show info message
   */
  showInfo(message, title = 'Thông tin', duration = 3000) {
    if (this.toast) {
      this.toast.info(message, { title, duration })
    } else {
      window.dispatchEvent(new CustomEvent('show-toast', {
        detail: {
          type: 'info',
          title,
          message,
          duration
        }
      }))
    }
  }
}

// Create global error handler instance
export const errorHandler = new StandardErrorHandler()

// Export convenience functions
export const handleApiError = (error, context) => errorHandler.handleApiError(error, context)
export const handleNetworkError = (error) => errorHandler.handleNetworkError(error)
export const handleValidationError = (errors, message) => errorHandler.handleValidationError(errors, message)
export const handleAuthError = (error, message) => errorHandler.handleAuthError(error, message)
export const handlePermissionError = (error, message) => errorHandler.handlePermissionError(error, message)
export const handleNotFoundError = (error, message) => errorHandler.handleNotFoundError(error, message)
export const handleGenericError = (error, message) => errorHandler.handleGenericError(error, message)

export const showError = (options) => errorHandler.showError(options)
export const showSuccess = (message, title, duration) => errorHandler.showSuccess(message, title, duration)
export const showWarning = (message, title, duration) => errorHandler.showWarning(message, title, duration)
export const showInfo = (message, title, duration) => errorHandler.showInfo(message, title, duration)
