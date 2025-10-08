import { ref, onMounted, onUnmounted } from 'vue'

// Performance metrics store
const metrics = ref({
  // Core Web Vitals
  lcp: null, // Largest Contentful Paint
  fid: null, // First Input Delay
  cls: null, // Cumulative Layout Shift
  
  // Navigation timing
  fcp: null, // First Contentful Paint
  ttfb: null, // Time to First Byte
  domContentLoaded: null,
  loadComplete: null,
  
  // Custom metrics
  componentRenderTime: {},
  apiResponseTime: {},
  imageLoadTime: {},
  
  // Memory usage
  memoryUsage: null,
  
  // Network info
  connectionType: null,
  effectiveType: null
})

// Performance observer instances
let performanceObserver = null
let memoryObserver = null

export function usePerformance() {
  const isSupported = ref(false)
  const isMonitoring = ref(false)

  // Check browser support
  const checkSupport = () => {
    isSupported.value = !!(
      window.performance &&
      window.PerformanceObserver &&
      window.requestIdleCallback
    )
    return isSupported.value
  }

  // Initialize performance monitoring
  const startMonitoring = () => {
    if (!checkSupport() || isMonitoring.value) {
      return false
    }

    isMonitoring.value = true

    // Monitor Core Web Vitals
    setupWebVitalsObserver()
    
    // Monitor memory usage
    setupMemoryObserver()
    
    // Monitor navigation timing
    measureNavigationTiming()
    
    // Monitor network information
    getNetworkInformation()

    return true
  }

  // Stop performance monitoring
  const stopMonitoring = () => {
    isMonitoring.value = false
    
    if (performanceObserver) {
      performanceObserver.disconnect()
      performanceObserver = null
    }
    
    if (memoryObserver) {
      memoryObserver.disconnect()
      memoryObserver = null
    }
  }

  // Setup Web Vitals observer
  const setupWebVitalsObserver = () => {
    try {
      performanceObserver = new PerformanceObserver((list) => {
        list.getEntries().forEach((entry) => {
          switch (entry.entryType) {
            case 'largest-contentful-paint':
              metrics.value.lcp = entry.startTime
              break
            case 'first-input':
              metrics.value.fid = entry.processingStart - entry.startTime
              break
            case 'layout-shift':
              if (!entry.hadRecentInput) {
                metrics.value.cls = (metrics.value.cls || 0) + entry.value
              }
              break
            case 'paint':
              if (entry.name === 'first-contentful-paint') {
                metrics.value.fcp = entry.startTime
              }
              break
          }
        })
      })

      // Observe different entry types
      performanceObserver.observe({ entryTypes: ['largest-contentful-paint'] })
      performanceObserver.observe({ entryTypes: ['first-input'] })
      performanceObserver.observe({ entryTypes: ['layout-shift'] })
      performanceObserver.observe({ entryTypes: ['paint'] })
    } catch (error) {
      console.warn('Performance Observer not supported:', error)
    }
  }

  // Setup memory observer
  const setupMemoryObserver = () => {
    if ('memory' in performance) {
      const updateMemoryUsage = () => {
        metrics.value.memoryUsage = {
          used: Math.round(performance.memory.usedJSHeapSize / 1024 / 1024),
          total: Math.round(performance.memory.totalJSHeapSize / 1024 / 1024),
          limit: Math.round(performance.memory.jsHeapSizeLimit / 1024 / 1024)
        }
      }

      updateMemoryUsage()
      
      // Update memory usage periodically
      const memoryInterval = setInterval(() => {
        if (isMonitoring.value) {
          updateMemoryUsage()
        } else {
          clearInterval(memoryInterval)
        }
      }, 5000)
    }
  }

  // Measure navigation timing
  const measureNavigationTiming = () => {
    if ('getEntriesByType' in performance) {
      const navigationEntries = performance.getEntriesByType('navigation')
      if (navigationEntries.length > 0) {
        const nav = navigationEntries[0]
        
        metrics.value.ttfb = nav.responseStart - nav.requestStart
        metrics.value.domContentLoaded = nav.domContentLoadedEventEnd - nav.navigationStart
        metrics.value.loadComplete = nav.loadEventEnd - nav.navigationStart
      }
    }
  }

  // Get network information
  const getNetworkInformation = () => {
    if ('connection' in navigator) {
      const connection = navigator.connection
      metrics.value.connectionType = connection.type
      metrics.value.effectiveType = connection.effectiveType
    }
  }

  // Measure component render time
  const measureComponentRender = (componentName, renderFn) => {
    const startTime = performance.now()
    
    const result = renderFn()
    
    const endTime = performance.now()
    const renderTime = endTime - startTime
    
    metrics.value.componentRenderTime[componentName] = {
      time: renderTime,
      timestamp: Date.now()
    }
    
    return result
  }

  // Measure API response time
  const measureApiCall = async (apiName, apiCall) => {
    const startTime = performance.now()
    
    try {
      const result = await apiCall()
      const endTime = performance.now()
      const responseTime = endTime - startTime
      
      metrics.value.apiResponseTime[apiName] = {
        time: responseTime,
        success: true,
        timestamp: Date.now()
      }
      
      return result
    } catch (error) {
      const endTime = performance.now()
      const responseTime = endTime - startTime
      
      metrics.value.apiResponseTime[apiName] = {
        time: responseTime,
        success: false,
        error: error.message,
        timestamp: Date.now()
      }
      
      throw error
    }
  }

  // Measure image load time
  const measureImageLoad = (imageSrc) => {
    return new Promise((resolve, reject) => {
      const startTime = performance.now()
      
      const img = new Image()
      
      img.onload = () => {
        const endTime = performance.now()
        const loadTime = endTime - startTime
        
        metrics.value.imageLoadTime[imageSrc] = {
          time: loadTime,
          success: true,
          timestamp: Date.now()
        }
        
        resolve(img)
      }
      
      img.onerror = () => {
        const endTime = performance.now()
        const loadTime = endTime - startTime
        
        metrics.value.imageLoadTime[imageSrc] = {
          time: loadTime,
          success: false,
          timestamp: Date.now()
        }
        
        reject(new Error('Image load failed'))
      }
      
      img.src = imageSrc
    })
  }

  // Get performance report
  const getPerformanceReport = () => {
    const report = {
      timestamp: Date.now(),
      userAgent: navigator.userAgent,
      url: window.location.href,
      metrics: { ...metrics.value }
    }

    // Calculate performance score
    report.score = calculatePerformanceScore()

    return report
  }

  // Calculate performance score (0-100)
  const calculatePerformanceScore = () => {
    let score = 100
    const weights = {
      lcp: 25,    // Largest Contentful Paint
      fid: 25,    // First Input Delay
      cls: 25,    // Cumulative Layout Shift
      fcp: 15,    // First Contentful Paint
      ttfb: 10    // Time to First Byte
    }

    // LCP scoring (Good: <2.5s, Needs Improvement: 2.5-4s, Poor: >4s)
    if (metrics.value.lcp) {
      if (metrics.value.lcp > 4000) score -= weights.lcp
      else if (metrics.value.lcp > 2500) score -= weights.lcp * 0.5
    }

    // FID scoring (Good: <100ms, Needs Improvement: 100-300ms, Poor: >300ms)
    if (metrics.value.fid) {
      if (metrics.value.fid > 300) score -= weights.fid
      else if (metrics.value.fid > 100) score -= weights.fid * 0.5
    }

    // CLS scoring (Good: <0.1, Needs Improvement: 0.1-0.25, Poor: >0.25)
    if (metrics.value.cls) {
      if (metrics.value.cls > 0.25) score -= weights.cls
      else if (metrics.value.cls > 0.1) score -= weights.cls * 0.5
    }

    // FCP scoring (Good: <1.8s, Needs Improvement: 1.8-3s, Poor: >3s)
    if (metrics.value.fcp) {
      if (metrics.value.fcp > 3000) score -= weights.fcp
      else if (metrics.value.fcp > 1800) score -= weights.fcp * 0.5
    }

    // TTFB scoring (Good: <800ms, Needs Improvement: 800-1800ms, Poor: >1800ms)
    if (metrics.value.ttfb) {
      if (metrics.value.ttfb > 1800) score -= weights.ttfb
      else if (metrics.value.ttfb > 800) score -= weights.ttfb * 0.5
    }

    return Math.max(0, Math.round(score))
  }

  // Export performance data
  const exportPerformanceData = () => {
    const report = getPerformanceReport()
    const dataStr = JSON.stringify(report, null, 2)
    const dataBlob = new Blob([dataStr], { type: 'application/json' })
    
    const link = document.createElement('a')
    link.href = URL.createObjectURL(dataBlob)
    link.download = `performance-report-${Date.now()}.json`
    link.click()
  }

  // Log performance metrics
  const logMetrics = () => {
    console.group('🚀 Performance Metrics')
    console.log('📊 Core Web Vitals:')
    console.log(`  LCP: ${metrics.value.lcp ? `${metrics.value.lcp.toFixed(2)}ms` : 'N/A'}`)
    console.log(`  FID: ${metrics.value.fid ? `${metrics.value.fid.toFixed(2)}ms` : 'N/A'}`)
    console.log(`  CLS: ${metrics.value.cls ? metrics.value.cls.toFixed(3) : 'N/A'}`)
    console.log(`  FCP: ${metrics.value.fcp ? `${metrics.value.fcp.toFixed(2)}ms` : 'N/A'}`)
    console.log(`  TTFB: ${metrics.value.ttfb ? `${metrics.value.ttfb.toFixed(2)}ms` : 'N/A'}`)
    console.log(`📈 Performance Score: ${calculatePerformanceScore()}/100`)
    
    if (metrics.value.memoryUsage) {
      console.log('💾 Memory Usage:')
      console.log(`  Used: ${metrics.value.memoryUsage.used}MB`)
      console.log(`  Total: ${metrics.value.memoryUsage.total}MB`)
      console.log(`  Limit: ${metrics.value.memoryUsage.limit}MB`)
    }
    
    if (metrics.value.connectionType) {
      console.log('🌐 Network:')
      console.log(`  Type: ${metrics.value.connectionType}`)
      console.log(`  Effective: ${metrics.value.effectiveType}`)
    }
    
    console.groupEnd()
  }

  // Auto-start monitoring in development
  if (import.meta.env.DEV) {
    onMounted(() => {
      startMonitoring()
    })
  }

  return {
    // State
    metrics,
    isSupported,
    isMonitoring,
    
    // Methods
    startMonitoring,
    stopMonitoring,
    measureComponentRender,
    measureApiCall,
    measureImageLoad,
    getPerformanceReport,
    calculatePerformanceScore,
    exportPerformanceData,
    logMetrics
  }
}

// Global performance instance
export const globalPerformance = usePerformance()
