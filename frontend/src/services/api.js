import axios from 'axios'

class ApiService {
  constructor() {
    this.baseURL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'
    this.client = axios.create({
      baseURL: this.baseURL,
      timeout: 10000,
      headers: {
        'Content-Type': 'application/json'
      }
    })
    this.setupInterceptors()
  }

  setupInterceptors() {
    // Request interceptor
    this.client.interceptors.request.use(
      (config) => {
        const token = localStorage.getItem('auro_token')
        if (token) {
          config.headers.Authorization = `Bearer ${token}`
        }
        return config
      },
      (error) => Promise.reject(error)
    )

    // Response interceptor
    this.client.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          localStorage.removeItem('auro_token')
          localStorage.removeItem('auro_user')
          window.location.href = '/'
        }
        return Promise.reject(error)
      }
    )
  }

  // Generic HTTP methods
  async get(url, config = {}) {
    try {
      const response = await this.client.get(url, config)
      return response.data
    } catch (error) {
      throw this.handleError(error)
    }
  }

  async post(url, data = {}, config = {}) {
    try {
      const response = await this.client.post(url, data, config)
      return response.data
    } catch (error) {
      throw this.handleError(error)
    }
  }

  async put(url, data = {}, config = {}) {
    try {
      const response = await this.client.put(url, data, config)
      return response.data
    } catch (error) {
      throw this.handleError(error)
    }
  }

  async patch(url, data = {}, config = {}) {
    try {
      const response = await this.client.patch(url, data, config)
      return response.data
    } catch (error) {
      throw this.handleError(error)
    }
  }

  async delete(url, config = {}) {
    try {
      const response = await this.client.delete(url, config)
      return response.data
    } catch (error) {
      throw this.handleError(error)
    }
  }

  // File upload
  async upload(url, file, onProgress = null) {
    const formData = new FormData()
    formData.append('file', file)

    const config = {
      headers: { 'Content-Type': 'multipart/form-data' }
    }

    if (onProgress) {
      config.onUploadProgress = (e) => {
        const percentCompleted = Math.round((e.loaded * 100) / e.total)
        onProgress(percentCompleted)
      }
    }

    try {
      const response = await this.client.post(url, formData, config)
      return response.data
    } catch (error) {
      throw this.handleError(error)
    }
  }

  // Error handling
  handleError(error) {
    if (error.response) {
      const { status, data } = error.response
      return {
        message: data.message || 'Có lỗi xảy ra từ server',
        status,
        data: data.data || null,
        type: 'server_error'
      }
    } else if (error.request) {
      return {
        message: 'Không thể kết nối đến server',
        status: 0,
        type: 'network_error'
      }
    } else {
      return {
        message: error.message || 'Có lỗi không xác định',
        status: 0,
        type: 'unknown_error'
      }
    }
  }

  // Chuẩn hoá phản hồi đăng nhập để FE hiểu đúng
  normalizeAuthResponse(be) {
    const accessToken =
      be?.accessToken ||
      be?.token ||
      be?.jwt ||
      be?.data?.accessToken ||
      be?.data?.token ||
      be?.data?.jwt

    const user =
      be?.user ||
      be?.data?.user || {
        id: be?.data?.id ?? be?.id ?? null,
        name: be?.data?.name ?? be?.name ?? be?.username ?? null,
        email: be?.data?.email ?? be?.email ?? null,
        role: be?.data?.role ?? be?.role ?? 'user'
      }

    const success =
      be?.success ??
      be?.ok ??
      (typeof be?.status === 'string'
        ? be.status.toUpperCase() === 'OK'
        : undefined) ??
      Boolean(accessToken)

    return {
      success: Boolean(success),
      data: {
        accessToken: accessToken || null,
        user: user || null
      },
      message: be?.message || 'OK'
    }
  }

  // Auth endpoints
  auth = {
    login: async (credentials) => {
      const be = await this.post('/auth/login', credentials)
      const mapped = this.normalizeAuthResponse(be)
      if (!mapped.data.accessToken) {
        return {
          success: false,
          data: { accessToken: null, user: null },
          message: mapped.message || 'Đăng nhập thất bại: thiếu accessToken'
        }
      }
      return mapped
    },
    register: (userData) => this.post('/auth/register', userData),
    logout: () => this.post('/auth/logout'),
    refresh: () => this.post('/auth/refresh'),
    forgotPassword: (email) => this.post('/auth/forgot-password', { email }),
    resetPassword: (token, password) =>
      this.post('/auth/reset-password', { token, password }),
    verifyEmail: (token) => this.post('/auth/verify-email', { token })
  }

  // User endpoints
  user = {
    getProfile: () => this.get('/user/profile'),
    updateProfile: (data) => this.put('/user/profile', data),
    changePassword: (data) => this.post('/user/change-password', data),
    uploadAvatar: (file, onProgress) =>
      this.upload('/user/avatar', file, onProgress),
    getAddresses: () => this.get('/user/addresses'),
    addAddress: (data) => this.post('/user/addresses', data),
    updateAddress: (id, data) => this.put(`/user/addresses/${id}`, data),
    deleteAddress: (id) => this.delete(`/user/addresses/${id}`)
  }

  // Product endpoints
  products = {
    getAll: (params = {}) => this.get('/products', { params }),
    getById: (id) => this.get(`/products/${id}`),
    getByCategory: (categoryId, params = {}) =>
      this.get(`/products/category/${categoryId}`, { params }),
    search: (query, params = {}) =>
      this.get('/products/search', { params: { q: query, ...params } }),
    getFeatured: () => this.get('/products/featured'),
    getRelated: (id) => this.get(`/products/${id}/related`),
    getReviews: (id) => this.get(`/products/${id}/reviews`),
    addReview: (id, data) => this.post(`/products/${id}/reviews`, data)
  }

  // Category endpoints
  categories = {
    getAll: () => this.get('/categories'),
    getById: (id) => this.get(`/categories/${id}`),
    getProducts: (id, params = {}) =>
      this.get(`/categories/${id}/products`, { params })
  }

  // Cart endpoints
  cart = {
    get: () => this.get('/cart'),
    addItem: (data) => this.post('/cart/items', data),
    updateItem: (id, data) => this.put(`/cart/items/${id}`, data),
    removeItem: (id) => this.delete(`/cart/items/${id}`),
    clear: () => this.delete('/cart'),
    applyCoupon: (code) => this.post('/cart/coupon', { code }),
    removeCoupon: () => this.delete('/cart/coupon')
  }

  // Order endpoints
  orders = {
    getAll: (params = {}) => this.get('/orders', { params }),
    getById: (id) => this.get(`/orders/${id}`),
    create: (data) => this.post('/orders', data),
    update: (id, data) => this.put(`/orders/${id}`, data),
    cancel: (id) => this.post(`/orders/${id}/cancel`),
    getStatus: (id) => this.get(`/orders/${id}/status`)
  }

  // Payment endpoints
  payment = {
    createPayment: (orderId, method) =>
      this.post('/payment/create', { orderId, method }),
    verifyPayment: (paymentId) => this.post('/payment/verify', { paymentId }),
    getMethods: () => this.get('/payment/methods')
  }

  // Newsletter endpoints
  newsletter = {
    subscribe: (email) => this.post('/newsletter/subscribe', { email }),
    unsubscribe: (email) => this.post('/newsletter/unsubscribe', { email })
  }

  // Contact endpoints
  contact = {
    sendMessage: (data) => this.post('/contact', data),
    getFaq: () => this.get('/contact/faq')
  }
}

const apiService = new ApiService()
export default apiService
