<template>
  <ProductVariantSelector
    :variants="variants"
    @variant-selected="handleVariantSelected"
  />
</template>

<script setup>
import ProductVariantSelector from '@/components/product/ProductVariantSelector.vue'
import variantService from '@/services/variantService'
import { ref, onMounted } from 'vue'

const variants = ref([])
const currentPrice = ref(0)
const currentImage = ref('')

onMounted(async () => {
  variants.value = await variantService.getBySanPham(productId)
})

const handleVariantSelected = (variant) => {
  // variant = { id, sku, size, color, stock, price, imageUrl, available }
  currentPrice.value = variant.price || product.price
  currentImage.value = variant.imageUrl || product.image
}
</script>

<style scoped>
.product-variant-selector {
  padding: 1.5rem;
  background: #f8fafb;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.variant-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 1.5rem;
}

.variant-section {
  margin-bottom: 1.5rem;
}

.variant-label {
  display: block;
  font-size: 0.95rem;
  color: #475569;
  margin-bottom: 0.75rem;
}

.variant-label strong {
  color: #1e293b;
}

/* Color Options */
.color-options {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.color-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem;
  background: white;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  min-width: 80px;
}

.color-option:hover {
  border-color: #6366f1;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.15);
}

.color-option.selected {
  border-color: #6366f1;
  background: #eef2ff;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.color-swatch {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border: 3px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.color-option.selected .color-swatch {
  border-color: #6366f1;
}

.color-swatch i {
  color: white;
  font-size: 1.5rem;
  font-weight: bold;
  text-shadow: 0 0 4px rgba(0, 0, 0, 0.5);
}

.color-name {
  font-size: 0.85rem;
  color: #475569;
  font-weight: 500;
  text-align: center;
}

.color-option.selected .color-name {
  color: #4338ca;
  font-weight: 600;
}

/* Size Options */
.size-options {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.size-option {
  min-width: 60px;
  padding: 0.75rem 1rem;
  background: white;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-weight: 600;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s ease;
}

.size-option:hover:not(.disabled) {
  border-color: #6366f1;
  color: #4338ca;
  transform: translateY(-2px);
}

.size-option.selected {
  border-color: #6366f1;
  background: #6366f1;
  color: white;
}

.size-option.disabled {
  opacity: 0.4;
  cursor: not-allowed;
  background: #f1f5f9;
  text-decoration: line-through;
}

/* Selected Variant Info */
.selected-variant-info {
  margin-top: 1.5rem;
  padding: 1rem;
  background: white;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.variant-info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 1rem;
  margin-bottom: 1rem;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.info-label {
  font-size: 0.85rem;
  color: #64748b;
}

.info-value {
  font-size: 1rem;
  font-weight: 600;
  color: #1e293b;
}

.info-value.price {
  font-size: 1.25rem;
  color: #6366f1;
}

.variant-image {
  margin-top: 1rem;
  border-radius: 8px;
  overflow: hidden;
  max-width: 200px;
}

.variant-image img {
  width: 100%;
  height: auto;
  display: block;
}

/* Material Info */
.material-info {
  padding: 0.75rem 1rem;
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: 8px;
  color: #1e40af;
  font-size: 0.9rem;
  margin-top: 1rem;
}

/* Alert */
.alert {
  padding: 0.75rem 1rem;
  border-radius: 8px;
  font-size: 0.9rem;
  margin-top: 1rem;
}

.alert-warning {
  background: #fffbeb;
  border: 1px solid #fde047;
  color: #854d0e;
}

/* Responsive */
@media (max-width: 768px) {
  .product-variant-selector {
    padding: 1rem;
  }

  .color-options {
    gap: 0.5rem;
  }

  .color-option {
    min-width: 70px;
    padding: 0.5rem;
  }

  .color-swatch {
    width: 40px;
    height: 40px;
  }

  .size-option {
    min-width: 50px;
    padding: 0.6rem 0.8rem;
  }

  .variant-info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
