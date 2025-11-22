<script setup>
import { ref, onMounted } from 'vue'
// sửa: import sanPhamService và ProductCard
import sanPhamService from '@/services/sanPhamService'
import ProductCard from '@/components/product/ProductCard.vue'

const loading = ref(false)
const error = ref('')
const items = ref([])              // content từ server
const page = ref(0)                // pageNumber (0-based)
const size = ref(12)               // pageSize
const totalPages = ref(0)
const totalElements = ref(0)

async function load() {
  loading.value = true
  error.value = ''
  try {
    const res = await sanPhamService.page({ page: page.value, size: size.value })
    // res có dạng: { content, totalPages, totalElements, ... }
    items.value = res?.content || res || []
    totalPages.value = res?.totalPages ?? 0
    totalElements.value = res?.totalElements ?? (Array.isArray(res) ? res.length : 0)
  } catch (e) {
    error.value = e?.message || 'Không tải được danh sách sản phẩm'
  } finally {
    loading.value = false
  }
}

function nextPage() {
  if (page.value + 1 < totalPages.value) {
    page.value++
    load()
  }
}
function prevPage() {
  if (page.value > 0) {
    page.value--
    load()
  }
}

onMounted(load)
</script>

<template>
  <div class="products">
    <div v-if="error" class="alert alert-danger">{{ error }}</div>

    <div v-if="loading" class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
      <div v-for="n in size" :key="n" class="animate-pulse h-64 rounded-xl bg-gray-200"></div>
    </div>

    <div v-else>
      <div v-if="items.length" class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
        <!-- mỗi sản phẩm 1 card -->
        <ProductCard
          v-for="p in items"
          :key="p.id || p._id"
          :id="p.id"
          :name="p.ten || p.name"
          :img="p.anhDaiDien || p.image"
          :price-now="p.gia || p.price"
          :price-old="p.giaGoc || p.originalPrice"
          :discount="p.giamGia || p.discount"
          :promotional-badge="p.promotionalBadge"
          :color-options="p.colorOptions || p.mauSac"
          :sizes="p.sizes || p.kichCo"
          :rating="p.danhGia !== null && p.danhGia !== undefined ? p.danhGia : null"
          :review-count="p.soLuongDanhGia !== null && p.soLuongDanhGia !== undefined ? p.soLuongDanhGia : null"
        />
      </div>

      <div v-else class="text-center py-10 text-gray-500">
        Chưa có sản phẩm nào.
      </div>

      <!-- Pagination -->
      <div class="flex items-center justify-center gap-3 mt-6">
        <button class="btn btn-outline" :disabled="page===0" @click="prevPage">Trước</button>
        <span>Trang {{ page + 1 }} / {{ totalPages }}</span>
        <button class="btn btn-outline" :disabled="page + 1 >= totalPages" @click="nextPage">Sau</button>
      </div>

      <!-- tổng số sp (tuỳ chọn) -->
      <p class="text-center text-sm mt-2 text-gray-500">
        Tổng: {{ totalElements }} sản phẩm
      </p>
    </div>
  </div>
</template>

<style scoped>
.btn { @apply px-3 py-2 rounded-lg border; }
.btn-outline { @apply border-gray-300 hover:bg-gray-50 disabled:opacity-50; }
</style>
