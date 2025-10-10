<template>
  <nav class="breadcrumb-container" aria-label="Breadcrumb">
    <ol class="breadcrumb-list">
      <li v-for="(item, index) in items" :key="index" class="breadcrumb-item">
        <router-link 
          v-if="item.to && index < items.length - 1" 
          :to="item.to" 
          class="breadcrumb-link"
        >
          <i v-if="item.icon" :class="item.icon" class="breadcrumb-icon"></i>
          {{ item.text }}
        </router-link>
        <span v-else class="breadcrumb-current">
          <i v-if="item.icon" :class="item.icon" class="breadcrumb-icon"></i>
          {{ item.text }}
        </span>
        <i v-if="index < items.length - 1" class="bi bi-caret-right breadcrumb-separator"></i>
      </li>
    </ol>
  </nav>
</template>

<script setup>
import { defineProps } from 'vue'

const props = defineProps({
  items: {
    type: Array,
    required: true,
    validator: (items) => {
      return items.every(item => 
        typeof item === 'object' && 
        typeof item.text === 'string' &&
        (item.to === undefined || typeof item.to === 'string' || typeof item.to === 'object')
      )
    }
  }
})
</script>

<style scoped>
.breadcrumb-container {
  margin-bottom: 2rem;
}

.breadcrumb-list {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 0.5rem;
  list-style: none;
  padding: 0;
  margin: 0;
  font-size: 14px;
}

.breadcrumb-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.breadcrumb-link {
  color: var(--auro-text-light);
  text-decoration: none;
  padding: 0.5rem 0.75rem;
  border-radius: 8px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 500;
}

.breadcrumb-link:hover {
  color: var(--auro-accent);
  background: rgba(140, 144, 126, 0.1);
  transform: translateY(-1px);
}

.breadcrumb-current {
  color: var(--auro-text);
  font-weight: 600;
  padding: 0.5rem 0.75rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: rgba(140, 144, 126, 0.1);
  border-radius: 8px;
}

.breadcrumb-icon {
  font-size: 16px;
  flex-shrink: 0;
}

.breadcrumb-separator {
  color: var(--auro-text-light);
  font-size: 12px;
  margin: 0 0.25rem;
}

/* Responsive */
@media (max-width: 768px) {
  .breadcrumb-container {
    margin-bottom: 1rem;
  }
  
  .breadcrumb-list {
    font-size: 13px;
    gap: 0.25rem;
  }
  
  .breadcrumb-link,
  .breadcrumb-current {
    padding: 0.375rem 0.5rem;
  }
  
  .breadcrumb-icon {
    font-size: 14px;
  }
  
  .breadcrumb-separator {
    font-size: 10px;
  }
}

/* Animation for breadcrumb items */
.breadcrumb-item {
  animation: slideInRight 0.3s ease-out;
  animation-fill-mode: both;
}

.breadcrumb-item:nth-child(1) { animation-delay: 0.1s; }
.breadcrumb-item:nth-child(2) { animation-delay: 0.2s; }
.breadcrumb-item:nth-child(3) { animation-delay: 0.3s; }
.breadcrumb-item:nth-child(4) { animation-delay: 0.4s; }
.breadcrumb-item:nth-child(5) { animation-delay: 0.5s; }

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* Dark mode support */
@media (prefers-color-scheme: dark) {
  .breadcrumb-link {
    color: #9ca3af;
  }
  
  .breadcrumb-link:hover {
    color: var(--auro-accent);
    background: rgba(140, 144, 126, 0.2);
  }
  
  .breadcrumb-current {
    color: white;
    background: rgba(140, 144, 126, 0.2);
  }
  
  .breadcrumb-separator {
    color: #6b7280;
  }
}
</style>
