import apiService from './api'

class CategoryService {
  // Lấy tất cả danh mục
  async getAll() {
    try {
      const response = await apiService.categories.getAll()
      return {
        success: true,
        data: response.data || response,
        message: 'Lấy danh sách danh mục thành công'
      }
    } catch (error) {
      return {
        success: false,
        data: null,
        message: error.message || 'Lỗi khi lấy danh sách danh mục'
      }
    }
  }

  // Lấy danh mục theo ID
  async getById(id) {
    try {
      const response = await apiService.categories.getById(id)
      return {
        success: true,
        data: response.data || response,
        message: 'Lấy thông tin danh mục thành công'
      }
    } catch (error) {
      return {
        success: false,
        data: null,
        message: error.message || 'Lỗi khi lấy thông tin danh mục'
      }
    }
  }

  // Lấy sản phẩm theo danh mục
  async getProducts(id, params = {}) {
    try {
      const response = await apiService.categories.getProducts(id, params)
      return {
        success: true,
        data: response.data || response,
        message: 'Lấy sản phẩm theo danh mục thành công'
      }
    } catch (error) {
      return {
        success: false,
        data: null,
        message: error.message || 'Lỗi khi lấy sản phẩm theo danh mục'
      }
    }
  }

  // Tạo danh mục mới
  async create(data) {
    try {
      const response = await apiService.categories.create(data)
      return {
        success: true,
        data: response.data || response,
        message: response.message || 'Tạo danh mục thành công'
      }
    } catch (error) {
      return {
        success: false,
        data: null,
        message: error.message || 'Lỗi khi tạo danh mục'
      }
    }
  }
}

const categoryService = new CategoryService()
export default categoryService