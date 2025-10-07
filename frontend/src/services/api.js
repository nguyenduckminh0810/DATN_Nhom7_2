import axios from 'axios'
import config from '@/config/env'

const api = axios.create({
  baseURL: config.api.baseURL,
  timeout: config.api.timeout,
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use(
  (cfg) => {
    const token = localStorage.getItem('token')
    if (token) cfg.headers.Authorization = `Bearer ${token}`
    return cfg
  },
  (error) => Promise.reject(error),
)

export default api
