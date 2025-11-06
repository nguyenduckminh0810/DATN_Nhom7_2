import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import cartService from '../services/cartService'

export const useCartStore = defineStore('cart', () => {
  // State
  const items = ref(JSON.parse(localStorage.getItem('auro_cart')) || [])
  const isLoading = ref(false)

  // Getters
  const itemCount = computed(() => {
    return items.value.reduce((total, item) => {
      const quantity = parseInt(item.quantity) || 0
      return total + quantity
    }, 0)
  })

  const totalPrice = computed(() => {
    return items.value.reduce((total, item) => {
      const price = parseFloat(item.price) || 0
      const quantity = parseInt(item.quantity) || 0
      return total + (price * quantity)
    }, 0)
  })

  const isEmpty = computed(() => items.value.length === 0)

  // Actions
  const addItem = (product, quantity = 1) => {
    // Validate and sanitize product data
    const sanitizedProduct = {
      id: product.id || null,
      name: product.name || 'Sản phẩm không tên',
      price: parseFloat(product.price) || 0,
      image: product.image || '',
      variantId: product.variantId || null,
      color: product.color || null,
      size: product.size || null,
      quantity: parseInt(product.quantity) || parseInt(quantity) || 1,
      stock: parseInt(product.stock) || 1
    }

    // Create unique key for variant-based products
    const itemKey = sanitizedProduct.variantId || sanitizedProduct.id
    const existingItem = items.value.find(item => 
      item.itemKey === itemKey || 
      (item.id === sanitizedProduct.id && !item.variantId && !sanitizedProduct.variantId)
    )
    
    if (existingItem) {
      existingItem.quantity += sanitizedProduct.quantity
      existingItem.price = sanitizedProduct.price // Update price in case it changed
    } else {
      items.value.push({
        id: sanitizedProduct.id,
        itemKey: itemKey,
        variantId: sanitizedProduct.variantId,
        color: sanitizedProduct.color,
        size: sanitizedProduct.size,
        name: sanitizedProduct.name,
        price: sanitizedProduct.price,
        image: sanitizedProduct.image,
        quantity: sanitizedProduct.quantity,
        addedAt: new Date().toISOString()
      })
    }
    
    saveToStorage()
  }

  const removeItem = (itemKey) => {
    const index = items.value.findIndex(item => item.itemKey === itemKey)
    if (index > -1) {
      items.value.splice(index, 1)
      saveToStorage()
    }
  }

  const updateQuantity = (itemKey, quantity) => {
    const item = items.value.find(item => item.itemKey === itemKey)
    if (item) {
      if (quantity <= 0) {
        removeItem(itemKey)
      } else {
        // Validate stock và quantity trước khi update
        const maxStock = item.stock || 1
        const safeQuantity = Math.max(1, Math.min(quantity, maxStock, 100)) // Giới hạn tối đa 100
        
        if (quantity !== safeQuantity) {
          console.warn(`Quantity ${quantity} invalid, using safe quantity ${safeQuantity}`)
        }
        
        item.quantity = safeQuantity
        saveToStorage()
      }
    }
  }

  const clearCart = () => {
    items.value = []
    saveToStorage()
  }

  const saveToStorage = () => {
    localStorage.setItem('auro_cart_v1', JSON.stringify(items.value))
  }

  const loadFromStorage = () => {
    const stored = localStorage.getItem('auro_cart_v1')
    if (stored) {
      try {
        items.value = JSON.parse(stored)
        // Ensure all items have itemKey for backward compatibility
        items.value.forEach(item => {
          if (!item.itemKey) {
            item.itemKey = item.variantId || item.id
          }
        })
      } catch (error) {
        console.error('Error loading cart from storage:', error)
        items.value = []
      }
    }
  }

  // Utility function to format price
  const formatPrice = (price) => {
    const numPrice = parseFloat(price) || 0
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
      minimumFractionDigits: 0
    }).format(numPrice)
  }

  // Load cart from backend API
  const loadCart = async () => {
    try {
      isLoading.value = true
      const response = await cartService.getCart()
      
      if (response && Array.isArray(response.chiTietList) && response.chiTietList.length > 0) {
        // Map backend response to cart items format (only when backend has items)
        items.value = response.chiTietList.map(item => ({
          id: item.id, // GioHangChiTiet ID
          itemKey: item.id,
          productId: item.productId || item.bienTheId,
          bienTheId: item.bienTheId,
          variantId: item.bienTheId,
          name: item.tenSanPham || 'Sản phẩm',
          price: parseFloat(item.donGia) || 0,
          quantity: parseInt(item.soLuong) || 1,
          image: item.image || '',
          color: item.color || extractColorFromThuocTinh(item.thuocTinh),
          size: item.size || extractSizeFromThuocTinh(item.thuocTinh),
          thuocTinh: item.thuocTinh || '',
          addedAt: new Date().toISOString()
        }))
        
        saveToStorage()
      } else {
        // Backend giỏ rỗng → giữ giỏ cục bộ (đừng ghi đè thành rỗng)
        loadFromStorage()
      }
      
      return response
    } catch (error) {
      console.error('❌ Error loading cart from backend:', error)
      // Fallback to localStorage if API fails
      loadFromStorage()
      throw error
    } finally {
      isLoading.value = false
    }
  }

  // Helper function to extract color from thuocTinh string
  const extractColorFromThuocTinh = (thuocTinh) => {
    if (!thuocTinh) return null
    const match = thuocTinh.match(/Màu:\s*([^,]+)/)
    return match ? match[1].trim() : null
  }

  // Helper function to extract size from thuocTinh string
  const extractSizeFromThuocTinh = (thuocTinh) => {
    if (!thuocTinh) return null
    const match = thuocTinh.match(/Size:\s*([^,]+)/)
    return match ? match[1].trim() : null
  }

  // Initialize cart from storage (fallback)
  loadFromStorage()

  return {
    // State
    items,
    isLoading,
    
    // Getters
    itemCount,
    totalPrice,
    isEmpty,
    
    // Actions
    addItem,
    removeItem,
    updateQuantity,
    clearCart,
    saveToStorage,
    loadFromStorage,
    loadCart,
    formatPrice
  }
})
