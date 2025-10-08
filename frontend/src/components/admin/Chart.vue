<template>
  <div class="chart-container">
    <canvas ref="chartCanvas"></canvas>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import {
  Chart,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  ArcElement,
  Title,
  Tooltip,
  Legend,
  Filler,
  LineController,
  BarController,
  DoughnutController
} from 'chart.js'

// Register Chart.js components
Chart.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  ArcElement,
  Title,
  Tooltip,
  Legend,
  Filler,
  LineController,
  BarController,
  DoughnutController
)

const props = defineProps({
  type: {
    type: String,
    default: 'line',
    validator: (value) => ['line', 'bar', 'pie', 'doughnut'].includes(value)
  },
  data: {
    type: Object,
    required: true
  },
  options: {
    type: Object,
    default: () => ({})
  },
  height: {
    type: Number,
    default: 300
  }
})

const chartCanvas = ref(null)
let chartInstance = null

const defaultOptions = {
  responsive: true,
  maintainAspectRatio: false,
  interaction: {
    intersect: false,
    mode: 'index',
  },
  plugins: {
    legend: {
      position: 'top',
      labels: {
        usePointStyle: true,
        padding: 20,
        font: {
          family: 'Inter, system-ui, sans-serif',
          size: 12,
          weight: '500'
        },
        color: '#64748b'
      }
    },
    title: {
      display: false,
    },
    tooltip: {
      backgroundColor: 'rgba(15, 23, 42, 0.95)',
      titleColor: '#f8fafc',
      bodyColor: '#e2e8f0',
      borderColor: 'rgba(148, 163, 184, 0.2)',
      borderWidth: 1,
      cornerRadius: 12,
      displayColors: true,
      padding: 12,
      titleFont: {
        family: 'Inter, system-ui, sans-serif',
        size: 13,
        weight: '600'
      },
      bodyFont: {
        family: 'Inter, system-ui, sans-serif',
        size: 12,
        weight: '500'
      },
      boxPadding: 6,
      usePointStyle: true,
      callbacks: {
        title: function(context) {
          return context[0].label
        },
        label: function(context) {
          return context.dataset.label + ': ' + context.parsed.y
        }
      }
    }
  },
  scales: {
    y: {
      beginAtZero: true,
      grid: {
        color: 'rgba(148, 163, 184, 0.1)',
        drawBorder: false,
        lineWidth: 1
      },
      ticks: {
        color: '#64748b',
        font: {
          family: 'Inter, system-ui, sans-serif',
          size: 11,
          weight: '500'
        },
        padding: 8
      },
      border: {
        display: false
      }
    },
    x: {
      grid: {
        color: 'rgba(148, 163, 184, 0.1)',
        drawBorder: false,
        lineWidth: 1
      },
      ticks: {
        color: '#64748b',
        font: {
          family: 'Inter, system-ui, sans-serif',
          size: 11,
          weight: '500'
        },
        padding: 8
      },
      border: {
        display: false
      }
    },
  },
  elements: {
    point: {
      radius: 4,
      hoverRadius: 6,
      borderWidth: 2,
      hoverBorderWidth: 3
    },
    line: {
      borderWidth: 3,
      tension: 0.4
    },
    bar: {
      borderRadius: 6,
      borderSkipped: false
    }
  },
  animation: {
    duration: 1000,
    easing: 'easeInOutQuart'
  }
}

const createChart = () => {
  if (!chartCanvas.value) return

  const ctx = chartCanvas.value.getContext('2d')
  
  // Destroy existing chart
  if (chartInstance) {
    chartInstance.destroy()
  }

  // Merge options
  const mergedOptions = {
    ...defaultOptions,
    ...props.options
  }

  // Create new chart
  chartInstance = new Chart(ctx, {
    type: props.type,
    data: props.data,
    options: mergedOptions
  })
}

const updateChart = () => {
  if (chartInstance) {
    chartInstance.data = props.data
    chartInstance.update()
  }
}

// Watch for data changes
watch(() => props.data, updateChart, { deep: true })

onMounted(() => {
  createChart()
})

onUnmounted(() => {
  if (chartInstance) {
    chartInstance.destroy()
  }
})
</script>

<style scoped>
.chart-container {
  position: relative;
  height: v-bind(height + 'px');
  width: 100%;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.1) 0%, rgba(255, 255, 255, 0.05) 100%);
  border-radius: 16px;
  padding: 20px;
  box-shadow: 
    0 4px 6px -1px rgba(0, 0, 0, 0.1),
    0 2px 4px -1px rgba(0, 0, 0, 0.06);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.chart-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.05) 0%, rgba(16, 185, 129, 0.05) 100%);
  border-radius: 16px;
  z-index: -1;
}

.chart-container canvas {
  border-radius: 12px;
}
</style>
