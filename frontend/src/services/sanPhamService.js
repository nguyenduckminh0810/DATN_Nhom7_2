import api from './api'

export default {
  page: (params) => api.get('/san-pham', { params }),
  get: (id) => api.get(`/san-pham/${id}`),
  create: (data) => api.post('/san-pham', data),
  update: (id, data) => api.put(`/san-pham/${id}`, data),
  delete: (id) => api.delete(`/san-pham/${id}`),
}
