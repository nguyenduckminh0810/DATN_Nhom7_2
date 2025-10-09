import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import apiService from '../services/api'
import { handleApiError, handleNetworkError } from '../utils/errorHandler'
import { useApiCache } from '../composables/useCache'

export const useProductStore = defineStore('product', () => {
  // Cache instance
  const apiCache = useApiCache({
    ttl: 5 * 60 * 1000, // 5 minutes
    maxSize: 100
  })

  // Product State
  const products = ref([])
  const featuredProducts = ref([])
  const categories = ref([])
  const currentProduct = ref(null)
  const relatedProducts = ref([])
  const productReviews = ref([])
  const isLoading = ref(false)
  const error = ref(null)
  const pagination = ref({
    page: 1,
    limit: 12,
    total: 0,
    totalPages: 0
  })

  // Wishlist State
  const wishlistItems = ref([])

  // Product Getters
  const hasProducts = computed(() => products.value.length > 0)
  const hasFeaturedProducts = computed(() => featuredProducts.value.length > 0)
  const hasCategories = computed(() => categories.value.length > 0)
  const productCount = computed(() => products.value.length)
  const featuredCount = computed(() => featuredProducts.value.length)
  const categoryCount = computed(() => categories.value.length)

  // Wishlist Getters
  const wishlistCount = computed(() => wishlistItems.value.length)
  const isWishlistEmpty = computed(() => wishlistItems.value.length === 0)
  const wishlistItemIds = computed(() => wishlistItems.value.map(item => item.id))

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

  const setFeaturedProducts = (newProducts) => {
    featuredProducts.value = newProducts
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

  // Fetch featured products
  const fetchFeaturedProducts = async () => {
    try {
      setLoading(true)
      clearError()

      const response = await apiService.products.getFeatured()
      
      if (response.success) {
        setFeaturedProducts(response.data.products)
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

  // Get product by ID from current products
  const getProductById = (id) => {
    return products.value.find(product => product.id === id)
  }

  // Get category by ID
  const getCategoryById = (id) => {
    return categories.value.find(category => category.id === id)
  }

  // Get products by category ID
  const getProductsByCategoryId = (categoryId) => {
    return products.value.filter(product => product.categoryId === categoryId)
  }

  // Clear current product
  const clearCurrentProduct = () => {
    currentProduct.value = null
    relatedProducts.value = []
    productReviews.value = []
  }

  // Wishlist Actions
  const addToWishlist = (product) => {
    // Check if item already exists
    const existingItem = wishlistItems.value.find(item => item.id === product.id)
    
    if (!existingItem) {
      wishlistItems.value.push({
        ...product,
        addedAt: new Date().toISOString()
      })
      saveWishlistToStorage()
      return true // Successfully added
    }
    
    return false // Already exists
  }

  const removeFromWishlist = (productId) => {
    const index = wishlistItems.value.findIndex(item => item.id === productId)
    
    if (index > -1) {
      wishlistItems.value.splice(index, 1)
      saveWishlistToStorage()
      return true // Successfully removed
    }
    
    return false // Item not found
  }

  const toggleWishlist = (product) => {
    const existingItem = wishlistItems.value.find(item => item.id === product.id)
    
    if (existingItem) {
      return removeFromWishlist(product.id)
    } else {
      return addToWishlist(product)
    }
  }

  const isInWishlist = (productId) => {
    return wishlistItems.value.some(item => item.id === productId)
  }

  const clearWishlist = () => {
    wishlistItems.value = []
    saveWishlistToStorage()
  }

  const moveWishlistToCart = (productId) => {
    const item = wishlistItems.value.find(item => item.id === productId)
    
    if (item) {
      // Import cart store dynamically to avoid circular dependency
      import('./cart.js').then(({ useCartStore }) => {
        const cartStore = useCartStore()
        cartStore.addItem(item)
        removeFromWishlist(productId)
      })
    }
  }

  const moveAllWishlistToCart = () => {
    // Import cart store dynamically to avoid circular dependency
    import('./cart.js').then(({ useCartStore }) => {
      const cartStore = useCartStore()
      
      // Add all items to cart
      wishlistItems.value.forEach(item => {
        cartStore.addItem(item)
      })
      
      // Clear wishlist
      clearWishlist()
    })
  }

  // Wishlist Storage methods
  const saveWishlistToStorage = () => {
    localStorage.setItem('auro_wishlist', JSON.stringify(wishlistItems.value))
  }

  const loadWishlistFromStorage = () => {
    const saved = localStorage.getItem('auro_wishlist')
    if (saved) {
      try {
        wishlistItems.value = JSON.parse(saved)
      } catch (error) {
        console.error('Error loading wishlist from storage:', error)
        wishlistItems.value = []
      }
    }
  }

  // Initialize wishlist from localStorage when store is created
  loadWishlistFromStorage()

  // Clear all data
  const clearAll = () => {
    products.value = []
    featuredProducts.value = []
    categories.value = []
    currentProduct.value = null
    relatedProducts.value = []
    productReviews.value = []
    pagination.value = {
      page: 1,
      limit: 12,
      total: 0,
      totalPages: 0
    }
    clearError()
  }

  // Initialize
  loadWishlistFromStorage()

  return {
    // Product State
    products,
    featuredProducts,
    categories,
    currentProduct,
    relatedProducts,
    productReviews,
    isLoading,
    error,
    pagination,

    // Wishlist State
    wishlistItems,

    // Product Getters
    hasProducts,
    hasFeaturedProducts,
    hasCategories,
    productCount,
    featuredCount,
    categoryCount,

    // Wishlist Getters
    wishlistCount,
    isWishlistEmpty,
    wishlistItemIds,

    // Product Actions
    fetchProducts,
    fetchFeaturedProducts,
    fetchProductById,
    fetchProductsByCategory,
    searchProducts,
    fetchRelatedProducts,
    fetchProductReviews,
    addProductReview,
    fetchCategories,
    fetchCategoryById,
    getProductById,
    getCategoryById,
    getProductsByCategoryId,
    clearCurrentProduct,
    clearAll,
    clearError,

    // Wishlist Actions
    addToWishlist,
    removeFromWishlist,
    toggleWishlist,
    isInWishlist,
    clearWishlist,
    moveWishlistToCart,
    moveAllWishlistToCart,
    saveWishlistToStorage,
    loadWishlistFromStorage
  }
})
