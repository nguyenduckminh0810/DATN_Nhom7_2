// src/services/variantService.js
import api from './api'

export default {
  // Upsert variants & stock for a product
  upsert: (productId, data) => api.post(`/san-pham/${productId}/variants`, data),
}
