// Standardized error messages for consistent UX
export const ERROR_MESSAGES = {
  // Network errors
  NETWORK_ERROR: 'Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng.',
  TIMEOUT_ERROR: 'Yêu cầu mất quá nhiều thời gian để xử lý. Vui lòng thử lại.',
  CONNECTION_REFUSED: 'Không thể kết nối đến server. Server có thể đang bảo trì.',
  
  // API errors
  API_ERROR: 'Có lỗi xảy ra từ server. Vui lòng thử lại sau.',
  SERVER_ERROR: 'Server đang gặp sự cố. Vui lòng thử lại sau.',
  BAD_REQUEST: 'Dữ liệu không hợp lệ. Vui lòng kiểm tra lại thông tin.',
  UNAUTHORIZED: 'Bạn cần đăng nhập để tiếp tục.',
  FORBIDDEN: 'Bạn không có quyền thực hiện hành động này.',
  NOT_FOUND: 'Không tìm thấy dữ liệu bạn yêu cầu.',
  VALIDATION_ERROR: 'Dữ liệu không hợp lệ. Vui lòng kiểm tra lại.',
  
  // Authentication errors
  LOGIN_FAILED: 'Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin.',
  REGISTER_FAILED: 'Đăng ký thất bại. Vui lòng thử lại.',
  TOKEN_EXPIRED: 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.',
  INVALID_TOKEN: 'Token không hợp lệ. Vui lòng đăng nhập lại.',
  
  // Product errors
  PRODUCT_NOT_FOUND: 'Không tìm thấy sản phẩm.',
  PRODUCT_OUT_OF_STOCK: 'Sản phẩm đã hết hàng.',
  PRODUCT_LOAD_ERROR: 'Không thể tải thông tin sản phẩm.',
  
  // Cart errors
  CART_EMPTY: 'Giỏ hàng trống.',
  CART_UPDATE_FAILED: 'Không thể cập nhật giỏ hàng.',
  CART_CLEAR_FAILED: 'Không thể xóa giỏ hàng.',
  
  // Order errors
  ORDER_CREATE_FAILED: 'Không thể tạo đơn hàng.',
  ORDER_NOT_FOUND: 'Không tìm thấy đơn hàng.',
  ORDER_UPDATE_FAILED: 'Không thể cập nhật đơn hàng.',
  
  // File upload errors
  FILE_TOO_LARGE: 'File quá lớn. Vui lòng chọn file nhỏ hơn.',
  INVALID_FILE_TYPE: 'Loại file không được hỗ trợ.',
  UPLOAD_FAILED: 'Không thể tải file lên server.',
  
  // Form validation errors
  REQUIRED_FIELD: 'Trường này là bắt buộc.',
  INVALID_EMAIL: 'Email không hợp lệ.',
  INVALID_PHONE: 'Số điện thoại không hợp lệ.',
  PASSWORD_TOO_SHORT: 'Mật khẩu phải có ít nhất 6 ký tự.',
  PASSWORD_MISMATCH: 'Mật khẩu xác nhận không khớp.',
  
  // Generic errors
  UNKNOWN_ERROR: 'Có lỗi không xác định xảy ra.',
  OPERATION_FAILED: 'Thao tác thất bại. Vui lòng thử lại.',
  DATA_LOAD_ERROR: 'Không thể tải dữ liệu.',
  SAVE_FAILED: 'Không thể lưu dữ liệu.',
  DELETE_FAILED: 'Không thể xóa dữ liệu.',
  UPDATE_FAILED: 'Không thể cập nhật dữ liệu.'
}

// Error titles
export const ERROR_TITLES = {
  NETWORK: 'Lỗi kết nối',
  SERVER: 'Lỗi server',
  VALIDATION: 'Lỗi xác thực',
  AUTH: 'Lỗi đăng nhập',
  PERMISSION: 'Không có quyền',
  NOT_FOUND: 'Không tìm thấy',
  GENERIC: 'Có lỗi xảy ra'
}

// Success messages
export const SUCCESS_MESSAGES = {
  LOGIN_SUCCESS: 'Đăng nhập thành công!',
  REGISTER_SUCCESS: 'Đăng ký thành công!',
  LOGOUT_SUCCESS: 'Đăng xuất thành công!',
  PROFILE_UPDATED: 'Cập nhật thông tin thành công!',
  PASSWORD_CHANGED: 'Đổi mật khẩu thành công!',
  AVATAR_UPDATED: 'Cập nhật ảnh đại diện thành công!',
  
  CART_ADDED: 'Đã thêm vào giỏ hàng!',
  CART_UPDATED: 'Cập nhật giỏ hàng thành công!',
  CART_REMOVED: 'Đã xóa khỏi giỏ hàng!',
  CART_CLEARED: 'Đã xóa tất cả sản phẩm khỏi giỏ hàng!',
  
  ORDER_CREATED: 'Đặt hàng thành công!',
  ORDER_UPDATED: 'Cập nhật đơn hàng thành công!',
  ORDER_CANCELLED: 'Hủy đơn hàng thành công!',
  
  REVIEW_ADDED: 'Đánh giá thành công!',
  REVIEW_UPDATED: 'Cập nhật đánh giá thành công!',
  REVIEW_DELETED: 'Xóa đánh giá thành công!',
  
  FILE_UPLOADED: 'Tải file thành công!',
  DATA_SAVED: 'Lưu dữ liệu thành công!',
  DATA_DELETED: 'Xóa dữ liệu thành công!',
  DATA_UPDATED: 'Cập nhật dữ liệu thành công!'
}

// Warning messages
export const WARNING_MESSAGES = {
  UNSAVED_CHANGES: 'Bạn có thay đổi chưa được lưu. Bạn có chắc chắn muốn rời khỏi trang?',
  CONFIRM_DELETE: 'Bạn có chắc chắn muốn xóa?',
  CONFIRM_CLEAR: 'Bạn có chắc chắn muốn xóa tất cả?',
  CONFIRM_LOGOUT: 'Bạn có chắc chắn muốn đăng xuất?',
  CONFIRM_CANCEL: 'Bạn có chắc chắn muốn hủy?',
  
  CART_EMPTY_WARNING: 'Giỏ hàng trống. Vui lòng thêm sản phẩm trước khi thanh toán.',
  STOCK_LOW: 'Sản phẩm sắp hết hàng.',
  PRICE_CHANGED: 'Giá sản phẩm đã thay đổi.',
  
  SESSION_EXPIRING: 'Phiên đăng nhập sắp hết hạn. Vui lòng đăng nhập lại.',
  NETWORK_SLOW: 'Kết nối mạng chậm. Vui lòng kiểm tra kết nối.',
  DATA_OUTDATED: 'Dữ liệu có thể đã cũ. Vui lòng tải lại trang.'
}

// Info messages
export const INFO_MESSAGES = {
  LOADING: 'Đang tải dữ liệu...',
  SAVING: 'Đang lưu dữ liệu...',
  PROCESSING: 'Đang xử lý...',
  UPLOADING: 'Đang tải file...',
  SEARCHING: 'Đang tìm kiếm...',
  
  NO_RESULTS: 'Không tìm thấy kết quả nào.',
  NO_DATA: 'Không có dữ liệu để hiển thị.',
  COMING_SOON: 'Tính năng sắp ra mắt.',
  MAINTENANCE: 'Hệ thống đang bảo trì.',
  
  FEATURE_BETA: 'Tính năng đang trong giai đoạn thử nghiệm.',
  FEATURE_PREMIUM: 'Tính năng chỉ dành cho tài khoản premium.',
  FEATURE_UNAVAILABLE: 'Tính năng tạm thời không khả dụng.'
}

// Error categories for better handling
export const ERROR_CATEGORIES = {
  NETWORK: 'network',
  SERVER: 'server', 
  VALIDATION: 'validation',
  AUTH: 'auth',
  PERMISSION: 'permission',
  NOT_FOUND: 'not_found',
  GENERIC: 'generic'
}

// Helper function to get error message by category
export const getErrorMessage = (category, customMessage = null) => {
  if (customMessage) return customMessage
  
  switch (category) {
    case ERROR_CATEGORIES.NETWORK:
      return ERROR_MESSAGES.NETWORK_ERROR
    case ERROR_CATEGORIES.SERVER:
      return ERROR_MESSAGES.SERVER_ERROR
    case ERROR_CATEGORIES.VALIDATION:
      return ERROR_MESSAGES.VALIDATION_ERROR
    case ERROR_CATEGORIES.AUTH:
      return ERROR_MESSAGES.UNAUTHORIZED
    case ERROR_CATEGORIES.PERMISSION:
      return ERROR_MESSAGES.FORBIDDEN
    case ERROR_CATEGORIES.NOT_FOUND:
      return ERROR_MESSAGES.NOT_FOUND
    default:
      return ERROR_MESSAGES.UNKNOWN_ERROR
  }
}

// Helper function to get error title by category
export const getErrorTitle = (category) => {
  switch (category) {
    case ERROR_CATEGORIES.NETWORK:
      return ERROR_TITLES.NETWORK
    case ERROR_CATEGORIES.SERVER:
      return ERROR_TITLES.SERVER
    case ERROR_CATEGORIES.VALIDATION:
      return ERROR_TITLES.VALIDATION
    case ERROR_CATEGORIES.AUTH:
      return ERROR_TITLES.AUTH
    case ERROR_CATEGORIES.PERMISSION:
      return ERROR_TITLES.PERMISSION
    case ERROR_CATEGORIES.NOT_FOUND:
      return ERROR_TITLES.NOT_FOUND
    default:
      return ERROR_TITLES.GENERIC
  }
}
