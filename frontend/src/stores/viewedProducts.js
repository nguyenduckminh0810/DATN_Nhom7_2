import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useViewedProductsStore = defineStore('viewedProducts', () => {
  // State
  const viewedProducts = ref([])
  const maxViewedProducts = 12 // Giới hạn tối đa 12 sản phẩm đã xem

  // Getters
  const getViewedProducts = computed(() => viewedProducts.value)
  const getRecentViewedProducts = computed(() => 
    viewedProducts.value.slice(0, 8) // Chỉ hiển thị 8 sản phẩm gần nhất
  )

  // Actions
  const addViewedProduct = (product) => {
    if (!product || !product.id) return

    // Kiểm tra xem sản phẩm đã tồn tại chưa
    const existingIndex = viewedProducts.value.findIndex(p => p.id === product.id)
    
    if (existingIndex !== -1) {
      // Nếu đã tồn tại, xóa và thêm lại ở đầu (để sản phẩm mới xem lên đầu)
      viewedProducts.value.splice(existingIndex, 1)
    }
    
    // Thêm sản phẩm vào đầu danh sách
    viewedProducts.value.unshift({
      id: product.id,
      name: product.name,
      price: product.price,
      originalPrice: product.originalPrice,
      image: product.image || product.img,
      discount: product.discount,
      viewedAt: new Date().toISOString()
    })
    
    // Giới hạn số lượng sản phẩm đã xem
    if (viewedProducts.value.length > maxViewedProducts) {
      viewedProducts.value = viewedProducts.value.slice(0, maxViewedProducts)
    }
    
    // Lưu vào localStorage
    saveToLocalStorage()
  }

  const removeViewedProduct = (productId) => {
    const index = viewedProducts.value.findIndex(p => p.id === productId)
    if (index !== -1) {
      viewedProducts.value.splice(index, 1)
      saveToLocalStorage()
    }
  }

  const clearViewedProducts = () => {
    viewedProducts.value = []
    saveToLocalStorage()
  }

  const isProductViewed = (productId) => {
    return viewedProducts.value.some(p => p.id === productId)
  }

  // Local Storage functions
  const saveToLocalStorage = () => {
    try {
      localStorage.setItem('auro-viewed-products', JSON.stringify(viewedProducts.value))
    } catch (error) {
      console.warn('Could not save viewed products to localStorage:', error)
    }
  }

  const loadFromLocalStorage = () => {
    try {
      const stored = localStorage.getItem('auro-viewed-products')
      if (stored) {
        const parsed = JSON.parse(stored)
        viewedProducts.value = parsed || []
      }
    } catch (error) {
      console.warn('Could not load viewed products from localStorage:', error)
      viewedProducts.value = []
    }
  }

  // Initialize từ localStorage
  loadFromLocalStorage()

  return {
    // State
    viewedProducts,
    
    // Getters
    getViewedProducts,
    getRecentViewedProducts,
    
    // Actions
    addViewedProduct,
    removeViewedProduct,
    clearViewedProducts,
    isProductViewed,
    loadFromLocalStorage,
    saveToLocalStorage
  }
})
