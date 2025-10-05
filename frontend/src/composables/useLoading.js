import { ref } from 'vue'

export const useLoading = () => {
  const isLoading = ref(false)
  const loadingText = ref('Đang tải...')

  // Set loading state
  const setLoading = (loading, text = 'Đang tải...') => {
    isLoading.value = loading
    loadingText.value = text
  }

  // Show loading
  const showLoading = (text = 'Đang tải...') => {
    setLoading(true, text)
  }

  // Hide loading
  const hideLoading = () => {
    setLoading(false)
  }

  // Execute function with loading state
  const withLoading = async (fn, loadingText = 'Đang xử lý...') => {
    try {
      showLoading(loadingText)
      const result = await fn()
      return result
    } finally {
      hideLoading()
    }
  }

  // Execute multiple functions with loading state
  const withLoadingMultiple = async (functions, loadingText = 'Đang xử lý...') => {
    try {
      showLoading(loadingText)
      const results = await Promise.all(functions.map(fn => fn()))
      return results
    } finally {
      hideLoading()
    }
  }

  // Execute function with loading state and error handling
  const withLoadingAndError = async (fn, options = {}) => {
    const {
      loadingText = 'Đang xử lý...',
      errorText = 'Có lỗi xảy ra',
      onError = null
    } = options

    try {
      showLoading(loadingText)
      const result = await fn()
      return { success: true, data: result }
    } catch (error) {
      console.error('Error in withLoadingAndError:', error)
      
      if (onError) {
        onError(error)
      }
      
      return { 
        success: false, 
        error: error.message || errorText,
        originalError: error
      }
    } finally {
      hideLoading()
    }
  }

  // Loading state for specific operations
  const createLoadingState = (initialText = 'Đang tải...') => {
    const state = ref({
      isLoading: false,
      text: initialText,
      progress: 0
    })

    const setLoading = (loading, text = initialText) => {
      state.value.isLoading = loading
      state.value.text = text
      if (!loading) {
        state.value.progress = 0
      }
    }

    const setProgress = (progress) => {
      state.value.progress = Math.max(0, Math.min(100, progress))
    }

    const showLoading = (text = initialText) => {
      setLoading(true, text)
    }

    const hideLoading = () => {
      setLoading(false)
    }

    return {
      state,
      setLoading,
      setProgress,
      showLoading,
      hideLoading
    }
  }

  // Global loading states for different operations
  const globalLoadingStates = ref({
    auth: false,
    products: false,
    cart: false,
    orders: false,
    profile: false
  })

  const setGlobalLoading = (key, loading) => {
    if (globalLoadingStates.value.hasOwnProperty(key)) {
      globalLoadingStates.value[key] = loading
    }
  }

  const getGlobalLoading = (key) => {
    return globalLoadingStates.value[key] || false
  }

  const isAnyGlobalLoading = () => {
    return Object.values(globalLoadingStates.value).some(loading => loading)
  }

  return {
    // State
    isLoading,
    loadingText,
    globalLoadingStates,

    // Actions
    setLoading,
    showLoading,
    hideLoading,
    withLoading,
    withLoadingMultiple,
    withLoadingAndError,

    // Global loading states
    setGlobalLoading,
    getGlobalLoading,
    isAnyGlobalLoading,

    // Factory
    createLoadingState
  }
}
