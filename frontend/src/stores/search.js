import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useSearchStore = defineStore('search', () => {
  // State
  const searchQuery = ref('')
  const searchResults = ref([])
  const isSearching = ref(false)
  const searchHistory = ref([])
  const recentSearches = ref([])

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

  // Actions
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

  // Initialize
  loadSearchHistory()

  return {
    // State
    searchQuery,
    searchResults,
    isSearching,
    searchHistory,
    recentSearches,
    allProducts,

    // Getters
    hasResults,
    resultCount,
    popularSearches,

    // Actions
    searchProducts,
    addToSearchHistory,
    loadSearchHistory,
    clearSearchHistory,
    clearSearch,
    getSuggestions
  }
})
