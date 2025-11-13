import apiService from './api'

class ThongKeService {
  getSummary(params = {}) {
    const final = { lowStockThreshold: params.lowStockThreshold ?? 10 }
    return apiService.get('/thong-ke/summary', { params: final })
  }
  getAlerts(params = {}) {
    const final = { lowStockThreshold: params.lowStockThreshold ?? 10 }
    return apiService.get('/thong-ke/alerts', { params: final })
  }
  getChart(params = {}) {
    const final = {
      range: params.range ?? '30days',
      metric: params.metric ?? 'revenue',
    }
    return apiService.get('/thong-ke/chart', { params: final })
  }
  getTopProducts(params = {}) {
    const final = {
      limit: params.limit ?? 5,
      rangeDays: params.rangeDays ?? 30,
    }
    return apiService.get('/thong-ke/top-products', { params: final })
  }
  getRecentOrders(params = {}) {
    const final = { limit: params.limit ?? 10 }
    return apiService.get('/thong-ke/recent-orders', { params: final })
  }
  getCustomerSummary() {
    return apiService.get('/thong-ke/customers/summary')
  }
  getCategoryPerformance(params = {}) {
    const final = {
      limit: params.limit ?? 6,
      rangeDays: params.rangeDays ?? 30,
    }
    return apiService.get('/thong-ke/categories/performance', { params: final })
  }
  getOrderStatusCounts() {
    return apiService.get('/thong-ke/orders/status-counts')
  }

  // Analytics endpoints
  getAnalyticsKpis(params = {}) {
    return apiService.get('/thong-ke/analytics/kpis', { params })
  }

  getHourlySales(params = {}) {
    return apiService.get('/thong-ke/analytics/hourly-sales', { params })
  }

  getBusinessInsights(params = {}) {
    return apiService.get('/thong-ke/analytics/business-insights', { params })
  }

  getPerformanceMetrics(params = {}) {
    return apiService.get('/thong-ke/analytics/performance', { params })
  }

  getOrderDistribution(params = {}) {
    const final = {
      type: params.type ?? 'status',
      ...params
    }
    return apiService.get('/thong-ke/analytics/order-distribution', { params: final })
  }

  getCustomerAnalytics(params = {}) {
    const final = {
      type: params.type ?? 'segments',
      ...params
    }
    return apiService.get('/thong-ke/analytics/customer-analytics', { params: final })
  }
}

const thongKeService = new ThongKeService()
export default thongKeService

