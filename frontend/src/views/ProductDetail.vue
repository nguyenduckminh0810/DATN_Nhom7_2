<template>
  <div class="product-detail">
    <div class="container py-4">
      <!-- Breadcrumb -->
      <nav aria-label="breadcrumb">
        <ol class="breadcrumb">
          <li class="breadcrumb-item">
            <router-link to="/">Trang chủ</router-link>
          </li>
          <li class="breadcrumb-item">
            <router-link to="/category">Sản phẩm</router-link>
          </li>
          <li class="breadcrumb-item active" aria-current="page">
            {{ product.name }}
          </li>
        </ol>
      </nav>

      <div class="row">
        <!-- Product Images -->
        <div class="col-lg-6 mb-4">
          <div class="product-images">
            <div class="main-image mb-3">
              <img :src="selectedImage" :alt="product.name" class="img-fluid rounded shadow">
            </div>
            <div class="thumbnail-images d-flex gap-2">
              <img 
                v-for="(image, index) in product.images" 
                :key="index"
                :src="image" 
                :alt="product.name"
                class="img-thumbnail cursor-pointer"
                :class="{ active: image === selectedImage }"
                @click="selectedImage = image"
                style="width: 80px; height: 80px; object-fit: cover;"
              >
            </div>
          </div>
        </div>

        <!-- Product Info -->
        <div class="col-lg-6">
          <div class="product-info">
            <h1 class="h2 fw-bold mb-3">{{ product.name }}</h1>
            
            <!-- Price -->
            <div class="price-section mb-4">
              <span class="h3 text-warning fw-bold">{{ formatPrice(product.price) }}</span>
              <span v-if="product.originalPrice" class="text-muted text-decoration-line-through ms-2">
                {{ formatPrice(product.originalPrice) }}
              </span>
              <span v-if="product.discount" class="badge bg-danger ms-2">
                -{{ product.discount }}%
              </span>
            </div>

            <!-- Description -->
            <div class="description mb-4">
              <p class="text-muted">{{ product.description }}</p>
            </div>

            <!-- Variants -->
            <div class="variants mb-4">
              <!-- Size Selection -->
              <div class="mb-3">
                <label class="form-label fw-bold">Kích thước:</label>
                <div class="d-flex gap-2 flex-wrap">
                  <button 
                    v-for="size in product.sizes" 
                    :key="size"
                    class="btn btn-outline-secondary"
                    :class="{ 'btn-warning': selectedSize === size }"
                    @click="selectedSize = size"
                  >
                    {{ size }}
                  </button>
                </div>
              </div>

              <!-- Color Selection -->
              <div class="mb-3">
                <label class="form-label fw-bold">Màu sắc:</label>
                <div class="d-flex gap-2 flex-wrap">
                  <button 
                    v-for="color in product.colors" 
                    :key="color"
                    class="btn btn-outline-secondary"
                    :class="{ 'btn-warning': selectedColor === color }"
                    @click="selectedColor = color"
                  >
                    {{ color }}
                  </button>
                </div>
              </div>
            </div>

            <!-- Quantity -->
            <div class="quantity-section mb-4">
              <label class="form-label fw-bold">Số lượng:</label>
              <div class="d-flex align-items-center gap-3">
                <div class="input-group" style="width: 120px;">
                  <button class="btn btn-outline-secondary" @click="decreaseQuantity">-</button>
                  <input 
                    v-model.number="quantity" 
                    type="number" 
                    class="form-control text-center" 
                    min="1" 
                    :max="product.stock"
                  >
                  <button class="btn btn-outline-secondary" @click="increaseQuantity">+</button>
                </div>
                <small class="text-muted">
                  Còn {{ product.stock }} sản phẩm
                </small>
              </div>
            </div>

            <!-- Actions -->
            <div class="actions d-flex gap-3 mb-4">
              <button 
                class="btn btn-warning btn-lg flex-grow-1"
                @click="addToCart"
                :disabled="!selectedSize || !selectedColor"
              >
                <i class="bi bi-cart-plus me-2"></i>
                Thêm vào giỏ hàng
              </button>
              <button class="btn btn-outline-danger btn-lg">
                <i class="bi bi-heart"></i>
              </button>
            </div>

            <!-- Product Features -->
            <div class="product-features">
              <div class="row g-3">
                <div class="col-6">
                  <div class="d-flex align-items-center">
                    <i class="bi bi-truck text-warning me-2"></i>
                    <small>Miễn phí vận chuyển</small>
                  </div>
                </div>
                <div class="col-6">
                  <div class="d-flex align-items-center">
                    <i class="bi bi-arrow-clockwise text-warning me-2"></i>
                    <small>Đổi trả 30 ngày</small>
                  </div>
                </div>
                <div class="col-6">
                  <div class="d-flex align-items-center">
                    <i class="bi bi-shield-check text-warning me-2"></i>
                    <small>Bảo hành chính hãng</small>
                  </div>
                </div>
                <div class="col-6">
                  <div class="d-flex align-items-center">
                    <i class="bi bi-headset text-warning me-2"></i>
                    <small>Hỗ trợ 24/7</small>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Product Details Tabs -->
      <div class="row mt-5">
        <div class="col-12">
          <ul class="nav nav-tabs" role="tablist">
            <li class="nav-item" role="presentation">
              <button class="nav-link active" data-bs-toggle="tab" data-bs-target="#description" type="button">
                Mô tả sản phẩm
              </button>
            </li>
            <li class="nav-item" role="presentation">
              <button class="nav-link" data-bs-toggle="tab" data-bs-target="#specifications" type="button">
                Thông số kỹ thuật
              </button>
            </li>
            <li class="nav-item" role="presentation">
              <button class="nav-link" data-bs-toggle="tab" data-bs-target="#reviews" type="button">
                Đánh giá ({{ product.reviews?.length || 0 }})
              </button>
            </li>
          </ul>
          
          <div class="tab-content p-4 border border-top-0 rounded-bottom">
            <div class="tab-pane fade show active" id="description">
              <h5>Mô tả chi tiết</h5>
              <p>{{ product.detailedDescription || product.description }}</p>
            </div>
            
            <div class="tab-pane fade" id="specifications">
              <h5>Thông số kỹ thuật</h5>
              <table class="table table-striped">
                <tbody>
                  <tr v-for="spec in product.specifications" :key="spec.name">
                    <td class="fw-bold">{{ spec.name }}</td>
                    <td>{{ spec.value }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
            
            <div class="tab-pane fade" id="reviews">
              <h5>Đánh giá sản phẩm</h5>
              <div v-if="product.reviews && product.reviews.length > 0">
                <div v-for="review in product.reviews" :key="review.id" class="review-item mb-3 p-3 border rounded">
                  <div class="d-flex justify-content-between align-items-start">
                    <div>
                      <h6 class="mb-1">{{ review.userName }}</h6>
                      <div class="rating mb-2">
                        <i v-for="i in 5" :key="i" 
                           class="bi bi-star-fill text-warning" 
                           :class="{ 'text-muted': i > review.rating }">
                        </i>
                      </div>
                    </div>
                    <small class="text-muted">{{ formatDate(review.date) }}</small>
                  </div>
                  <p class="mb-0">{{ review.comment }}</p>
                </div>
              </div>
              <div v-else class="text-center py-4">
                <p class="text-muted">Chưa có đánh giá nào cho sản phẩm này.</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Related Products -->
      <div class="row mt-5">
        <div class="col-12">
          <h4 class="fw-bold mb-4">Sản phẩm liên quan</h4>
          <div class="row g-4">
            <div class="col-md-6 col-lg-3" v-for="relatedProduct in relatedProducts" :key="relatedProduct.id">
              <div class="card product-card h-100">
                <img :src="relatedProduct.image" class="card-img-top" :alt="relatedProduct.name">
                <div class="card-body d-flex flex-column">
                  <h6 class="card-title">{{ relatedProduct.name }}</h6>
                  <p class="card-text text-muted small flex-grow-1">{{ relatedProduct.description }}</p>
                  <div class="d-flex justify-content-between align-items-center">
                    <span class="price">{{ formatPrice(relatedProduct.price) }}</span>
                    <router-link :to="`/product/${relatedProduct.id}`" class="btn btn-sm btn-warning">
                      <i class="bi bi-eye"></i>
                    </router-link>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useCartStore } from '../stores/cart'

const route = useRoute()
const cartStore = useCartStore()

// Reactive data
const product = ref({})
const selectedImage = ref('')
const selectedSize = ref('')
const selectedColor = ref('')
const quantity = ref(1)

// Mock data
const mockProduct = {
  id: 1,
  name: 'Áo sơ mi nam cao cấp',
  description: 'Áo sơ mi nam chất liệu cotton 100% với thiết kế tinh tế và phong cách hiện đại.',
  detailedDescription: 'Áo sơ mi nam cao cấp được làm từ 100% cotton chất lượng cao, mang đến cảm giác thoải mái và mát mẻ khi mặc. Thiết kế cổ điển với đường may tinh tế, phù hợp cho cả công sở và các dịp đặc biệt.',
  price: 450000,
  originalPrice: 600000,
  discount: 25,
  stock: 15,
  images: [
    'https://via.placeholder.com/500x500/6c757d/ffffff?text=Áo+sơ+mi+1',
    'https://via.placeholder.com/500x500/6c757d/ffffff?text=Áo+sơ+mi+2',
    'https://via.placeholder.com/500x500/6c757d/ffffff?text=Áo+sơ+mi+3',
    'https://via.placeholder.com/500x500/6c757d/ffffff?text=Áo+sơ+mi+4'
  ],
  sizes: ['S', 'M', 'L', 'XL', 'XXL'],
  colors: ['Trắng', 'Xanh navy', 'Xám'],
  specifications: [
    { name: 'Chất liệu', value: '100% Cotton' },
    { name: 'Xuất xứ', value: 'Việt Nam' },
    { name: 'Kiểu dáng', value: 'Regular fit' },
    { name: 'Cổ áo', value: 'Cổ bẻ' },
    { name: 'Tay áo', value: 'Dài' }
  ],
  reviews: [
    {
      id: 1,
      userName: 'Nguyễn Văn A',
      rating: 5,
      comment: 'Áo rất đẹp, chất liệu tốt, mặc rất thoải mái.',
      date: '2024-01-15'
    },
    {
      id: 2,
      userName: 'Trần Thị B',
      rating: 4,
      comment: 'Sản phẩm tốt, giao hàng nhanh.',
      date: '2024-01-10'
    }
  ]
}

const relatedProducts = ref([
  {
    id: 2,
    name: 'Áo sơ mi kẻ sọc',
    description: 'Áo sơ mi kẻ sọc phong cách',
    price: 380000,
    image: 'https://via.placeholder.com/300x300/6c757d/ffffff?text=Áo+kẻ+sọc'
  },
  {
    id: 3,
    name: 'Áo sơ mi trắng',
    description: 'Áo sơ mi trắng công sở',
    price: 420000,
    image: 'https://via.placeholder.com/300x300/6c757d/ffffff?text=Áo+trắng'
  },
  {
    id: 4,
    name: 'Áo sơ mi denim',
    description: 'Áo sơ mi denim casual',
    price: 480000,
    image: 'https://via.placeholder.com/300x300/6c757d/ffffff?text=Áo+denim'
  },
  {
    id: 5,
    name: 'Áo sơ mi linen',
    description: 'Áo sơ mi linen mùa hè',
    price: 520000,
    image: 'https://via.placeholder.com/300x300/6c757d/ffffff?text=Áo+linen'
  }
])

// Computed
const canAddToCart = computed(() => {
  return selectedSize.value && selectedColor.value && quantity.value > 0
})

// Methods
const formatPrice = (price) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

const formatDate = (dateString) => {
  return new Date(dateString).toLocaleDateString('vi-VN')
}

const increaseQuantity = () => {
  if (quantity.value < product.value.stock) {
    quantity.value++
  }
}

const decreaseQuantity = () => {
  if (quantity.value > 1) {
    quantity.value--
  }
}

const addToCart = () => {
  if (canAddToCart.value) {
    const cartItem = {
      ...product.value,
      selectedSize: selectedSize.value,
      selectedColor: selectedColor.value,
      quantity: quantity.value
    }
    
    cartStore.addItem(cartItem, quantity.value)
    
    // Show success message
    alert('Đã thêm sản phẩm vào giỏ hàng!')
  }
}

// Lifecycle
onMounted(() => {
  // Load product data based on route params
  const productId = route.params.id
  product.value = mockProduct
  selectedImage.value = product.value.images[0]
  
  // TODO: Load product from API
})
</script>

<style scoped>
.product-card {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  border: none;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 1rem 3rem rgba(0, 0, 0, 0.175);
}

.product-card .card-img-top {
  transition: transform 0.3s ease;
  height: 250px;
  object-fit: cover;
}

.product-card:hover .card-img-top {
  transform: scale(1.05);
}

.price {
  font-size: 1.25rem;
  font-weight: 700;
  color: #ffc107;
}

.cursor-pointer {
  cursor: pointer;
}

.img-thumbnail.active {
  border-color: #ffc107;
  border-width: 2px;
}

.breadcrumb {
  background: none;
  padding: 0;
}

.breadcrumb-item a {
  text-decoration: none;
  color: #6c757d;
}

.breadcrumb-item a:hover {
  color: #ffc107;
}

.rating {
  font-size: 0.9rem;
}

.review-item {
  background-color: #f8f9fa;
}

.nav-tabs .nav-link {
  border: none;
  color: #6c757d;
  font-weight: 500;
}

.nav-tabs .nav-link.active {
  color: #ffc107;
  border-bottom: 2px solid #ffc107;
  background: none;
}

.tab-content {
  background-color: #f8f9fa;
}
</style>
