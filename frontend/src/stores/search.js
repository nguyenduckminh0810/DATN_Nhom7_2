import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../services/api'

export const useSearchStore = defineStore('search', () => {
  // Search State
  const searchQuery = ref('')
  const searchResults = ref([])
  const isSearching = ref(false)
  const searchHistory = ref([])
  const recentSearches = ref([])

  // Filter State
  const activeFilters = ref({
    priceRange: { min: 0, max: 5000000 },
    sizes: [],
    colors: [],
    materials: [],
    categories: [],
    sortBy: 'relevance',
    availability: 'all' // all, inStock, outOfStock
  })

  const isFilterActive = ref(false)

  // Available filter options
  const availableSizes = ref([])
  const isLoadingSizes = ref(false)

  const availableColors = ref([
    { name: 'Đen', value: 'black', hex: '#000000' },
    { name: 'Trắng', value: 'white', hex: '#FFFFFF' },
    { name: 'Xám', value: 'gray', hex: '#808080' },
    { name: 'Xanh navy', value: 'navy', hex: '#000080' },
    { name: 'Xanh dương', value: 'blue', hex: '#0066CC' },
    { name: 'Xanh lá', value: 'green', hex: '#008000' },
    { name: 'Đỏ', value: 'red', hex: '#FF0000' },
    { name: 'Nâu', value: 'brown', hex: '#8B4513' },
    { name: 'Be', value: 'beige', hex: '#F5F5DC' },
    { name: 'Kem', value: 'cream', hex: '#FFFDD0' }
  ])

  const availableMaterials = ref([
    'Cotton',
    'Polyester',
    'Denim',
    'Linen',
    'Wool',
    'Silk',
    'Leather',
    'Synthetic',
    'Blend'
  ])

  const priceRanges = ref([
    { label: 'Dưới 200k', min: 0, max: 200000 },
    { label: '200k - 500k', min: 200000, max: 500000 },
    { label: '500k - 1M', min: 500000, max: 1000000 },
    { label: '1M - 2M', min: 1000000, max: 2000000 },
    { label: 'Trên 2M', min: 2000000, max: 5000000 }
  ])

  // Mock products data for search
  const allProducts = ref([
    {
      id: 1,
      name: 'Áo sơ mi nam cao cấp',
      description: 'Áo sơ mi nam chất liệu cotton 100%',
      price: 450000,
      originalPrice: 600000,
      discount: 25,
      image: 'https://images.unsplash.com/photo-1594938298605-cd64d190e6bc?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
      category: 'ao',
      tags: ['áo sơ mi', 'cotton', 'nam', 'cao cấp', 'formal']
    },
    {
      id: 2,
      name: 'Quần âu nam',
      description: 'Quần âu nam thiết kế hiện đại',
      price: 650000,
      originalPrice: 800000,
      discount: 19,
      image: 'https://images.unsplash.com/photo-1506629905607-1a5a1b1b1b1b?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
      category: 'quan',
      tags: ['quần âu', 'nam', 'hiện đại', 'formal']
    },
    {
      id: 3,
      name: 'Áo khoác nam',
      description: 'Áo khoác nam phong cách casual',
      price: 850000,
      originalPrice: 1200000,
      discount: 29,
      image: 'https://images.unsplash.com/photo-1551028719-00167b16eac5?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
      category: 'ao',
      tags: ['áo khoác', 'casual', 'nam', 'thời trang']
    },
    {
      id: 4,
      name: 'Áo thun nam',
      description: 'Áo thun nam chất liệu cotton mềm mại',
      price: 250000,
      originalPrice: 350000,
      discount: 29,
      image: 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
      category: 'ao',
      tags: ['áo thun', 'cotton', 'nam', 'casual', 'mềm mại']
    },
    {
      id: 5,
      name: 'Quần jean nam',
      description: 'Quần jean nam phong cách trẻ trung',
      price: 550000,
      originalPrice: 750000,
      discount: 27,
      image: 'https://images.unsplash.com/photo-1542272604-787c3835535d?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
      category: 'quan',
      tags: ['quần jean', 'denim', 'nam', 'trẻ trung', 'casual']
    },
    {
      id: 6,
      name: 'Áo polo nam',
      description: 'Áo polo nam chất liệu cao cấp',
      price: 380000,
      originalPrice: 500000,
      discount: 24,
      image: 'https://images.unsplash.com/photo-1586790170083-2f9ceadc732d?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
      category: 'ao',
      tags: ['áo polo', 'nam', 'cao cấp', 'casual', 'sport']
    },
    {
      id: 7,
      name: 'Quần short nam',
      description: 'Quần short nam thoải mái cho mùa hè',
      price: 320000,
      originalPrice: 420000,
      discount: 24,
      image: 'https://images.unsplash.com/photo-1594633312681-425c7b97ccd1?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
      category: 'quan',
      tags: ['quần short', 'nam', 'mùa hè', 'thoải mái', 'casual']
    },
    {
      id: 8,
      name: 'Áo hoodie nam',
      description: 'Áo hoodie nam phong cách streetwear',
      price: 680000,
      originalPrice: 850000,
      discount: 20,
      image: 'https://images.unsplash.com/photo-1556821840-3a63f95609a7?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
      category: 'ao',
      tags: ['áo hoodie', 'streetwear', 'nam', 'thời trang', 'casual']
    }
  ])

  // Getters
  const hasResults = computed(() => searchResults.value.length > 0)
  const resultCount = computed(() => searchResults.value.length)
  const popularSearches = computed(() => [
    'áo sơ mi', 'quần âu', 'áo khoác', 'áo thun', 'quần jean'
  ])

  // Filter Getters
  const hasActiveFilters = computed(() => {
    return (
      activeFilters.value.sizes.length > 0 ||
      activeFilters.value.colors.length > 0 ||
      activeFilters.value.materials.length > 0 ||
      activeFilters.value.categories.length > 0 ||
      activeFilters.value.priceRange.min > 0 ||
      activeFilters.value.priceRange.max < 5000000 ||
      activeFilters.value.availability !== 'all'
    )
  })

  const activeFiltersCount = computed(() => {
    let count = 0
    if (activeFilters.value.sizes.length > 0) count++
    if (activeFilters.value.colors.length > 0) count++
    if (activeFilters.value.materials.length > 0) count++
    if (activeFilters.value.categories.length > 0) count++
    if (activeFilters.value.priceRange.min > 0 || activeFilters.value.priceRange.max < 5000000) count++
    if (activeFilters.value.availability !== 'all') count++
    return count
  })

  // Actions
  /**
   * Load available sizes from database
   */
  const loadAvailableSizes = async () => {
    if (isLoadingSizes.value) return // Prevent duplicate calls
    
    try {
      isLoadingSizes.value = true
      console.log('🔍 Loading sizes from database...')
      
      // api.get() already returns response.data, so we get the data directly
      // Note: api.js baseURL is already '/api', so we just need '/kich-co/ten'
      const data = await api.get('/kich-co/ten')
      
      console.log('🔍 Received data from API:', data)
      
      if (data && Array.isArray(data)) {
        availableSizes.value = data
        console.log('✅ Loaded sizes from DB:', availableSizes.value)
      } else if (Array.isArray(data.data)) {
        // Fallback if API returns nested data
        availableSizes.value = data.data
        console.log('✅ Loaded sizes from DB (nested):', availableSizes.value)
      } else {
        console.warn('⚠️ Invalid size data, using fallback. Received:', data)
        availableSizes.value = ['S', 'M', 'L', 'XL', '2XL']
      }
    } catch (error) {
      console.error('❌ Error loading sizes:', error)
      console.error('❌ Error details:', error.message, error.status, error.type)
      // Fallback to default sizes
      availableSizes.value = ['S', 'M', 'L', 'XL', '2XL']
    } finally {
      isLoadingSizes.value = false
    }
  }

  const searchProducts = async (query) => {
    if (!query.trim()) {
      searchResults.value = []
      return
    }

    isSearching.value = true
    searchQuery.value = query

    // Simulate API delay
    await new Promise(resolve => setTimeout(resolve, 500))

    // Search logic
    const results = allProducts.value.filter(product => {
      const searchTerm = query.toLowerCase()
      return (
        product.name.toLowerCase().includes(searchTerm) ||
        product.description.toLowerCase().includes(searchTerm) ||
        product.tags.some(tag => tag.toLowerCase().includes(searchTerm))
      )
    })

    searchResults.value = results
    isSearching.value = false

    // Add to search history
    addToSearchHistory(query)
  }

  const addToSearchHistory = (query) => {
    if (!query.trim()) return

    // Remove if already exists
    const existingIndex = searchHistory.value.findIndex(item => 
      item.toLowerCase() === query.toLowerCase()
    )
    
    if (existingIndex > -1) {
      searchHistory.value.splice(existingIndex, 1)
    }

    // Add to beginning
    searchHistory.value.unshift(query)

    // Keep only last 10 searches
    if (searchHistory.value.length > 10) {
      searchHistory.value = searchHistory.value.slice(0, 10)
    }

    // Save to localStorage
    localStorage.setItem('auro_search_history', JSON.stringify(searchHistory.value))
  }

  const loadSearchHistory = () => {
    const saved = localStorage.getItem('auro_search_history')
    if (saved) {
      try {
        searchHistory.value = JSON.parse(saved)
      } catch (error) {
        console.error('Error loading search history:', error)
      }
    }
  }

  const clearSearchHistory = () => {
    searchHistory.value = []
    localStorage.removeItem('auro_search_history')
  }

  const clearSearch = () => {
    searchQuery.value = ''
    searchResults.value = []
  }

  const getSuggestions = (query) => {
    if (!query.trim()) return popularSearches.value

    const suggestions = []
    const searchTerm = query.toLowerCase()

    // Search in product names and tags
    allProducts.value.forEach(product => {
      if (product.name.toLowerCase().includes(searchTerm)) {
        suggestions.push(product.name)
      }
      product.tags.forEach(tag => {
        if (tag.toLowerCase().includes(searchTerm) && !suggestions.includes(tag)) {
          suggestions.push(tag)
        }
      })
    })

    // Search in history
    searchHistory.value.forEach(item => {
      if (item.toLowerCase().includes(searchTerm) && !suggestions.includes(item)) {
        suggestions.push(item)
      }
    })

    return suggestions.slice(0, 5) // Limit to 5 suggestions
  }

  // Filter Actions
  const setPriceRange = (min, max) => {
    activeFilters.value.priceRange = { min, max }
    updateFilterStatus()
  }

  const toggleSize = (size) => {
    const index = activeFilters.value.sizes.indexOf(size)
    if (index > -1) {
      activeFilters.value.sizes.splice(index, 1)
    } else {
      activeFilters.value.sizes.push(size)
    }
    updateFilterStatus()
  }

  const toggleColor = (color) => {
    const index = activeFilters.value.colors.indexOf(color)
    if (index > -1) {
      activeFilters.value.colors.splice(index, 1)
    } else {
      activeFilters.value.colors.push(color)
    }
    updateFilterStatus()
  }

  const toggleMaterial = (material) => {
    const index = activeFilters.value.materials.indexOf(material)
    if (index > -1) {
      activeFilters.value.materials.splice(index, 1)
    } else {
      activeFilters.value.materials.push(material)
    }
    updateFilterStatus()
  }

  const toggleCategory = (category) => {
    const index = activeFilters.value.categories.indexOf(category)
    if (index > -1) {
      activeFilters.value.categories.splice(index, 1)
    } else {
      activeFilters.value.categories.push(category)
    }
    updateFilterStatus()
  }

  const setSortBy = (sortBy) => {
    activeFilters.value.sortBy = sortBy
  }

  const setAvailability = (availability) => {
    activeFilters.value.availability = availability
    updateFilterStatus()
  }

  const updateFilterStatus = () => {
    isFilterActive.value = hasActiveFilters.value
  }

  const clearAllFilters = () => {
    activeFilters.value = {
      priceRange: { min: 0, max: 5000000 },
      sizes: [],
      colors: [],
      materials: [],
      categories: [],
      sortBy: 'relevance',
      availability: 'all'
    }
    isFilterActive.value = false
  }

  const clearFilter = (filterType) => {
    switch (filterType) {
      case 'price':
        activeFilters.value.priceRange = { min: 0, max: 5000000 }
        break
      case 'sizes':
        activeFilters.value.sizes = []
        break
      case 'colors':
        activeFilters.value.colors = []
        break
      case 'materials':
        activeFilters.value.materials = []
        break
      case 'categories':
        activeFilters.value.categories = []
        break
      case 'availability':
        activeFilters.value.availability = 'all'
        break
    }
    updateFilterStatus()
  }

  // Filter products based on active filters
  const filterProducts = (products) => {
    if (!hasActiveFilters.value) {
      return products
    }

    return products.filter(product => {
      // Price filter
      if (product.price < activeFilters.value.priceRange.min || 
          product.price > activeFilters.value.priceRange.max) {
        return false
      }

      // Size filter
      if (activeFilters.value.sizes.length > 0) {
        const productSizes = product.availableSizes || ['M', 'L', 'XL']
        const hasMatchingSize = activeFilters.value.sizes.some(size => 
          productSizes.includes(size)
        )
        if (!hasMatchingSize) return false
      }

      // Color filter
      if (activeFilters.value.colors.length > 0) {
        const productColors = product.availableColors || ['black', 'white']
        const hasMatchingColor = activeFilters.value.colors.some(color => 
          productColors.includes(color)
        )
        if (!hasMatchingColor) return false
      }

      // Material filter
      if (activeFilters.value.materials.length > 0) {
        const productMaterials = product.materials || ['Cotton']
        const hasMatchingMaterial = activeFilters.value.materials.some(material => 
          productMaterials.includes(material)
        )
        if (!hasMatchingMaterial) return false
      }

      // Category filter
      if (activeFilters.value.categories.length > 0) {
        if (!activeFilters.value.categories.includes(product.category)) {
          return false
        }
      }

      // Availability filter
      if (activeFilters.value.availability !== 'all') {
        const inStock = product.stock > 0
        if (activeFilters.value.availability === 'inStock' && !inStock) {
          return false
        }
        if (activeFilters.value.availability === 'outOfStock' && inStock) {
          return false
        }
      }

      return true
    })
  }

  // Sort products based on active sort option
  const sortProducts = (products) => {
    const sortedProducts = [...products]
    
    switch (activeFilters.value.sortBy) {
      case 'price-asc':
        return sortedProducts.sort((a, b) => a.price - b.price)
      case 'price-desc':
        return sortedProducts.sort((a, b) => b.price - a.price)
      case 'name-asc':
        return sortedProducts.sort((a, b) => a.name.localeCompare(b.name))
      case 'name-desc':
        return sortedProducts.sort((a, b) => b.name.localeCompare(a.name))
      case 'discount-desc':
        return sortedProducts.sort((a, b) => (b.discount || 0) - (a.discount || 0))
      case 'newest':
        return sortedProducts.sort((a, b) => new Date(b.createdAt || Date.now()) - new Date(a.createdAt || Date.now()))
      case 'popular':
        return sortedProducts.sort((a, b) => (b.sales || 0) - (a.sales || 0))
      default:
        return sortedProducts // relevance - keep original order
    }
  }

  // Apply all filters and sorting
  const applyFilters = (products) => {
    const filtered = filterProducts(products)
    return sortProducts(filtered)
  }

  // Save filters to localStorage
  const saveFilters = () => {
    localStorage.setItem('auro_active_filters', JSON.stringify(activeFilters.value))
  }

  // Load filters from localStorage
  const loadFilters = () => {
    const saved = localStorage.getItem('auro_active_filters')
    if (saved) {
      try {
        activeFilters.value = { ...activeFilters.value, ...JSON.parse(saved) }
        updateFilterStatus()
      } catch (error) {
        console.error('Error loading filters:', error)
      }
    }
  }

  // Initialize
  loadSearchHistory()
  loadFilters()
  loadAvailableSizes() // Load sizes from database
  
  // Clear old filter data if needed (for migration)
  const clearOldFilterData = () => {
    const saved = localStorage.getItem('auro_active_filters')
    if (saved) {
      try {
        const parsed = JSON.parse(saved)
        if (parsed.brands && !parsed.materials) {
          // Old structure detected, clear it
          localStorage.removeItem('auro_active_filters')
          console.log('Cleared old filter data')
        }
      } catch (error) {
        // Invalid data, clear it
        localStorage.removeItem('auro_active_filters')
        console.log('Cleared invalid filter data')
      }
    }
  }
  
  clearOldFilterData()

  return {
    // Search State
    searchQuery,
    searchResults,
    isSearching,
    searchHistory,
    recentSearches,
    allProducts,

    // Filter State
    activeFilters,
    isFilterActive,
    availableSizes,
    isLoadingSizes,
    availableColors,
    availableMaterials,
    priceRanges,

    // Search Getters
    hasResults,
    resultCount,
    popularSearches,

    // Filter Getters
    hasActiveFilters,
    activeFiltersCount,

    // Search Actions
    loadAvailableSizes,
    searchProducts,
    addToSearchHistory,
    loadSearchHistory,
    clearSearchHistory,
    clearSearch,
    getSuggestions,

    // Filter Actions
    setPriceRange,
    toggleSize,
    toggleColor,
    toggleMaterial,
    toggleCategory,
    setSortBy,
    setAvailability,
    clearAllFilters,
    clearFilter,
    filterProducts,
    sortProducts,
    applyFilters,
    saveFilters,
    loadFilters
  }
})
