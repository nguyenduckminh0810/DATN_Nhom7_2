/**
 * Shared Color Utility for Product Variants
 * Chuẩn hóa màu sắc giữa ProductCard, ProductDetail và VariantModal
 */

// Color name to hex code mapping
export const COLOR_MAP = {
  'Đen': '#000000',
  'Trắng': '#ffffff',
  'Xám': '#808080',
  'Xanh navy': '#000080',
  'Xanh dương': '#007bff',
  'Đỏ': '#dc3545',
  'Đỏ đậm': '#dc143c',
  'Xanh lá': '#28a745',
  'Xanh rừng': '#228b22',
  'Nâu': '#8b4513',
  'Hồng': '#ff69b4',
  'Vàng': '#ffc107',
  'Cam': '#fd7e14',
  'Tím': '#6f42c1',
  'Be': '#f5f5dc'
}

// Hex code to color name mapping (reverse)
export const HEX_TO_NAME = Object.fromEntries(
  Object.entries(COLOR_MAP).map(([name, hex]) => [hex, name])
)

/**
 * Get color name from hex code
 * @param {string} hexCode - Hex color code (e.g., '#ffffff')
 * @returns {string} Color name in Vietnamese
 */
export const getColorName = (hexCode) => {
  return HEX_TO_NAME[hexCode?.toLowerCase()] || hexCode || 'Không xác định'
}

/**
 * Get hex code from color name
 * @param {string} colorName - Color name in Vietnamese
 * @returns {string} Hex color code
 */
export const getColorHex = (colorName) => {
  return COLOR_MAP[colorName] || '#cccccc'
}

/**
 * Check if color is light (for text color contrast)
 * @param {string} hexCode - Hex color code
 * @returns {boolean} True if light color
 */
export const isLightColor = (hexCode) => {
  if (!hexCode) return false
  
  // Remove # if present
  const hex = hexCode.replace('#', '')
  
  // Convert to RGB
  const r = parseInt(hex.substr(0, 2), 16)
  const g = parseInt(hex.substr(2, 2), 16)
  const b = parseInt(hex.substr(4, 2), 16)
  
  // Calculate luminance
  const luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255
  
  return luminance > 0.5
}

/**
 * Get contrasting text color (black or white) for background
 * @param {string} hexCode - Background hex color code
 * @returns {string} '#000000' or '#ffffff'
 */
export const getContrastColor = (hexCode) => {
  return isLightColor(hexCode) ? '#000000' : '#ffffff'
}

/**
 * Convert color name array to hex array
 * @param {string[]} colorNames - Array of color names
 * @returns {string[]} Array of hex codes
 */
export const colorNamesToHex = (colorNames) => {
  return colorNames.map(name => getColorHex(name))
}

/**
 * Convert hex array to color name array
 * @param {string[]} hexCodes - Array of hex codes
 * @returns {string[]} Array of color names
 */
export const hexToColorNames = (hexCodes) => {
  return hexCodes.map(hex => getColorName(hex))
}

/**
 * Standard sizes in order
 */
export const STANDARD_SIZES = ['XS', 'S', 'M', 'L', 'XL', 'XXL', '3XL']

/**
 * Sort sizes in standard order
 * @param {string[]} sizes - Array of size strings
 * @returns {string[]} Sorted array
 */
export const sortSizes = (sizes) => {
  return sizes.sort((a, b) => {
    const indexA = STANDARD_SIZES.indexOf(a)
    const indexB = STANDARD_SIZES.indexOf(b)
    
    // If both sizes are in standard list, sort by index
    if (indexA !== -1 && indexB !== -1) {
      return indexA - indexB
    }
    
    // If only one is in standard list, prioritize it
    if (indexA !== -1) return -1
    if (indexB !== -1) return 1
    
    // Otherwise, alphabetical
    return a.localeCompare(b)
  })
}

/**
 * Build color-size mapping from variants array
 * Converts from API format to component format
 * @param {Array} variants - Array of variant objects with color, size, stock
 * @returns {Object} colorSizeMapping object
 * 
 * Example input:
 * [
 *   { color: 'Trắng', size: 'M', stock: 5 },
 *   { color: 'Trắng', size: 'L', stock: 3 },
 *   { color: 'Đen', size: 'M', stock: 2 }
 * ]
 * 
 * Example output:
 * {
 *   '#ffffff': ['M', 'L'],
 *   '#000000': ['M']
 * }
 */
export const buildColorSizeMapping = (variants) => {
  if (!variants || !Array.isArray(variants)) return {}
  
  const mapping = {}
  
  variants.forEach(variant => {
    // Skip variants with no stock
    if (!variant.stock || variant.stock <= 0) return
    
    // Get hex code from color name or use as-is if already hex
    const hexCode = variant.color?.startsWith('#') 
      ? variant.color 
      : getColorHex(variant.color)
    
    // Initialize array if not exists
    if (!mapping[hexCode]) {
      mapping[hexCode] = []
    }
    
    // Add size if not already in array
    if (!mapping[hexCode].includes(variant.size)) {
      mapping[hexCode].push(variant.size)
    }
  })
  
  // Sort sizes in each color
  Object.keys(mapping).forEach(color => {
    mapping[color] = sortSizes(mapping[color])
  })
  
  return mapping
}

/**
 * Get available colors from variants
 * @param {Array} variants - Array of variant objects
 * @returns {string[]} Array of hex color codes with stock
 */
export const getAvailableColors = (variants) => {
  if (!variants || !Array.isArray(variants)) return []
  
  const colors = new Set()
  
  variants.forEach(variant => {
    if (variant.stock && variant.stock > 0) {
      const hexCode = variant.color?.startsWith('#')
        ? variant.color
        : getColorHex(variant.color)
      colors.add(hexCode)
    }
  })
  
  return Array.from(colors)
}

/**
 * Get available sizes from variants
 * @param {Array} variants - Array of variant objects
 * @returns {string[]} Sorted array of sizes with stock
 */
export const getAvailableSizes = (variants) => {
  if (!variants || !Array.isArray(variants)) return []
  
  const sizes = new Set()
  
  variants.forEach(variant => {
    if (variant.stock && variant.stock > 0) {
      sizes.add(variant.size)
    }
  })
  
  return sortSizes(Array.from(sizes))
}

/**
 * Get stock for specific variant
 * @param {Array} variants - Array of variant objects
 * @param {string} color - Color (name or hex)
 * @param {string} size - Size
 * @returns {number} Stock quantity
 */
export const getVariantStock = (variants, color, size) => {
  if (!variants || !Array.isArray(variants) || !color || !size) return 0
  
  // Normalize color to hex
  const hexColor = color.startsWith('#') ? color : getColorHex(color)
  
  const variant = variants.find(v => {
    const vColor = v.color?.startsWith('#') ? v.color : getColorHex(v.color)
    return vColor === hexColor && v.size === size
  })
  
  return variant?.stock || 0
}

/**
 * Get total stock for all variants
 * @param {Array} variants - Array of variant objects
 * @returns {number} Total stock
 */
export const getTotalStock = (variants) => {
  if (!variants || !Array.isArray(variants)) return 0
  
  return variants.reduce((total, variant) => {
    return total + (variant.stock || 0)
  }, 0)
}

/**
 * Check if variant combination is available
 * @param {Array} variants - Array of variant objects
 * @param {string} color - Color (name or hex)
 * @param {string} size - Size
 * @returns {boolean} True if variant has stock
 */
export const isVariantAvailable = (variants, color, size) => {
  return getVariantStock(variants, color, size) > 0
}

