<template>
  <div class="product-reviews">
    <div class="reviews-header">
      <div class="rating-summary">
        <div class="rating-score">
          <span class="score">{{ computedRating }}</span>
          <div class="stars">
            <i v-for="star in 5" :key="star" 
               :class="star <= Math.round(computedRating) ? 'bi bi-star-fill' : 'bi bi-star'"
               class="star-icon"></i>
          </div>
          <span class="review-count">Dựa trên {{ computedReviewCount }} đánh giá</span>
        </div>
      </div>
      
      <div class="rating-breakdown">
        <div class="rating-bar" v-for="rating in 5" :key="rating">
          <span class="rating-label">{{ rating }} sao</span>
          <div class="bar-container">
            <div class="bar-fill" :style="{ width: getRatingPercentage(rating) + '%' }"></div>
          </div>
          <span class="rating-count">{{ getRatingCount(rating) }}</span>
        </div>
      </div>
    </div>

    <div class="reviews-filters">
      <div class="filter-group">
        <label>Sắp xếp theo:</label>
        <select v-model="sortBy" class="filter-select">
          <option value="newest">Mới nhất</option>
          <option value="oldest">Cũ nhất</option>
          <option value="highest">Đánh giá cao nhất</option>
          <option value="lowest">Đánh giá thấp nhất</option>
        </select>
      </div>
      
      <div class="filter-group">
        <label>Phân loại xếp hạng:</label>
        <div class="rating-filters">
          <button 
            v-for="rating in 5" 
            :key="rating"
            class="rating-filter"
            :class="{ active: selectedRating === rating }"
            @click="toggleRatingFilter(rating)"
          >
            {{ rating }} sao
          </button>
        </div>
      </div>
    </div>

    <div class="reviews-list">
      <div v-if="loading" class="text-center py-4">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">Đang tải...</span>
        </div>
        <p class="mt-2 text-muted">Đang tải đánh giá...</p>
      </div>
      <div v-else-if="filteredReviews.length === 0" class="no-reviews">
        <i class="bi bi-chat-dots"></i>
        <h4>Chưa có đánh giá nào</h4>
        <p>Hãy là người đầu tiên đánh giá sản phẩm này!</p>
      </div>
      
      <div v-else>
        <div 
          v-for="review in paginatedReviews" 
          :key="review.id"
          class="review-item"
        >
          <div class="review-header">
            <div class="reviewer-info">
              <div class="reviewer-avatar">
                <img :src="review.avatar" :alt="review.name" />
              </div>
              <div class="reviewer-details">
                <h5 class="reviewer-name">{{ review.name }}</h5>
                <div class="review-rating">
                  <i v-for="star in 5" :key="star" 
                     :class="star <= review.rating ? 'bi bi-star-fill' : 'bi bi-star'"
                     class="star-icon"></i>
                </div>
              </div>
            </div>
            <div class="review-date">{{ formatDate(review.date) }}</div>
          </div>
          
          <div class="review-content">
            <p class="review-text">{{ review.comment }}</p>
            
            <div v-if="review.images && review.images.length > 0" class="review-images">
              <div 
                v-for="(image, index) in review.images" 
                :key="index"
                class="review-image"
                @click="openImageModal(image)"
              >
                <img :src="image" :alt="`Review image ${index + 1}`" />
              </div>
            </div>
            
            <div class="review-actions">
              <button class="action-btn helpful" @click="toggleHelpful(review.id)">
                <i class="bi bi-hand-thumbs-up"></i>
                Hữu ích ({{ review.helpfulCount || 0 }})
              </button>
              <button class="action-btn reply" @click="toggleReply(review.id)">
                <i class="bi bi-chat-dots"></i>
                Trả lời
              </button>
            </div>
          </div>
        </div>
        
        <div v-if="totalPages > 1" class="pagination">
          <button 
            class="page-btn"
            :disabled="currentPage === 1"
            @click="currentPage = Math.max(1, currentPage - 1); loadReviews()"
          >
            Trước
          </button>
          <span class="page-info">Trang {{ currentPage }} / {{ totalPages }}</span>
          <button 
            class="page-btn"
            :disabled="currentPage >= totalPages"
            @click="currentPage = Math.min(totalPages, currentPage + 1); loadReviews()"
          >
            Sau
          </button>
        </div>
      </div>
    </div>

    <!-- Write Review Modal -->
    <div v-if="showWriteReview" class="modal-overlay" @click="closeWriteReview">
      <div class="modal-content" @click.stop>
        <button class="modal-close" @click="closeWriteReview">
          <i class="bi bi-x"></i>
        </button>
        <h3>Viết đánh giá</h3>
        <form @submit.prevent="submitReview">
          <div class="form-group">
            <label>Đánh giá của bạn:</label>
            <div class="rating-input">
              <button 
                v-for="star in 5" 
                :key="star"
                type="button"
                class="rating-star"
                :class="{ active: star <= newReview.rating }"
                @click="newReview.rating = star"
              >
                <i class="bi bi-star-fill"></i>
              </button>
            </div>
          </div>
          
          <div class="form-group">
            <label>Nhận xét:</label>
            <textarea 
              v-model="newReview.comment" 
              placeholder="Chia sẻ trải nghiệm của bạn về sản phẩm..."
              rows="4"
            ></textarea>
          </div>
          
          <div class="form-group">
            <label>Hình ảnh (tùy chọn):</label>
            <input type="file" multiple accept="image/*" @change="handleImageUpload" />
          </div>
          
          <div class="form-actions">
            <button type="button" class="btn btn-secondary" @click="closeWriteReview">
              Hủy
            </button>
            <button type="submit" class="btn btn-primary">
              Gửi đánh giá
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Image Modal -->
    <div v-if="showImageModal" class="image-modal-overlay" @click="closeImageModal">
      <div class="image-modal-content" @click.stop>
        <button class="image-modal-close" @click="closeImageModal">
          <i class="bi bi-x"></i>
        </button>
        <img :src="selectedImage" alt="Review image" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useToast } from '../../composables/useToast'
import productService from '../../services/productService'

const props = defineProps({
  product: {
    type: Object,
    required: true
  }
})

const { showToast } = useToast()

// Reactive data
const reviews = ref([])
const loading = ref(false)
const sortBy = ref('newest')
const selectedRating = ref(null)
const currentPage = ref(1)
const itemsPerPage = 10
const totalPages = ref(1)
const totalItems = ref(0)
const showWriteReview = ref(false)
const showImageModal = ref(false)
const selectedImage = ref('')

const newReview = ref({
  rating: 0,
  comment: '',
  images: []
})

// Computed properties
const filteredReviews = computed(() => {
  let filtered = [...reviews.value]
  
  if (selectedRating.value) {
    filtered = filtered.filter(review => review.rating === selectedRating.value)
  }
  
  switch (sortBy.value) {
    case 'newest':
      filtered.sort((a, b) => new Date(b.date) - new Date(a.date))
      break
    case 'oldest':
      filtered.sort((a, b) => new Date(a.date) - new Date(b.date))
      break
    case 'highest':
      filtered.sort((a, b) => b.rating - a.rating)
      break
    case 'lowest':
      filtered.sort((a, b) => a.rating - b.rating)
      break
  }
  
  return filtered
})

const paginatedReviews = computed(() => {
  return filteredReviews.value
})

// Tính toán rating và reviewCount từ reviews thực tế hoặc từ product prop
const computedRating = computed(() => {
  if (reviews.value.length > 0) {
    const totalRating = reviews.value.reduce((sum, review) => sum + (review.rating || 0), 0)
    const avgRating = totalRating / reviews.value.length
    return Math.round(avgRating * 10) / 10 // Làm tròn 1 chữ số thập phân
  }
  // Fallback về product.rating từ prop nếu chưa load reviews
  return props.product?.rating != null ? Number(props.product.rating) : 0
})

const computedReviewCount = computed(() => {
  // Ưu tiên dùng số lượng reviews đã load (từ totalItems hoặc reviews.length)
  if (totalItems.value > 0) {
    return totalItems.value
  }
  if (reviews.value.length > 0) {
    return reviews.value.length
  }
  // Fallback về product.reviewCount từ prop
  return props.product?.reviewCount != null ? Number(props.product.reviewCount) : 0
})

// Methods
const loadReviews = async () => {
  if (!props.product?.id) {
    return
  }

  try {
    loading.value = true
    const response = await productService.getReviews(props.product.id, {
      page: currentPage.value - 1,
      size: itemsPerPage
    })

    if (response.success && response.data) {
      const data = response.data
      const content = Array.isArray(data.content) ? data.content : (Array.isArray(data) ? data : [])
      
      reviews.value = content.map(review => ({
        id: review.id,
        name: review.khachHangTen || 'Khách hàng',
        avatar: review.khachHangAvatar || 'https://via.placeholder.com/50/6c757d/ffffff?text=U',
        rating: review.soSao || 0,
        comment: review.noiDung || '',
        date: review.taoLuc || review.capNhatLuc || new Date().toISOString(),
        helpfulCount: 0,
        images: []
      }))

      if (data.totalPages !== undefined) {
        totalPages.value = data.totalPages
        totalItems.value = data.totalElements || content.length
      } else {
        totalPages.value = 1
        totalItems.value = content.length
      }
    } else {
      reviews.value = []
    }
  } catch (error) {
    console.error('Error loading reviews:', error)
    reviews.value = []
    showToast('Không thể tải đánh giá', 'error')
  } finally {
    loading.value = false
  }
}

const getRatingPercentage = (rating) => {
  const count = getRatingCount(rating)
  const total = reviews.value.length
  return total > 0 ? (count / total) * 100 : 0
}

const getRatingCount = (rating) => {
  return reviews.value.filter(review => review.rating === rating).length
}

const toggleRatingFilter = (rating) => {
  selectedRating.value = selectedRating.value === rating ? null : rating
  currentPage.value = 1
}

const toggleHelpful = (reviewId) => {
  const review = reviews.value.find(r => r.id === reviewId)
  if (review) {
    review.helpfulCount = (review.helpfulCount || 0) + 1
    showToast('Cảm ơn bạn đã đánh giá hữu ích!', 'success')
  }
}

const toggleReply = (reviewId) => {
  // Implement reply functionality
  showToast('Tính năng trả lời đang được phát triển', 'info')
}

const formatDate = (dateString) => {
  return new Date(dateString).toLocaleDateString('vi-VN')
}

const openImageModal = (image) => {
  selectedImage.value = image
  showImageModal.value = true
}

const closeImageModal = () => {
  showImageModal.value = false
  selectedImage.value = ''
}

const handleImageUpload = (event) => {
  const files = event.target.files
  newReview.value.images = Array.from(files)
}

const submitReview = async () => {
  if (newReview.value.rating === 0) {
    showToast('Vui lòng chọn đánh giá', 'warning')
    return
  }
  
  if (!newReview.value.comment.trim()) {
    showToast('Vui lòng nhập nhận xét', 'warning')
    return
  }
  
  // Note: Đánh giá sản phẩm chỉ có thể thực hiện từ đơn hàng
  // Component này chỉ hiển thị đánh giá, không cho phép tạo mới
  showToast('Vui lòng đánh giá sản phẩm từ trang đơn hàng của bạn', 'info')
  closeWriteReview()
}

const closeWriteReview = () => {
  showWriteReview.value = false
  newReview.value = {
    rating: 0,
    comment: '',
    images: []
  }
}

// Watch for product changes
watch(() => props.product?.id, (newId) => {
  if (newId) {
    currentPage.value = 1
    loadReviews()
  }
}, { immediate: true })

// Lifecycle
onMounted(() => {
  if (props.product?.id) {
    loadReviews()
  }
})
</script>

<style scoped>
.product-reviews {
  padding: 2rem;
}

.reviews-header {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 3rem;
  margin-bottom: 2rem;
  padding: 2rem;
  background: #f8f9fa;
  border-radius: 12px;
}

.rating-summary {
  text-align: center;
}

.rating-score .score {
  font-size: 3rem;
  font-weight: 700;
  color: #2c3e50;
  display: block;
}

.stars {
  display: flex;
  justify-content: center;
  gap: 0.25rem;
  margin: 1rem 0;
}

.star-icon {
  color: #ffc107;
  font-size: 1.5rem;
}

.review-count {
  color: #6c757d;
  font-size: 1.1rem;
}

.rating-breakdown {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.rating-bar {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.rating-label {
  min-width: 60px;
  font-size: 0.9rem;
  color: #495057;
}

.bar-container {
  flex: 1;
  height: 8px;
  background: #e9ecef;
  border-radius: 4px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #ffc107, #ff9800);
  transition: width 0.3s ease;
}

.rating-count {
  min-width: 30px;
  text-align: right;
  font-size: 0.9rem;
  color: #495057;
}

.page-info {
  padding: 0 1rem;
  color: #495057;
  font-size: 0.9rem;
}

.reviews-filters {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding: 1.5rem;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.filter-group label {
  font-weight: 600;
  color: #495057;
}

.filter-select {
  padding: 0.5rem 1rem;
  border: 1px solid #dee2e6;
  border-radius: 6px;
  background: white;
}

.rating-filters {
  display: flex;
  gap: 0.5rem;
}

.rating-filter {
  padding: 0.5rem 1rem;
  border: 1px solid #dee2e6;
  border-radius: 20px;
  background: white;
  color: #495057;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.rating-filter:hover {
  border-color: #007bff;
  color: #007bff;
}

.rating-filter.active {
  background: #007bff;
  border-color: #007bff;
  color: white;
}

.reviews-list {
  background: white;
  border-radius: 8px;
  overflow: hidden;
}

.no-reviews {
  text-align: center;
  padding: 4rem 2rem;
  color: #6c757d;
}

.no-reviews i {
  font-size: 4rem;
  margin-bottom: 1rem;
  display: block;
}

.review-item {
  padding: 2rem;
  border-bottom: 1px solid #e9ecef;
}

.review-item:last-child {
  border-bottom: none;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.reviewer-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.reviewer-avatar img {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
}

.reviewer-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 0.25rem 0;
}

.review-rating {
  display: flex;
  gap: 0.125rem;
}

.review-rating .star-icon {
  font-size: 1rem;
}

.review-date {
  color: #6c757d;
  font-size: 0.9rem;
}

.review-content {
  margin-left: 4rem;
}

.review-text {
  color: #495057;
  line-height: 1.6;
  margin-bottom: 1rem;
}

.review-images {
  display: flex;
  gap: 1rem;
  margin-bottom: 1rem;
}

.review-image {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.review-image:hover {
  transform: scale(1.05);
}

.review-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.review-actions {
  display: flex;
  gap: 1rem;
}

.action-btn {
  background: none;
  border: 1px solid #dee2e6;
  border-radius: 6px;
  padding: 0.5rem 1rem;
  color: #495057;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.action-btn:hover {
  border-color: #007bff;
  color: #007bff;
}

.pagination {
  display: flex;
  justify-content: center;
  gap: 0.5rem;
  padding: 2rem;
}

.page-btn {
  width: 40px;
  height: 40px;
  border: 1px solid #dee2e6;
  border-radius: 6px;
  background: white;
  color: #495057;
  cursor: pointer;
  transition: all 0.3s ease;
}

.page-btn:hover {
  border-color: #007bff;
  color: #007bff;
}

.page-btn.active {
  background: #007bff;
  border-color: #007bff;
  color: white;
}

/* Modal Styles */
.modal-overlay, .image-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}

.modal-content, .image-modal-content {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  max-width: 600px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
}

.modal-close, .image-modal-close {
  position: absolute;
  top: 1rem;
  right: 1rem;
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #6c757d;
}

.image-modal-content {
  max-width: 90vw;
  max-height: 90vh;
  padding: 0;
}

.image-modal-content img {
  width: 100%;
  height: auto;
  border-radius: 8px;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 0.5rem;
}

.rating-input {
  display: flex;
  gap: 0.5rem;
}

.rating-star {
  background: none;
  border: none;
  font-size: 2rem;
  color: #e9ecef;
  cursor: pointer;
  transition: color 0.3s ease;
}

.rating-star.active {
  color: #ffc107;
}

textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #dee2e6;
  border-radius: 6px;
  resize: vertical;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
}

.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background: #5a6268;
}

.btn-primary {
  background: #007bff;
  color: white;
}

.btn-primary:hover {
  background: #0056b3;
}

/* Responsive */
@media (max-width: 768px) {
  .reviews-header {
    grid-template-columns: 1fr;
    gap: 2rem;
  }
  
  .reviews-filters {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }
  
  .rating-filters {
    flex-wrap: wrap;
  }
  
  .review-content {
    margin-left: 0;
  }
  
  .review-header {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }
  
  .review-images {
    flex-wrap: wrap;
  }
}
</style>
