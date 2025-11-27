import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import apiService from '../services/api'
import { handleApiError, handleNetworkError } from '../utils/errorHandler'
import { useApiCache } from '../composables/useCache'

export const useProductStore = defineStore('product', () => {
  // Cache instance
  const apiCache = useApiCache({
    ttl: 5 * 60 * 1000, // 5 minutes
    maxSize: 100,
  })

  // Product State
  const products = ref([])
  const categories = ref([])
  const currentProduct = ref(null)
  const relatedProducts = ref([])
  const productReviews = ref([])
  const bestSellers = ref([])
  const isLoading = ref(false)
  const error = ref(null)
  const pagination = ref({
    page: 1,
    limit: 12,
    total: 0,
    totalPages: 0,
  })

  // Product Getters
  const hasProducts = computed(() => products.value.length > 0)
  const hasCategories = computed(() => categories.value.length > 0)
  const productCount = computed(() => products.value.length)
  const categoryCount = computed(() => categories.value.length)

  // Actions
  const setLoading = (loading) => {
    isLoading.value = loading
  }

  const setError = (err) => {
    error.value = err
  }

  const clearError = () => {
    error.value = null
  }

  // Standardized error handling
  const handleError = (err, context = 'Product') => {
    const errorResult = handleApiError(err, context)
    setError(errorResult.message)
    return { success: false, message: errorResult.message }
  }

  const setProducts = (newProducts) => {
    products.value = newProducts
  }

  const setCategories = (newCategories) => {
    categories.value = newCategories
  }

  const setCurrentProduct = (product) => {
    currentProduct.value = product
  }

  const setRelatedProducts = (newProducts) => {
    relatedProducts.value = newProducts
  }

  const setProductReviews = (reviews) => {
    productReviews.value = reviews
  }

  const setPagination = (paginationData) => {
    pagination.value = { ...pagination.value, ...paginationData }
  }

  // Fetch all products
  const fetchProducts = async (params = {}) => {
    try {
      setLoading(true)
      clearError()

      // Create cache key from parameters
      const cacheKey = `products_${JSON.stringify(params)}`

      // Try to get from cache first
      const cached = apiCache.get(cacheKey)
      if (cached) {
        setProducts(cached.products)
        setPagination(cached.pagination)
        setLoading(false)
        return { success: true, data: cached, fromCache: true }
      }

      const response = await apiService.products.getAll(params)

      if (response.success) {
        setProducts(response.data.products)
        setPagination(response.data.pagination)

        // Cache the response
        apiCache.set(cacheKey, response.data)

        return { success: true, data: response.data }
      } else {
        setError(response.message)
        return { success: false, message: response.message }
      }
    } catch (err) {
      return handleError(err, 'Products')
    } finally {
      setLoading(false)
    }
  }

  // Fetch product by ID
  const fetchProductById = async (id) => {
    try {
      setLoading(true)
      clearError()

      // Create cache key
      const cacheKey = `product_${id}`

      // Try to get from cache first
      const cached = apiCache.get(cacheKey)
      if (cached) {
        setCurrentProduct(cached.product)
        setLoading(false)
        return { success: true, data: cached, fromCache: true }
      }

      const response = await apiService.products.getById(id)

      if (response.success) {
        setCurrentProduct(response.data.product)

        // Cache the response
        apiCache.set(cacheKey, response.data)

        return { success: true, data: response.data }
      } else {
        setError(response.message)
        return { success: false, message: response.message }
      }
    } catch (err) {
      return handleError(err, 'Products')
    } finally {
      setLoading(false)
    }
  }

  // Fetch products by category
  const fetchProductsByCategory = async (categoryId, params = {}) => {
    try {
      setLoading(true)
      clearError()

      const response = await apiService.products.getByCategory(categoryId, params)

      if (response.success) {
        setProducts(response.data.products)
        setPagination(response.data.pagination)
        return { success: true, data: response.data }
      } else {
        setError(response.message)
        return { success: false, message: response.message }
      }
    } catch (err) {
      return handleError(err, 'Products')
    } finally {
      setLoading(false)
    }
  }

  // Search products
  const searchProducts = async (query, params = {}) => {
    try {
      setLoading(true)
      clearError()

      const response = await apiService.products.search(query, params)

      if (response.success) {
        setProducts(response.data.products)
        setPagination(response.data.pagination)
        return { success: true, data: response.data }
      } else {
        setError(response.message)
        return { success: false, message: response.message }
      }
    } catch (err) {
      return handleError(err, 'Products')
    } finally {
      setLoading(false)
    }
  }

  // Fetch related products
  const fetchRelatedProducts = async (id) => {
    try {
      setLoading(true)
      clearError()

      const response = await apiService.products.getRelated(id)

      if (response.success) {
        setRelatedProducts(response.data.products)
        return { success: true, data: response.data }
      } else {
        setError(response.message)
        return { success: false, message: response.message }
      }
    } catch (err) {
      return handleError(err, 'Products')
    } finally {
      setLoading(false)
    }
  }

  // Fetch product reviews
  const fetchProductReviews = async (id) => {
    try {
      setLoading(true)
      clearError()

      const response = await apiService.products.getReviews(id)

      if (response.success) {
        setProductReviews(response.data.reviews)
        return { success: true, data: response.data }
      } else {
        setError(response.message)
        return { success: false, message: response.message }
      }
    } catch (err) {
      return handleError(err, 'Products')
    } finally {
      setLoading(false)
    }
  }

  // Add product review
  const addProductReview = async (id, reviewData) => {
    try {
      setLoading(true)
      clearError()

      const response = await apiService.products.addReview(id, reviewData)

      if (response.success) {
        // Refresh reviews
        await fetchProductReviews(id)
        return { success: true, data: response.data }
      } else {
        setError(response.message)
        return { success: false, message: response.message }
      }
    } catch (err) {
      return handleError(err, 'Products')
    } finally {
      setLoading(false)
    }
  }

  // Fetch all categories
  const fetchCategories = async () => {
    try {
      setLoading(true)
      clearError()

      const response = await apiService.categories.getAll()

      if (response.success) {
        setCategories(response.data.categories)
        return { success: true, data: response.data }
      } else {
        setError(response.message)
        return { success: false, message: response.message }
      }
    } catch (err) {
      return handleError(err, 'Products')
    } finally {
      setLoading(false)
    }
  }

  // Fetch category by ID
  const fetchCategoryById = async (id) => {
    try {
      setLoading(true)
      clearError()

      const response = await apiService.categories.getById(id)

      if (response.success) {
        return { success: true, data: response.data }
      } else {
        setError(response.message)
        return { success: false, message: response.message }
      }
    } catch (err) {
      return handleError(err, 'Products')
    } finally {
      setLoading(false)
    }
  }

  // Fetch best sellers
  const fetchBestSellers = async (params = {}) => {
    try {
      setLoading(true)
      clearError()

      // Create cache key from parameters
      const cacheKey = `bestsellers_${JSON.stringify(params)}`

      // Try to get from cache first
      const cached = apiCache.get(cacheKey)
      if (cached) {
        setLoading(false)
        return { success: true, data: cached, fromCache: true }
      }

      const response = await apiService.products.getBestSellers(params)

      if (response.success || response.content) {
        // Handle Spring Boot Page response
        const productsData = response.content || response.data?.products || []
        const pagination = {
          page: response.number || response.page || 0,
          limit: response.size || response.limit || 10,
          total: response.totalElements || response.total || 0,
          totalPages: response.totalPages || response.totalPages || 0,
        }

        const data = { products: productsData, pagination }

        // Save to state
        bestSellers.value = productsData

        // Cache the response
        apiCache.set(cacheKey, data)

        return { success: true, data }
      } else {
        setError(response.message || 'Không thể lấy sản phẩm bán chạy')
        return { success: false, message: response.message }
      }
    } catch (err) {
      return handleError(err, 'Best Sellers')
    } finally {
      setLoading(false)
    }
  }

  // Get product by ID from current products
  const getProductById = (id) => {
    return products.value.find((product) => product.id === id)
  }

  // Get category by ID
  const getCategoryById = (id) => {
    return categories.value.find((category) => category.id === id)
  }

  // Get products by category ID
  const getProductsByCategoryId = (categoryId) => {
    return products.value.filter((product) => product.categoryId === categoryId)
  }

  // Clear current product
  const clearCurrentProduct = () => {
    currentProduct.value = null
    relatedProducts.value = []
    productReviews.value = []
  }

  // Clear all data
  const clearAll = () => {
    products.value = []
    // removed featured state
    categories.value = []
    currentProduct.value = null
    relatedProducts.value = []
    productReviews.value = []
    pagination.value = {
      page: 1,
      limit: 12,
      total: 0,
      totalPages: 0,
    }
    clearError()
  }

  return {
    // Product State
    products,
    categories,
    currentProduct,
    relatedProducts,
    productReviews,
    bestSellers,
    isLoading,
    error,
    pagination,

    // Product Getters
    hasProducts,
    hasCategories,
    productCount,
    categoryCount,

    // Product Actions
    fetchProducts,
    fetchProductById,
    fetchProductsByCategory,
    searchProducts,
    fetchRelatedProducts,
    fetchProductReviews,
    addProductReview,
    fetchCategories,
    fetchCategoryById,
    fetchBestSellers,
    getProductById,
    getCategoryById,
    getProductsByCategoryId,
    clearCurrentProduct,
    clearAll,
    clearError,
  }
})
