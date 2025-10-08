<template>
  <div 
    class="tooltip-wrapper"
    @mouseenter="handleMouseEnter"
    @mouseleave="handleMouseLeave"
    @focus="handleFocus"
    @blur="handleBlur"
  >
    <!-- Trigger element -->
    <div ref="triggerRef" class="tooltip-trigger">
      <slot />
    </div>

    <!-- Tooltip content -->
    <Teleport to="body">
      <Transition name="tooltip-fade">
        <div
          v-if="isVisible"
          ref="tooltipRef"
          class="tooltip-content"
          :class="tooltipClass"
          :style="tooltipStyle"
          role="tooltip"
        >
          <div class="tooltip-inner">
            <slot name="content">
              {{ content }}
            </slot>
          </div>
          <div 
            v-if="showArrow"
            class="tooltip-arrow"
            :class="`tooltip-arrow-${placement}`"
          ></div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onUnmounted } from 'vue'

const props = defineProps({
  // Content
  content: {
    type: String,
    default: ''
  },
  
  // Placement
  placement: {
    type: String,
    default: 'top',
    validator: (value) => [
      'top', 'top-start', 'top-end',
      'bottom', 'bottom-start', 'bottom-end',
      'left', 'left-start', 'left-end',
      'right', 'right-start', 'right-end'
    ].includes(value)
  },
  
  // Trigger
  trigger: {
    type: String,
    default: 'hover',
    validator: (value) => ['hover', 'click', 'focus', 'manual'].includes(value)
  },
  
  // Show arrow
  showArrow: {
    type: Boolean,
    default: true
  },
  
  // Delay
  showDelay: {
    type: Number,
    default: 0
  },
  hideDelay: {
    type: Number,
    default: 0
  },
  
  // Disabled
  disabled: {
    type: Boolean,
    default: false
  },
  
  // Offset
  offset: {
    type: Number,
    default: 8
  },
  
  // Theme
  theme: {
    type: String,
    default: 'dark',
    validator: (value) => ['dark', 'light'].includes(value)
  },
  
  // Max width
  maxWidth: {
    type: [String, Number],
    default: 200
  }
})

const emit = defineEmits(['show', 'hide'])

// Refs
const triggerRef = ref(null)
const tooltipRef = ref(null)
const isVisible = ref(false)
const showTimer = ref(null)
const hideTimer = ref(null)
const tooltipPosition = ref({ top: 0, left: 0 })

// Computed
const tooltipClass = computed(() => [
  'tooltip-content',
  `tooltip-${props.theme}`,
  `tooltip-placement-${props.placement}`
])

const tooltipStyle = computed(() => ({
  top: `${tooltipPosition.value.top}px`,
  left: `${tooltipPosition.value.left}px`,
  maxWidth: typeof props.maxWidth === 'number' ? `${props.maxWidth}px` : props.maxWidth
}))

// Methods
const calculatePosition = () => {
  if (!triggerRef.value || !tooltipRef.value) return

  const triggerRect = triggerRef.value.getBoundingClientRect()
  const tooltipRect = tooltipRef.value.getBoundingClientRect()
  const scrollX = window.pageXOffset || document.documentElement.scrollLeft
  const scrollY = window.pageYOffset || document.documentElement.scrollTop

  let top = 0
  let left = 0

  const arrowSize = props.showArrow ? 6 : 0
  const offset = props.offset + arrowSize

  // Calculate position based on placement
  switch (props.placement) {
    case 'top':
      top = triggerRect.top + scrollY - tooltipRect.height - offset
      left = triggerRect.left + scrollX + (triggerRect.width - tooltipRect.width) / 2
      break
    case 'top-start':
      top = triggerRect.top + scrollY - tooltipRect.height - offset
      left = triggerRect.left + scrollX
      break
    case 'top-end':
      top = triggerRect.top + scrollY - tooltipRect.height - offset
      left = triggerRect.right + scrollX - tooltipRect.width
      break
    case 'bottom':
      top = triggerRect.bottom + scrollY + offset
      left = triggerRect.left + scrollX + (triggerRect.width - tooltipRect.width) / 2
      break
    case 'bottom-start':
      top = triggerRect.bottom + scrollY + offset
      left = triggerRect.left + scrollX
      break
    case 'bottom-end':
      top = triggerRect.bottom + scrollY + offset
      left = triggerRect.right + scrollX - tooltipRect.width
      break
    case 'left':
      top = triggerRect.top + scrollY + (triggerRect.height - tooltipRect.height) / 2
      left = triggerRect.left + scrollX - tooltipRect.width - offset
      break
    case 'left-start':
      top = triggerRect.top + scrollY
      left = triggerRect.left + scrollX - tooltipRect.width - offset
      break
    case 'left-end':
      top = triggerRect.bottom + scrollY - tooltipRect.height
      left = triggerRect.left + scrollX - tooltipRect.width - offset
      break
    case 'right':
      top = triggerRect.top + scrollY + (triggerRect.height - tooltipRect.height) / 2
      left = triggerRect.right + scrollX + offset
      break
    case 'right-start':
      top = triggerRect.top + scrollY
      left = triggerRect.right + scrollX + offset
      break
    case 'right-end':
      top = triggerRect.bottom + scrollY - tooltipRect.height
      left = triggerRect.right + scrollX + offset
      break
  }

  // Viewport constraints
  const viewportWidth = window.innerWidth
  const viewportHeight = window.innerHeight

  if (left < 0) left = 0
  if (left + tooltipRect.width > viewportWidth) {
    left = viewportWidth - tooltipRect.width - 10
  }
  if (top < 0) top = 0
  if (top + tooltipRect.height > viewportHeight + scrollY) {
    top = viewportHeight + scrollY - tooltipRect.height - 10
  }

  tooltipPosition.value = { top, left }
}

const show = () => {
  if (props.disabled) return

  clearTimeout(hideTimer.value)

  if (props.showDelay > 0) {
    showTimer.value = setTimeout(() => {
      isVisible.value = true
      emit('show')
      nextTick(calculatePosition)
    }, props.showDelay)
  } else {
    isVisible.value = true
    emit('show')
    nextTick(calculatePosition)
  }
}

const hide = () => {
  clearTimeout(showTimer.value)

  if (props.hideDelay > 0) {
    hideTimer.value = setTimeout(() => {
      isVisible.value = false
      emit('hide')
    }, props.hideDelay)
  } else {
    isVisible.value = false
    emit('hide')
  }
}

const handleMouseEnter = () => {
  if (props.trigger === 'hover') {
    show()
  }
}

const handleMouseLeave = () => {
  if (props.trigger === 'hover') {
    hide()
  }
}

const handleFocus = () => {
  if (props.trigger === 'focus') {
    show()
  }
}

const handleBlur = () => {
  if (props.trigger === 'focus') {
    hide()
  }
}

// Watch for visibility changes
watch(isVisible, (newValue) => {
  if (newValue) {
    // Recalculate position on window resize
    window.addEventListener('resize', calculatePosition)
    window.addEventListener('scroll', calculatePosition, true)
  } else {
    window.removeEventListener('resize', calculatePosition)
    window.removeEventListener('scroll', calculatePosition, true)
  }
})

// Cleanup
onUnmounted(() => {
  clearTimeout(showTimer.value)
  clearTimeout(hideTimer.value)
  window.removeEventListener('resize', calculatePosition)
  window.removeEventListener('scroll', calculatePosition, true)
})

// Expose methods
defineExpose({
  show,
  hide,
  isVisible
})
</script>

<style scoped>
.tooltip-wrapper {
  display: inline-block;
}

.tooltip-trigger {
  display: inline-block;
  cursor: help;
}

.tooltip-content {
  position: absolute;
  z-index: 10000;
  padding: 0.5rem 0.75rem;
  font-size: 0.875rem;
  line-height: 1.25rem;
  border-radius: 0.375rem;
  pointer-events: none;
  word-wrap: break-word;
}

.tooltip-inner {
  position: relative;
  z-index: 1;
}

.tooltip-dark {
  background-color: #1f2937;
  color: #ffffff;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}

.tooltip-light {
  background-color: #ffffff;
  color: #1f2937;
  border: 1px solid #e5e7eb;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}

.tooltip-arrow {
  position: absolute;
  width: 0;
  height: 0;
  border-style: solid;
}

.tooltip-dark .tooltip-arrow-top,
.tooltip-dark .tooltip-arrow-top-start,
.tooltip-dark .tooltip-arrow-top-end {
  bottom: -6px;
  border-width: 6px 6px 0 6px;
  border-color: #1f2937 transparent transparent transparent;
}

.tooltip-dark .tooltip-arrow-bottom,
.tooltip-dark .tooltip-arrow-bottom-start,
.tooltip-dark .tooltip-arrow-bottom-end {
  top: -6px;
  border-width: 0 6px 6px 6px;
  border-color: transparent transparent #1f2937 transparent;
}

.tooltip-dark .tooltip-arrow-left,
.tooltip-dark .tooltip-arrow-left-start,
.tooltip-dark .tooltip-arrow-left-end {
  right: -6px;
  border-width: 6px 0 6px 6px;
  border-color: transparent transparent transparent #1f2937;
}

.tooltip-dark .tooltip-arrow-right,
.tooltip-dark .tooltip-arrow-right-start,
.tooltip-dark .tooltip-arrow-right-end {
  left: -6px;
  border-width: 6px 6px 6px 0;
  border-color: transparent #1f2937 transparent transparent;
}

.tooltip-light .tooltip-arrow-top,
.tooltip-light .tooltip-arrow-top-start,
.tooltip-light .tooltip-arrow-top-end {
  bottom: -7px;
  border-width: 6px 6px 0 6px;
  border-color: #ffffff transparent transparent transparent;
}

.tooltip-light .tooltip-arrow-bottom,
.tooltip-light .tooltip-arrow-bottom-start,
.tooltip-light .tooltip-arrow-bottom-end {
  top: -7px;
  border-width: 0 6px 6px 6px;
  border-color: transparent transparent #ffffff transparent;
}

.tooltip-light .tooltip-arrow-left,
.tooltip-light .tooltip-arrow-left-start,
.tooltip-light .tooltip-arrow-left-end {
  right: -7px;
  border-width: 6px 0 6px 6px;
  border-color: transparent transparent transparent #ffffff;
}

.tooltip-light .tooltip-arrow-right,
.tooltip-light .tooltip-arrow-right-start,
.tooltip-light .tooltip-arrow-right-end {
  left: -7px;
  border-width: 6px 6px 6px 0;
  border-color: transparent #ffffff transparent transparent;
}

/* Arrow positioning */
.tooltip-arrow-top,
.tooltip-arrow-bottom {
  left: 50%;
  transform: translateX(-50%);
}

.tooltip-arrow-top-start,
.tooltip-arrow-bottom-start {
  left: 12px;
}

.tooltip-arrow-top-end,
.tooltip-arrow-bottom-end {
  right: 12px;
}

.tooltip-arrow-left,
.tooltip-arrow-right {
  top: 50%;
  transform: translateY(-50%);
}

.tooltip-arrow-left-start,
.tooltip-arrow-right-start {
  top: 12px;
}

.tooltip-arrow-left-end,
.tooltip-arrow-right-end {
  bottom: 12px;
}

/* Transitions */
.tooltip-fade-enter-active,
.tooltip-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.tooltip-fade-enter-from,
.tooltip-fade-leave-to {
  opacity: 0;
}

.tooltip-placement-top.tooltip-fade-enter-from,
.tooltip-placement-top-start.tooltip-fade-enter-from,
.tooltip-placement-top-end.tooltip-fade-enter-from {
  transform: translateY(4px);
}

.tooltip-placement-bottom.tooltip-fade-enter-from,
.tooltip-placement-bottom-start.tooltip-fade-enter-from,
.tooltip-placement-bottom-end.tooltip-fade-enter-from {
  transform: translateY(-4px);
}

.tooltip-placement-left.tooltip-fade-enter-from,
.tooltip-placement-left-start.tooltip-fade-enter-from,
.tooltip-placement-left-end.tooltip-fade-enter-from {
  transform: translateX(4px);
}

.tooltip-placement-right.tooltip-fade-enter-from,
.tooltip-placement-right-start.tooltip-fade-enter-from,
.tooltip-placement-right-end.tooltip-fade-enter-from {
  transform: translateX(-4px);
}

/* Responsive */
@media (max-width: 640px) {
  .tooltip-content {
    max-width: 200px;
    font-size: 0.8125rem;
    padding: 0.375rem 0.625rem;
  }
}
</style>
