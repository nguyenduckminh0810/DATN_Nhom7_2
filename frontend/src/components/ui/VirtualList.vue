<template>
  <div 
    ref="containerRef"
    class="virtual-list-container"
    :style="containerStyle"
    @scroll="handleScroll"
  >
    <!-- Spacer for items before visible range -->
    <div :style="{ height: `${offsetY}px` }"></div>
    
    <!-- Visible items -->
    <div
      v-for="item in visibleItems"
      :key="getItemKey(item)"
      class="virtual-list-item"
      :style="{ height: `${itemHeight}px` }"
    >
      <slot 
        :item="item" 
        :index="item.index"
        :isScrolling="isScrolling"
      />
    </div>
    
    <!-- Spacer for items after visible range -->
    <div :style="{ height: `${endSpacerHeight}px` }"></div>
    
    <!-- Loading indicator -->
    <div 
      v-if="loading && visibleItems.length > 0"
      class="virtual-list-loading"
    >
      <div class="loading-spinner">
        <div class="spinner"></div>
        <span>{{ loadingText }}</span>
      </div>
    </div>
    
    <!-- Empty state -->
    <div 
      v-if="!loading && items.length === 0"
      class="virtual-list-empty"
    >
      <div class="empty-content">
        <i :class="emptyIcon"></i>
        <span>{{ emptyText }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'

const props = defineProps({
  // Data
  items: {
    type: Array,
    default: () => []
  },
  
  // Configuration
  itemHeight: {
    type: Number,
    default: 50
  },
  containerHeight: {
    type: Number,
    default: 400
  },
  overscan: {
    type: Number,
    default: 5
  },
  
  // Loading
  loading: {
    type: Boolean,
    default: false
  },
  loadingText: {
    type: String,
    default: 'Đang tải...'
  },
  
  // Empty state
  emptyText: {
    type: String,
    default: 'Không có dữ liệu'
  },
  emptyIcon: {
    type: String,
    default: 'bi bi-folder-open'
  },
  
  // Key generation
  itemKey: {
    type: [String, Function],
    default: 'id'
  },
  
  // Styling
  gap: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['scroll', 'load-more', 'item-click'])

// Refs
const containerRef = ref(null)

// State
const scrollTop = ref(0)
const isScrolling = ref(false)
const scrollTimeout = ref(null)

// Computed
const totalHeight = computed(() => {
  return props.items.length * props.itemHeight
})

const visibleCount = computed(() => {
  return Math.ceil(props.containerHeight / props.itemHeight) + props.overscan * 2
})

const startIndex = computed(() => {
  const index = Math.floor(scrollTop.value / props.itemHeight)
  return Math.max(0, index - props.overscan)
})

const endIndex = computed(() => {
  const index = startIndex.value + visibleCount.value
  return Math.min(props.items.length - 1, index)
})

const visibleItems = computed(() => {
  const items = []
  for (let i = startIndex.value; i <= endIndex.value; i++) {
    if (props.items[i]) {
      items.push({
        ...props.items[i],
        index: i
      })
    }
  }
  return items
})

const offsetY = computed(() => {
  return startIndex.value * props.itemHeight
})

const endSpacerHeight = computed(() => {
  return (props.items.length - endIndex.value - 1) * props.itemHeight
})

const containerStyle = computed(() => ({
  height: `${props.containerHeight}px`,
  overflowY: 'auto'
}))

// Methods
const getItemKey = (item) => {
  if (typeof props.itemKey === 'function') {
    return props.itemKey(item)
  }
  return item[props.itemKey] || item.index
}

const handleScroll = (event) => {
  scrollTop.value = event.target.scrollTop
  
  // Set scrolling state
  isScrolling.value = true
  clearTimeout(scrollTimeout.value)
  scrollTimeout.value = setTimeout(() => {
    isScrolling.value = false
  }, 150)
  
  emit('scroll', {
    scrollTop: scrollTop.value,
    scrollHeight: event.target.scrollHeight,
    clientHeight: event.target.clientHeight
  })
  
  // Check if we need to load more items
  const { scrollTop: top, scrollHeight, clientHeight } = event.target
  if (scrollHeight - top - clientHeight < props.itemHeight * props.overscan) {
    emit('load-more')
  }
}

const scrollToIndex = (index) => {
  if (containerRef.value) {
    const targetScrollTop = index * props.itemHeight
    containerRef.value.scrollTop = targetScrollTop
  }
}

const scrollToTop = () => {
  scrollToIndex(0)
}

const scrollToBottom = () => {
  scrollToIndex(props.items.length - 1)
}

// Lifecycle
onMounted(() => {
  nextTick(() => {
    // Initial scroll position
    if (containerRef.value) {
      scrollTop.value = containerRef.value.scrollTop
    }
  })
})

onUnmounted(() => {
  if (scrollTimeout.value) {
    clearTimeout(scrollTimeout.value)
  }
})

// Expose methods
defineExpose({
  scrollToIndex,
  scrollToTop,
  scrollToBottom,
  scrollTop: computed(() => scrollTop.value)
})
</script>

<style scoped>
.virtual-list-container {
  position: relative;
  overflow-y: auto;
  overflow-x: hidden;
}

.virtual-list-item {
  position: relative;
  width: 100%;
  display: flex;
  align-items: center;
  border-bottom: 1px solid #e5e7eb;
}

.virtual-list-item:last-child {
  border-bottom: none;
}

.virtual-list-loading {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 2rem;
  color: #6b7280;
}

.loading-spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.spinner {
  width: 1.5rem;
  height: 1.5rem;
  border: 2px solid #e5e7eb;
  border-top: 2px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.virtual-list-empty {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
  color: #9ca3af;
}

.empty-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  text-align: center;
}

.empty-content i {
  font-size: 2rem;
  opacity: 0.5;
}

.empty-content span {
  font-size: 0.875rem;
}

/* Scrollbar styling */
.virtual-list-container::-webkit-scrollbar {
  width: 6px;
}

.virtual-list-container::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 3px;
}

.virtual-list-container::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

.virtual-list-container::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

/* Responsive */
@media (max-width: 640px) {
  .virtual-list-container::-webkit-scrollbar {
    width: 4px;
  }
  
  .loading-spinner {
    padding: 1rem;
  }
  
  .spinner {
    width: 1.25rem;
    height: 1.25rem;
  }
}
</style>
