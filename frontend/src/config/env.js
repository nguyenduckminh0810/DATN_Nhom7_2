// Environment configuration
export const config = {
  // API Configuration
  api: {
    baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
    timeout: parseInt(import.meta.env.VITE_API_TIMEOUT) || 10000,
    retryAttempts: 3,
    retryDelay: 1000
  },

  // App Configuration
  app: {
    name: import.meta.env.VITE_APP_NAME || 'AURO',
    version: import.meta.env.VITE_APP_VERSION || '1.0.0',
    description: import.meta.env.VITE_APP_DESCRIPTION || 'Thương hiệu thời trang nam cao cấp',
    environment: import.meta.env.VITE_NODE_ENV || 'development'
  },

  // Features
  features: {
    analytics: import.meta.env.VITE_ENABLE_ANALYTICS === 'true',
    errorTracking: import.meta.env.VITE_ENABLE_ERROR_TRACKING === 'true',
    cache: import.meta.env.VITE_ENABLE_CACHE === 'true'
  },

  // External Services
  services: {
    googleAnalytics: {
      id: import.meta.env.VITE_GOOGLE_ANALYTICS_ID || ''
    },
    sentry: {
      dsn: import.meta.env.VITE_SENTRY_DSN || ''
    }
  },

  // Payment
  payment: {
    gatewayUrl: import.meta.env.VITE_PAYMENT_GATEWAY_URL || '',
    publicKey: import.meta.env.VITE_PAYMENT_PUBLIC_KEY || ''
  },

  // File Upload
  upload: {
    maxFileSize: parseInt(import.meta.env.VITE_MAX_FILE_SIZE) || 5242880, // 5MB
    allowedTypes: (import.meta.env.VITE_ALLOWED_FILE_TYPES || 'image/jpeg,image/png,image/webp').split(','),
    maxFiles: 10
  },

  // Pagination
  pagination: {
    defaultPageSize: parseInt(import.meta.env.VITE_DEFAULT_PAGE_SIZE) || 12,
    maxPageSize: parseInt(import.meta.env.VITE_MAX_PAGE_SIZE) || 50
  },

  // Cache
  cache: {
    duration: parseInt(import.meta.env.VITE_CACHE_DURATION) || 300000, // 5 minutes
    enabled: import.meta.env.VITE_ENABLE_CACHE === 'true'
  },

  // Development
  isDevelopment: import.meta.env.DEV,
  isProduction: import.meta.env.PROD,

  // URLs
  urls: {
    base: import.meta.env.BASE_URL || '/',
    api: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
    assets: import.meta.env.VITE_ASSETS_URL || '/assets'
  }
}

// Validate required environment variables
export const validateConfig = () => {
  const required = [
    'VITE_API_URL'
  ]

  const missing = required.filter(key => !import.meta.env[key])
  
  if (missing.length > 0) {
    console.warn('Missing required environment variables:', missing)
  }

  return missing.length === 0
}

// Export default config
export default config
