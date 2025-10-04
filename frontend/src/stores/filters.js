import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useFiltersStore = defineStore('filters', () => {
  // State
  const activeFilters = ref({
    priceRange: { min: 0, max: 5000000 },
    sizes: [],
    colors: [],
    brands: [],
    categories: [],
    sortBy: 'relevance',
    availability: 'all' // all, inStock, outOfStock
  })

  const isFilterActive = ref(false)

  // Available filter options
  const availableSizes = ref([
    'XS', 'S', 'M', 'L', 'XL', 'XXL', 'XXXL',
    '28', '29', '30', '31', '32', '33', '34', '35', '36', '37', '38', '39', '40'
  ])

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

  const availableBrands = ref([
    'AURO Premium',
    'AURO Classic',
    'AURO Sport',
    'AURO Formal',
    'AURO Casual'
  ])

  const priceRanges = ref([
    { label: 'Dưới 200k', min: 0, max: 200000 },
    { label: '200k - 500k', min: 200000, max: 500000 },
    { label: '500k - 1M', min: 500000, max: 1000000 },
    { label: '1M - 2M', min: 1000000, max: 2000000 },
    { label: 'Trên 2M', min: 2000000, max: 5000000 }
  ])

  // Getters
  const hasActiveFilters = computed(() => {
    return (
      activeFilters.value.sizes.length > 0 ||
      activeFilters.value.colors.length > 0 ||
      activeFilters.value.brands.length > 0 ||
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
    if (activeFilters.value.brands.length > 0) count++
    if (activeFilters.value.categories.length > 0) count++
    if (activeFilters.value.priceRange.min > 0 || activeFilters.value.priceRange.max < 5000000) count++
    if (activeFilters.value.availability !== 'all') count++
    return count
  })

  // Actions
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

  const toggleBrand = (brand) => {
    const index = activeFilters.value.brands.indexOf(brand)
    if (index > -1) {
      activeFilters.value.brands.splice(index, 1)
    } else {
      activeFilters.value.brands.push(brand)
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
      brands: [],
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
      case 'brands':
        activeFilters.value.brands = []
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

      // Brand filter
      if (activeFilters.value.brands.length > 0) {
        const productBrand = product.brand || 'AURO Premium'
        if (!activeFilters.value.brands.includes(productBrand)) {
          return false
        }
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
  loadFilters()

  return {
    // State
    activeFilters,
    isFilterActive,
    availableSizes,
    availableColors,
    availableBrands,
    priceRanges,

    // Getters
    hasActiveFilters,
    activeFiltersCount,

    // Actions
    setPriceRange,
    toggleSize,
    toggleColor,
    toggleBrand,
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
