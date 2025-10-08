<template>
  <button 
    class="btn wishlist-btn"
    :class="buttonClass"
    :title="buttonTitle"
    @click="handleToggle"
    :disabled="disabled"
  >
    <span class="heart-icon">{{ isInWishlist ? '❤️' : '🤍' }}</span>
    <span v-if="showText">{{ buttonText }}</span>
  </button>
</template>

<script setup>
import { computed } from 'vue'
import { useProductStore } from '../../stores/product'

const props = defineProps({
  product: {
    type: Object,
    required: true
  },
  variant: {
    type: String,
    default: 'icon', // 'icon', 'text', 'button'
    validator: (value) => ['icon', 'text', 'button'].includes(value)
  },
  size: {
    type: String,
    default: 'sm',
    validator: (value) => ['sm', 'md', 'lg'].includes(value)
  },
  disabled: {
    type: Boolean,
    default: false
  },
  showText: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['toggle'])

const productStore = useProductStore()

// Computed
const isInWishlist = computed(() => productStore.isInWishlist(props.product.id))

const buttonClass = computed(() => {
  const classes = []
  
  // Base class
  classes.push('wishlist-btn')
  
  // Size
  if (props.size === 'sm') classes.push('btn-sm')
  if (props.size === 'lg') classes.push('btn-lg')
  
  // Variant
  if (props.variant === 'icon') {
    classes.push('btn-outline-secondary')
  } else if (props.variant === 'text') {
    classes.push('btn-link', 'text-decoration-none', 'p-0')
  } else {
    classes.push('btn-outline-secondary')
  }
  
  // Active state
  if (isInWishlist.value) {
    classes.push('active')
  }
  
  return classes.join(' ')
})


const buttonTitle = computed(() => {
  return isInWishlist.value 
    ? `Xóa "${props.product.name}" khỏi danh sách yêu thích`
    : `Thêm "${props.product.name}" vào danh sách yêu thích`
})

const buttonText = computed(() => {
  return isInWishlist.value ? 'Đã yêu thích' : 'Yêu thích'
})

// Methods
const handleToggle = () => {
  if (props.disabled) return
  
  const success = productStore.toggleWishlist(props.product)
  emit('toggle', { product: props.product, added: success })
}
</script>

<style scoped>
.wishlist-btn {
  transition: all 0.3s ease;
  border-radius: 8px;
  position: relative;
  overflow: hidden;
  border: 2px solid #dee2e6 !important;
  font-weight: 500;
}

.wishlist-btn:hover {
  transform: translateY(-1px);
}

.wishlist-btn.active {
  background-color: rgba(220, 53, 69, 0.1);
  border-color: #dc3545 !important;
  color: #dc3545;
}

.wishlist-btn.active:hover {
  background-color: rgba(220, 53, 69, 0.2);
  border-color: #dc3545 !important;
  color: #dc3545;
}

/* Button variant specific styling */
.wishlist-btn.btn-outline-secondary {
  border: 2px solid #dee2e6 !important;
  color: #6c757d;
  background-color: transparent;
  padding: 12px 20px;
}

.wishlist-btn.btn-outline-secondary:hover {
  border: 2px solid #dc3545 !important;
  color: #dc3545;
  background-color: rgba(220, 53, 69, 0.05);
}

.wishlist-btn.btn-outline-secondary.active {
  border: 2px solid #dc3545 !important;
  background-color: rgba(220, 53, 69, 0.1);
  color: #dc3545;
}

.wishlist-btn.btn-link {
  color: #6c757d;
}

.wishlist-btn.btn-link:hover {
  color: #dc3545;
}

.wishlist-btn.btn-link.active {
  color: #dc3545;
}

/* Heart icon styling */
.heart-icon {
  font-size: 1.3em;
  display: inline-block;
  margin-right: 8px;
  transition: all 0.3s ease;
  user-select: none;
}

.wishlist-btn:hover .heart-icon {
  transform: scale(1.1);
}

.wishlist-btn.active .heart-icon {
  animation: heartbeat 0.6s ease-in-out;
}

@keyframes heartbeat {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.3);
  }
  100% {
    transform: scale(1);
  }
}

/* Pulse effect for new additions */
.wishlist-btn.pulse {
  animation: pulse 0.6s ease-in-out;
}

@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
  100% {
    transform: scale(1);
  }
}

/* Disabled state */
.wishlist-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none !important;
}

.wishlist-btn:disabled:hover {
  transform: none !important;
}
</style>
