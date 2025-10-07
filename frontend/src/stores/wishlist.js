import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useWishlistStore = defineStore('wishlist', () => {
  // State
  const items = ref([])

  // Getters
  const itemCount = computed(() => items.value.length)
  const isEmpty = computed(() => items.value.length === 0)
  const itemIds = computed(() => items.value.map(item => item.id))

  // Actions
  const addItem = (product) => {
    // Check if item already exists
    const existingItem = items.value.find(item => item.id === product.id)
    
    if (!existingItem) {
      items.value.push({
        ...product,
        addedAt: new Date().toISOString()
      })
      saveToStorage()
      
      // Show success toast
      if (window.$toast) {
        window.$toast.success(
          `${product.name} đã được thêm vào danh sách yêu thích`,
          'Thêm vào yêu thích'
        )
      }
      
      return true // Successfully added
    } else {
      // Show info toast
      if (window.$toast) {
        window.$toast.info(
          `${product.name} đã có trong danh sách yêu thích`,
          'Đã có trong yêu thích'
        )
      }
      
      return false // Already exists
    }
  }

  const removeItem = (productId) => {
    const index = items.value.findIndex(item => item.id === productId)
    
    if (index > -1) {
      const removedItem = items.value[index]
      items.value.splice(index, 1)
      saveToStorage()
      
      // Show success toast
      if (window.$toast) {
        window.$toast.success(
          `${removedItem.name} đã được xóa khỏi danh sách yêu thích`,
          'Xóa khỏi yêu thích'
        )
      }
      
      return true // Successfully removed
    }
    
    return false // Item not found
  }

  const toggleItem = (product) => {
    const existingItem = items.value.find(item => item.id === product.id)
    
    if (existingItem) {
      return removeItem(product.id)
    } else {
      return addItem(product)
    }
  }

  const isInWishlist = (productId) => {
    return items.value.some(item => item.id === productId)
  }

  const clearWishlist = () => {
    const count = items.value.length
    items.value = []
    saveToStorage()
    
    // Show success toast
    if (window.$toast) {
      window.$toast.success(
        `Đã xóa ${count} sản phẩm khỏi danh sách yêu thích`,
        'Xóa danh sách yêu thích'
      )
    }
  }

  const moveToCart = (productId) => {
    const item = items.value.find(item => item.id === productId)
    
    if (item) {
      // Import cart store dynamically to avoid circular dependency
      import('./cart.js').then(({ useCartStore }) => {
        const cartStore = useCartStore()
        cartStore.addItem(item)
        
        // Remove from wishlist
        removeItem(productId)
        
        // Show success toast
        if (window.$toast) {
          window.$toast.success(
            `${item.name} đã được chuyển vào giỏ hàng`,
            'Chuyển vào giỏ hàng'
          )
        }
      })
    }
  }

  const moveAllToCart = () => {
    // Import cart store dynamically to avoid circular dependency
    import('./cart.js').then(({ useCartStore }) => {
      const cartStore = useCartStore()
      const count = items.value.length
      
      // Add all items to cart
      items.value.forEach(item => {
        cartStore.addItem(item)
      })
      
      // Clear wishlist
      clearWishlist()
      
      // Show success toast
      if (window.$toast) {
        window.$toast.success(
          `${count} sản phẩm đã được chuyển vào giỏ hàng`,
          'Chuyển tất cả vào giỏ hàng'
        )
      }
    })
  }

  // Storage methods
  const saveToStorage = () => {
    localStorage.setItem('auro_wishlist', JSON.stringify(items.value))
  }

  const loadFromStorage = () => {
    const saved = localStorage.getItem('auro_wishlist')
    if (saved) {
      try {
        items.value = JSON.parse(saved)
      } catch (error) {
        console.error('Error loading wishlist from storage:', error)
        items.value = []
      }
    }
  }

  // Initialize
  loadFromStorage()

  return {
    // State
    items,

    // Getters
    itemCount,
    isEmpty,
    itemIds,

    // Actions
    addItem,
    removeItem,
    toggleItem,
    isInWishlist,
    clearWishlist,
    moveToCart,
    moveAllToCart,
    saveToStorage,
    loadFromStorage
  }
})
