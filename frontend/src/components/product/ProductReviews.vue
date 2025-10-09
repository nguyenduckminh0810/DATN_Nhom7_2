<template>
  <div class="product-reviews">
    <div class="reviews-header">
      <div class="rating-summary">
        <div class="rating-score">
          <span class="score">{{ product.rating || 4.5 }}</span>
          <div class="stars">
            <i v-for="star in 5" :key="star" 
               :class="star <= (product.rating || 4.5) ? 'ph-star-fill' : 'ph-star'"
               class="star-icon"></i>
          </div>
          <span class="review-count">Dựa trên {{ product.reviewCount || 0 }} đánh giá</span>
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
      <div v-if="filteredReviews.length === 0" class="no-reviews">
        <i class="ph-chat-circle"></i>
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
                     :class="star <= review.rating ? 'ph-star-fill' : 'ph-star'"
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
                <i class="ph-thumbs-up"></i>
                Hữu ích ({{ review.helpfulCount || 0 }})
              </button>
              <button class="action-btn reply" @click="toggleReply(review.id)">
                <i class="ph-chat-circle"></i>
                Trả lời
              </button>
            </div>
          </div>
        </div>
        
        <div v-if="totalPages > 1" class="pagination">
          <button 
            v-for="page in totalPages" 
            :key="page"
            class="page-btn"
            :class="{ active: currentPage === page }"
            @click="currentPage = page"
          >
            {{ page }}
          </button>
        </div>
      </div>
    </div>

    <!-- Write Review Modal -->
    <div v-if="showWriteReview" class="modal-overlay" @click="closeWriteReview">
      <div class="modal-content" @click.stop>
        <button class="modal-close" @click="closeWriteReview">
          <i class="ph-x"></i>
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
                <i class="ph-star-fill"></i>
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
          <i class="ph-x"></i>
        </button>
        <img :src="selectedImage" alt="Review image" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useToast } from '../../composables/useToast'

const props = defineProps({
  product: {
    type: Object,
    required: true
  }
})

const { showToast } = useToast()

// Reactive data
const reviews = ref([])
const sortBy = ref('newest')
const selectedRating = ref(null)
const currentPage = ref(1)
const itemsPerPage = 10
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
  const start = (currentPage.value - 1) * itemsPerPage
  const end = start + itemsPerPage
  return filteredReviews.value.slice(start, end)
})

const totalPages = computed(() => {
  return Math.ceil(filteredReviews.value.length / itemsPerPage)
})

// Methods
const loadMockReviews = () => {
  reviews.value = [
    {
      id: 1,
      name: 'Nguyễn Văn A',
      avatar: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=50&h=50&fit=crop&crop=face',
      rating: 5,
      comment: 'Sản phẩm rất đẹp, chất liệu tốt. Tôi rất hài lòng với chất lượng.',
      date: '2024-01-15',
      helpfulCount: 12,
      images: ['https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=200&h=200&fit=crop']
    },
    {
      id: 2,
      name: 'Trần Thị B',
      avatar: 'https://images.unsplash.com/photo-1494790108755-2616b612b786?w=50&h=50&fit=crop&crop=face',
      rating: 4,
      comment: 'Chất lượng tốt, form dáng đẹp. Chỉ hơi nhỏ so với size thường.',
      date: '2024-01-12',
      helpfulCount: 8,
      images: []
    },
    {
      id: 3,
      name: 'Lê Văn C',
      avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=50&h=50&fit=crop&crop=face',
      rating: 5,
      comment: 'Rất hài lòng với sản phẩm. Giao hàng nhanh, đóng gói cẩn thận.',
      date: '2024-01-10',
      helpfulCount: 15,
      images: ['https://images.unsplash.com/photo-1503341504253-dff4815485f1?w=200&h=200&fit=crop']
    }
  ]
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

const submitReview = () => {
  if (newReview.value.rating === 0) {
    showToast('Vui lòng chọn đánh giá', 'warning')
    return
  }
  
  if (!newReview.value.comment.trim()) {
    showToast('Vui lòng nhập nhận xét', 'warning')
    return
  }
  
  // Add new review
  const review = {
    id: reviews.value.length + 1,
    name: 'Bạn',
    avatar: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=50&h=50&fit=crop&crop=face',
    rating: newReview.value.rating,
    comment: newReview.value.comment,
    date: new Date().toISOString().split('T')[0],
    helpfulCount: 0,
    images: newReview.value.images
  }
  
  reviews.value.unshift(review)
  
  // Reset form
  newReview.value = {
    rating: 0,
    comment: '',
    images: []
  }
  
  closeWriteReview()
  showToast('Đánh giá đã được gửi thành công!', 'success')
}

const closeWriteReview = () => {
  showWriteReview.value = false
  newReview.value = {
    rating: 0,
    comment: '',
    images: []
  }
}

// Lifecycle
onMounted(() => {
  loadMockReviews()
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
