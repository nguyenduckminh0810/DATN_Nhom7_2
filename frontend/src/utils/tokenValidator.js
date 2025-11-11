/**
 * Token Validator Utility
 * Validates JWT token has proper authorities format
 */

export function validateToken(token) {
  if (!token) {
    return { valid: false, reason: 'No token' }
  }

  try {
    // Decode JWT payload
    const base64Url = token.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const payload = JSON.parse(window.atob(base64))

    

    // Check if authorities exist
    if (!payload.authorities) {
      return { valid: false, reason: 'No authorities in token' }
    }

    // Check if authorities is array
    if (!Array.isArray(payload.authorities)) {
      return { valid: false, reason: 'Authorities is not an array' }
    }

    // Check if authorities is not empty
    if (payload.authorities.length === 0) {
      return { valid: false, reason: 'Authorities array is empty' }
    }

    // Check if authorities have proper format
    const hasValidAuthority = payload.authorities.some(auth => {
      // Authority should be an object with "authority" property
      if (!auth || typeof auth !== 'object' || !auth.authority) return false

      const value = String(auth.authority).toUpperCase()
      const validWithRole = ['ROLE_CUS', 'ROLE_STF', 'ROLE_ADM', 'ROLE_GST']
      const validPlain    = ['CUS', 'STF', 'ADM', 'GST']
      
      if (!(validWithRole.includes(value) || validPlain.includes(value))) {
        return false
      }
      
      return true
    })

    if (!hasValidAuthority) {
      return { 
        valid: false, 
        reason: 'No valid ROLE_* authority found',
        authorities: payload.authorities
      }
    }

    // Token is valid
    return { 
      valid: true, 
      payload,
      authorities: payload.authorities
    }

  } catch (error) {
    
    return { valid: false, reason: 'Token decode error: ' + error.message }
  }
}

/**
 * Force clear invalid token and reload page
 */
export function clearInvalidToken(reason) {
  
  
  // Show user-friendly message
  const message = `Token không hợp lệ (${reason}). Vui lòng đăng nhập lại.`
  
  // Store message to show after reload
  sessionStorage.setItem('auth_clear_reason', message)
  
  // Clear all auth data
  localStorage.removeItem('auro_token')
  localStorage.removeItem('auro_user')
  localStorage.removeItem('auro_cart_v1')
  
  // Reload page to force re-initialization
  setTimeout(() => {
    window.location.href = '/'
  }, 100)
}

/**
 * Check and show auth clear reason if exists
 */
export function checkAuthClearReason() {
  const reason = sessionStorage.getItem('auth_clear_reason')
  if (reason) {
    sessionStorage.removeItem('auth_clear_reason')
    
    // Dispatch toast event
    window.dispatchEvent(new CustomEvent('show-toast', {
      detail: {
        type: 'warning',
        title: 'Cần đăng nhập lại',
        message: reason,
        duration: 5000
      }
    }))
  }
}
