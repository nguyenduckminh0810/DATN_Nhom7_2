import axios from 'axios'

class ApiService {
  constructor() {
    this.baseURL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'
    this.client = axios.create({
      baseURL: this.baseURL,
      timeout: 10000,
      withCredentials: true, // ✅ Quan trọng: Gửi cookie session
    })
    this.setupInterceptors()
  }

  setupInterceptors() {
    // Request interceptor
    this.client.interceptors.request.use(
      (config) => {
        const method = (config.method || 'get').toLowerCase()
        const url = config.url || ''
        
        // Chỉ set Content-Type nếu chưa được set và không phải FormData
        if (!config.headers['Content-Type'] && !(config.data instanceof FormData)) {
          config.headers['Content-Type'] = 'application/json'
        }
        
        // Xóa Content-Type nếu là FormData để browser tự động thêm boundary
        if (config.data instanceof FormData && config.headers['Content-Type']) {
          delete config.headers['Content-Type']
        }
        
        // Public endpoints không cần JWT token
        const publicGetPrefixes = ['/san-pham', '/danh-muc', '/phieu-giam-gia/co-san', '/hinh-anh', '/shipping', '/mau-sac', '/chat-lieu']
        const publicPrefixes = ['/shipping'] // Public cho tất cả methods (GET, POST, etc.)
        
        const isPublicGet = method === 'get' && publicGetPrefixes.some((p) => url.startsWith(p))
        const isPublicEndpoint = publicPrefixes.some((p) => url.startsWith(p))

        const token = localStorage.getItem('auro_token')
        
        // Luôn gửi token nếu có (trừ public endpoints)
        if (token && !isPublicGet && !isPublicEndpoint) {
          config.headers.Authorization = `Bearer ${token}`
          
        } else {
          // ensure we don't accidentally send a stale header
          if (config.headers && 'Authorization' in config.headers) {
            delete config.headers.Authorization
          }
          
        }
        return config
      },
      (error) => Promise.reject(error),
    )

    // Response interceptor
    this.client.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          // Kiểm tra xem có phải là lỗi từ login endpoint không
          const isLoginEndpoint = error.config?.url?.includes('/auth/login')
          
          // Chỉ reload trang nếu KHÔNG phải là lỗi từ login
          // Khi đăng nhập sai, không reload để hiển thị thông báo lỗi
          if (!isLoginEndpoint) {
            localStorage.removeItem('auro_token')
            localStorage.removeItem('auro_user')
            // Chỉ reload nếu không phải đang ở trang login
            const currentPath = window.location.pathname
            if (!currentPath.includes('/login') && !currentPath.includes('/register')) {
              window.location.href = '/'
            }
          } else {
            // Nếu là lỗi từ login, chỉ xóa token/user nếu có (có thể là token cũ)
            // Nhưng không reload trang để hiển thị thông báo lỗi
            const existingToken = localStorage.getItem('auro_token')
            if (existingToken) {
              // Có thể là token cũ không hợp lệ, xóa nó
              localStorage.removeItem('auro_token')
              localStorage.removeItem('auro_user')
            }
            // Không reload - để LoginPopup hiển thị thông báo lỗi
          }
        }
        return Promise.reject(error)
      },
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
  async upload(url, file, fields = {}, onProgress = null) {
    const formData = new FormData()
    formData.append('file', file)
    if (fields && typeof fields === 'object') {
      Object.entries(fields).forEach(([k, v]) => {
        if (v !== undefined && v !== null) formData.append(k, v)
      })
    }

    const config = {
      headers: { 'Content-Type': 'multipart/form-data' },
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
      const message =
        (typeof data === 'string' ? data : null) ||
        data?.message ||
        data?.error ||
        'Có lỗi xảy ra từ server'
      return {
        message,
        status,
        data: (typeof data === 'object' ? data.data : null) || null,
        type: 'server_error',
      }
    } else if (error.request) {
      return {
        message: 'Không thể kết nối đến server',
        status: 0,
        type: 'network_error',
      }
    } else {
      return {
        message: error.message || 'Có lỗi không xác định',
        status: 0,
        type: 'unknown_error',
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

    // Backend trả về user info trong be.data.user hoặc be.user
    let user = be?.data?.user || be?.user

    // Nếu có user, map các trường từ backend sang frontend
    if (user) {
      user = {
        id: user.id,
        name: user.hoTen || user.name || user.email?.split('@')[0] || 'User',
        email: user.email,
        phone: user.soDienThoai,
        role: user.vaiTro || user.role, // Backend trả về vaiTro (ADM, STF, CUS)
        vaiTroMa: user.vaiTro || user.vaiTroMa, // Lưu cả vaiTroMa
        trangThai: user.trangThai,
        kieu: user.kieu,
      }
    } else {
      // Fallback nếu không có user object
      user = {
        id: be?.data?.id ?? be?.id ?? null,
        name: be?.data?.hoTen ?? be?.data?.name ?? be?.name ?? be?.username ?? null,
        email: be?.data?.email ?? be?.email ?? null,
        role: be?.data?.vaiTro ?? be?.data?.role ?? be?.role ?? 'user',
        vaiTroMa: be?.data?.vaiTro ?? be?.data?.vaiTroMa ?? be?.vaiTroMa ?? null,
      }
    }

    

    const success =
      be?.success ??
      be?.ok ??
      (typeof be?.status === 'string' ? be.status.toUpperCase() === 'OK' : undefined) ??
      Boolean(accessToken)

    return {
      success: Boolean(success),
      data: {
        accessToken: accessToken || null,
        user: user || null,
      },
      message: be?.message || 'OK',
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
          message: mapped.message || 'Đăng nhập thất bại: thiếu accessToken',
        }
      }
      return mapped
    },
    register: (userData) => this.post('/auth/register', userData),
    me: () => this.get('/auth/me'),
    logout: () => this.post('/auth/logout'),
    refresh: () => this.post('/auth/refresh'),
    forgotPassword: (email) => this.post('/auth/forgot-password', { email }),
    resetPassword: (token, password) => this.post('/auth/reset-password', { token, password }),
    verifyEmail: (token) => this.post('/auth/verify-email', { token }),
  }

  // User endpoints
  user = {
    getProfile: () => this.get('/auth/me'),
    updateProfile: (data) => this.put('/user/profile', data),
    changePassword: (data) => this.post('/user/change-password', data),
    uploadAvatar: async (file) => {
      const formData = new FormData()
      formData.append('file', file)
      
      try {
        // FormData sẽ được interceptor tự động xử lý
        const response = await this.client.post('/auth/upload-avatar', formData)
        return response.data
      } catch (error) {
        throw this.handleError(error)
      }
    },
    getAddresses: () => this.get('/user/addresses'),
    addAddress: (data) => this.post('/user/addresses', data),
    updateAddress: (id, data) => this.put(`/user/addresses/${id}`, data),
    deleteAddress: (id) => this.delete(`/user/addresses/${id}`),
  }

  // Product endpoints
  products = {
    getAll: (params = {}) => this.get('/san-pham', { params }),
    getById: (id) => this.get(`/san-pham/${id}`),
    getByCategory: (categoryId, params = {}) =>
      this.get(`/san-pham/danh-muc/${categoryId}`, { params }),
    search: (query, params = {}) =>
      this.get('/san-pham', { params: { search: query, ...params } }),
    getFeatured: () => this.get('/san-pham/featured'),
    getBestSellers: (params = {}) => this.get('/san-pham/ban-chay', { params }),
    getRelated: (id) => this.get(`/san-pham/id/${id}/related`),
    getReviews: (id, params = {}) => this.get(`/san-pham/id/${id}/reviews`, { params }),
    addReview: (id, data) => this.post(`/san-pham/${id}/reviews`, data),
  }

  // Category endpoints
  categories = {
    getAll: () => this.get('/danh-muc'),
    getById: (id) => this.get(`/danh-muc/${id}`),
    getProducts: (id, params = {}) => this.get(`/danh-muc/${id}/san-pham`, { params }),
    create: (data) => this.post('/danh-muc/create', data),
  }

  // add delete
  categoriesDelete = {
    delete: (id, force = false) => this.delete(`/danh-muc/${id}`, { params: { force } }),
  }

  // Cart endpoints
  cart = {
    get: () => this.get('/cart'),
    addItem: (data) => this.post('/cart/items', data),
    updateItem: (id, data) => this.put(`/cart/items/${id}`, data),
    removeItem: (id) => this.delete(`/cart/items/${id}`),
    clear: () => this.delete('/cart'),
    applyCoupon: (code) => this.post('/cart/coupon', { code }),
    removeCoupon: () => this.delete('/cart/coupon'),
  }

  // Order endpoints
  orders = {
    getAll: (params = {}) => this.get('/orders', { params }),
    getById: (id) => this.get(`/orders/${id}`),
    create: (data) => this.post('/orders', data),
    update: (id, data) => this.put(`/orders/${id}`, data),
    cancel: (id) => this.post(`/orders/${id}/cancel`),
    getStatus: (id) => this.get(`/orders/${id}/status`),
    track: (params = {}) => this.get('/don-hang/theo-doi', { params }),
  }

  // Payment endpoints
  payment = {
    createPayment: (orderId, method) => this.post('/payment/create', { orderId, method }),
    verifyPayment: (paymentId) => this.post('/payment/verify', { paymentId }),
    getMethods: () => this.get('/payment/methods'),
  }

  // Newsletter endpoints
  newsletter = {
    subscribe: (email) => this.post('/newsletter/subscribe', { email }),
    unsubscribe: (email) => this.post('/newsletter/unsubscribe', { email }),
  }

  // Voucher
  voucher = {
    getAvailable: () => this.get('/phieu-giam-gia/co-san'),
    apDung: (data) => this.post('/phieu-giam-gia/ap-dung', data),
    kiemTra: (data) => this.post('/phieu-giam-gia/kiem-tra', data),
  }

  // Admin Voucher
  adminVoucher = {
    getAll: () => this.get('/phieu-giam-gia/quan-ly'),
    getById: (id) => this.get(`/phieu-giam-gia/quan-ly/${id}`),
    create: (data) => this.post('/phieu-giam-gia/quan-ly', data),
    update: (id, data) => this.put(`/phieu-giam-gia/quan-ly/${id}`, data),
    delete: (id) => this.delete(`/phieu-giam-gia/quan-ly/${id}`),
    reactivate: (id, soNgayGiaHan) => this.post(`/phieu-giam-gia/quan-ly/${id}/reactivate`, null, { params: { soNgayGiaHan } }),
    deactivate: (id) => this.post(`/phieu-giam-gia/quan-ly/${id}/deactivate`),
  }

  // Admin Users
  adminUsers = {
    getAll: (params = {}) => this.get('/nguoi-dung', { params }),
    getById: (id) => this.get(`/nguoi-dung/${id}`),
    update: (id, data) => this.put(`/nguoi-dung/${id}`, data),
    softDelete: (id) => this.delete(`/nguoi-dung/${id}`),
    // Bulk update trạng thái: PUT /api/admin/users/bulk-status
    bulkStatus: (data) => this.put('/admin/users/bulk-status', data),
    // Get orders by user ID
    getOrdersByUserId: (userId, params = {}) => this.get(`/don-hang/nguoi-dung/${userId}`, { params }),
  }

  // Contact endpoints
  contact = {
    sendMessage: (data) => this.post('/contact', data),
    getFaq: () => this.get('/contact/faq'),
  }

  // Shipping endpoints (GHN)
  shipping = {
    getProvinces: () => this.get('/shipping/provinces'),
    getDistricts: (provinceId) => this.get('/shipping/districts', { params: { provinceId } }),
    getWards: (districtId) => this.get('/shipping/wards', { params: { districtId } }),
    getServices: (toDistrictId) => this.get('/shipping/services', { params: { toDistrictId } }),
    calculate: (data) => this.post('/shipping/calculate', data),
    calculateFull: (data) => this.post('/shipping/calculate-full', data),
  }

  // Material (Chất liệu) endpoints
  materials = {
    getAll: () => this.get('/chat-lieu'),
    getById: (id) => this.get(`/chat-lieu/${id}`),
    create: (data) => this.post('/chat-lieu', data),
    update: (id, data) => this.put(`/chat-lieu/${id}`, data),
    delete: (id) => this.delete(`/chat-lieu/${id}`),
  }
}

const apiService = new ApiService()
export default apiService
