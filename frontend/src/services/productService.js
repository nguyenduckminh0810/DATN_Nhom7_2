import apiService from './api'

class ProductService {
  // Lấy tất cả sản phẩm
  async getAll(params = {}) {
    try {
      const response = await apiService.products.getAll(params)
      return {
        success: true,
        data: response.data || response,
        message: 'Lấy danh sách sản phẩm thành công',
      }
    } catch (error) {
      return {
        success: false,
        data: null,
        message: error.message || 'Lỗi khi lấy danh sách sản phẩm',
      }
    }
  }

  // Lấy sản phẩm theo ID
  async getById(id) {
    try {
      const response = await apiService.products.getById(id)
      return {
        success: true,
        data: response.data || response,
        message: 'Lấy thông tin sản phẩm thành công',
      }
    } catch (error) {
      return {
        success: false,
        data: null,
        message: error.message || 'Lỗi khi lấy thông tin sản phẩm',
      }
    }
  }

  // Lấy sản phẩm theo danh mục
  async getByCategory(categoryId, params = {}) {
    try {
      const response = await apiService.products.getByCategory(categoryId, params)
      return {
        success: true,
        data: response.data || response,
        message: 'Lấy sản phẩm theo danh mục thành công',
      }
    } catch (error) {
      return {
        success: false,
        data: null,
        message: error.message || 'Lỗi khi lấy sản phẩm theo danh mục',
      }
    }
  }

  // Tìm kiếm sản phẩm
  async search(query, params = {}) {
    try {
      const response = await apiService.products.search(query, params)
      return {
        success: true,
        data: response.data || response,
        message: 'Tìm kiếm sản phẩm thành công',
      }
    } catch (error) {
      return {
        success: false,
        data: null,
        message: error.message || 'Lỗi khi tìm kiếm sản phẩm',
      }
    }
  }

  // Lấy sản phẩm liên quan
  async getRelated(id) {
    try {
      const response = await apiService.products.getRelated(id)
      return {
        success: true,
        data: response.data || response,
        message: 'Lấy sản phẩm liên quan thành công',
      }
    } catch (error) {
      return {
        success: false,
        data: null,
        message: error.message || 'Lỗi khi lấy sản phẩm liên quan',
      }
    }
  }

  // Lấy đánh giá sản phẩm
  async getReviews(id) {
    try {
      const response = await apiService.products.getReviews(id)
      return {
        success: true,
        data: response.data || response,
        message: 'Lấy đánh giá sản phẩm thành công',
      }
    } catch (error) {
      return {
        success: false,
        data: null,
        message: error.message || 'Lỗi khi lấy đánh giá sản phẩm',
      }
    }
  }

  // Thêm đánh giá sản phẩm
  async addReview(id, data) {
    try {
      const response = await apiService.products.addReview(id, data)
      return {
        success: true,
        data: response.data || response,
        message: response.message || 'Thêm đánh giá thành công',
      }
    } catch (error) {
      return {
        success: false,
        data: null,
        message: error.message || 'Lỗi khi thêm đánh giá',
      }
    }
  }

  // Lấy sản phẩm mới - sản phẩm có trạng thái hoạt động và còn hàng
  async getNewArrivals(params = {}) {
    try {
      const queryParams = {
        ...params,
        sortBy: 'created_at',
        sortOrder: 'desc',
        status: 'active',
        inStock: true,
        limit: params.limit || 10,
      }

      const response = await apiService.products.getAll(queryParams)
      return {
        success: true,
        data: response.data || response,
        message: 'Lấy sản phẩm mới thành công',
      }
    } catch (error) {
      return {
        success: false,
        data: null,
        message: error.message || 'Lỗi khi lấy sản phẩm mới',
      }
    }
  }
}

const productService = new ProductService()
export default productService
