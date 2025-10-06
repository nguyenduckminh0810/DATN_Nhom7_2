/**
 * Authentication utilities for AURO
 * Handles user login state, token management, and authentication guards
 */

// Check if user is logged in
export const isLoggedIn = () => {
  const token = localStorage.getItem('auro_token')
  const user = localStorage.getItem('auro_user')
  return !!(token && user)
}

// Get current user data
export const getCurrentUser = () => {
  const userData = localStorage.getItem('auro_user')
  if (userData) {
    try {
      return JSON.parse(userData)
    } catch (error) {
      console.error('Error parsing user data:', error)
      return null
    }
  }
  return null
}

// Get user token
export const getUserToken = () => {
  return localStorage.getItem('auro_token')
}

// Set user session
export const setUserSession = (token, user) => {
  localStorage.setItem('auro_token', token)
  localStorage.setItem('auro_user', JSON.stringify(user))
}

// Clear user session
export const clearUserSession = () => {
  localStorage.removeItem('auro_token')
  localStorage.removeItem('auro_user')
}

// Voucher access control
export const canUseVoucher = () => {
  return isLoggedIn()
}

// Get voucher restriction message
export const getVoucherRestrictionMessage = () => {
  return {
    title: 'Cần đăng nhập để sử dụng voucher',
    message: 'Vui lòng đăng nhập tài khoản để áp dụng mã giảm giá và nhận nhiều ưu đãi khác.',
    action: 'Đăng nhập ngay'
  }
}

// Merge guest cart with user cart
export const mergeGuestCartWithUserCart = (guestCartItems, userCartItems = []) => {
  const mergedItems = [...userCartItems]
  
  guestCartItems.forEach(guestItem => {
    const existingItem = mergedItems.find(item => 
      item.itemKey === guestItem.itemKey ||
      (item.id === guestItem.id && !item.variantId && !guestItem.variantId)
    )
    
    if (existingItem) {
      existingItem.quantity += guestItem.quantity
    } else {
      mergedItems.push({
        ...guestItem,
        mergedFromGuest: true
      })
    }
  })
  
  return mergedItems
}

// Check if cart items were merged from guest
export const hasGuestCartItems = (cartItems) => {
  return cartItems.some(item => item.mergedFromGuest)
}

// Authentication guard for voucher operations
export const requireLoginForVoucher = (callback) => {
  if (isLoggedIn()) {
    return callback()
  } else {
    // Show login requirement message
    if (window.$toast) {
      const restriction = getVoucherRestrictionMessage()
      window.$toast.warning(restriction.message, restriction.title)
    }
    return false
  }
}

// Check user permissions
export const hasPermission = (permission) => {
  const user = getCurrentUser()
  if (!user || !user.permissions) return false
  return user.permissions.includes(permission)
}

// Check if user is admin
export const isAdmin = () => {
  const user = getCurrentUser()
  return user && user.role === 'admin'
}

// Check if user is premium
export const isPremium = () => {
  const user = getCurrentUser()
  return user && (user.role === 'premium' || user.isPremium)
}

// Session expiration check
export const isSessionExpired = () => {
  const token = getUserToken()
  if (!token) return true
  
  try {
    // Decode JWT token (basic implementation)
    const payload = JSON.parse(atob(token.split('.')[1]))
    const currentTime = Date.now() / 1000
    return payload.exp < currentTime
  } catch (error) {
    console.error('Error checking token expiration:', error)
    return true
  }
}

// Auto logout on session expiration
export const checkSessionAndLogout = () => {
  if (isSessionExpired()) {
    clearUserSession()
    if (window.$toast) {
      window.$toast.warning('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.', 'Phiên hết hạn')
    }
    return true
  }
  return false
}

// Login redirect with return URL
export const getLoginRedirectUrl = (returnUrl = null) => {
  const currentPath = window.location.pathname + window.location.search
  const targetUrl = returnUrl || currentPath
  return `/login?redirect=${encodeURIComponent(targetUrl)}`
}

// Voucher validation helpers
export const validateVoucherForUser = (voucher, user) => {
  if (!user) {
    return {
      valid: false,
      message: 'Cần đăng nhập để sử dụng voucher'
    }
  }
  
  // Add more validation logic here
  // - Check voucher availability
  // - Check user eligibility
  // - Check minimum order amount
  // - Check usage limits
  
  return {
    valid: true,
    message: 'Voucher hợp lệ'
  }
}

// Export all functions as default object
export default {
  isLoggedIn,
  getCurrentUser,
  getUserToken,
  setUserSession,
  clearUserSession,
  canUseVoucher,
  getVoucherRestrictionMessage,
  mergeGuestCartWithUserCart,
  hasGuestCartItems,
  requireLoginForVoucher,
  hasPermission,
  isAdmin,
  isPremium,
  isSessionExpired,
  checkSessionAndLogout,
  getLoginRedirectUrl,
  validateVoucherForUser
}
