import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useCartStore = defineStore('cart', () => {
  // State
  const items = ref(JSON.parse(localStorage.getItem('auro_cart')) || [])

  // Getters
  const itemCount = computed(() => {
    return items.value.reduce((total, item) => total + item.quantity, 0)
  })

  const totalPrice = computed(() => {
    return items.value.reduce((total, item) => total + (item.price * item.quantity), 0)
  })

  const isEmpty = computed(() => items.value.length === 0)

  // Actions
  const addItem = (product, quantity = 1) => {
    // Create unique key for variant-based products
    const itemKey = product.variantId || product.id
    const existingItem = items.value.find(item => 
      item.itemKey === itemKey || 
      (item.id === product.id && !item.variantId && !product.variantId)
    )
    
    const finalQuantity = product.quantity || quantity
    
    if (existingItem) {
      existingItem.quantity += finalQuantity
    } else {
      items.value.push({
        id: product.id,
        itemKey: itemKey,
        variantId: product.variantId || null,
        color: product.color || null,
        size: product.size || null,
        name: product.name,
        price: product.price,
        image: product.image,
        quantity: finalQuantity,
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
        item.quantity = quantity
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

  // Initialize cart from storage
  loadFromStorage()

  return {
    // State
    items,
    
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
    loadFromStorage
  }
})
