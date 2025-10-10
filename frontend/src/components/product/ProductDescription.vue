<template>
  <div class="product-description">
    <!-- Main Description -->
    <div class="description-section">
      <h3 class="section-title">{{ product.name }}</h3>
      
      <!-- Key Features -->
      <div class="key-features">
        <div class="feature-item" v-for="feature in product.features" :key="feature.title">
          <div class="feature-icon">
            <i :class="feature.icon"></i>
          </div>
          <div class="feature-content">
            <h4 class="feature-title">{{ feature.title }}</h4>
            <p class="feature-description">{{ feature.description }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Product Details -->
    <div class="product-details">
      <h3 class="section-title">Chi tiết sản phẩm</h3>
      
      <div class="details-grid">
        <div class="detail-item">
          <span class="detail-label">Chất liệu:</span>
          <span class="detail-value">{{ product.material || 'Cotton 100%' }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">Xuất xứ:</span>
          <span class="detail-value">{{ product.origin || 'Việt Nam' }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">Màu sắc:</span>
          <span class="detail-value">{{ availableColorsText }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">Kích thước:</span>
          <span class="detail-value">{{ availableSizesText }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">Bảo hành:</span>
          <span class="detail-value">{{ product.warranty || '60 ngày' }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">SKU:</span>
          <span class="detail-value">{{ product.sku || `AURO-${product.id}` }}</span>
        </div>
      </div>
    </div>

    <!-- Care Instructions -->
    <div class="care-instructions">
      <h3 class="section-title">
        <i class="bi bi-heart"></i>
        Bí Quyết Giữ Sản Phẩm Bền Đẹp
      </h3>
      
      <div class="care-content">
        <p class="care-intro">
          Để sản phẩm của bạn luôn giữ được chất lượng tốt nhất và màu sắc tươi mới, hãy tuân thủ các hướng dẫn sau:
        </p>
        
        <div class="care-list">
          <div class="care-item">
            <i class="bi bi-check-circle"></i>
            <span>Giặt máy ở chế độ nhẹ, nhiệt độ nước không quá 30°C</span>
          </div>
          <div class="care-item">
            <i class="bi bi-check-circle"></i>
            <span>Không sử dụng hóa chất tẩy rửa mạnh</span>
          </div>
          <div class="care-item">
            <i class="bi bi-check-circle"></i>
            <span>Phơi ở nơi thoáng mát, tránh ánh nắng trực tiếp</span>
          </div>
          <div class="care-item">
            <i class="bi bi-check-circle"></i>
            <span>Ủi ở nhiệt độ thấp, không ủi trực tiếp lên hình in</span>
          </div>
        </div>
        
        <div class="care-tip">
          <i class="bi bi-lightbulb"></i>
          <span>Mẹo nhỏ: Để sản phẩm luôn như mới, hãy giặt riêng với các sản phẩm cùng màu</span>
        </div>
      </div>
    </div>

    <!-- Styling Tips -->
    <div class="styling-tips">
      <h3 class="section-title">
        <i class="bi bi-palette"></i>
        Gợi Ý Phối Đồ
      </h3>
      
      <div class="styling-content">
        <p class="styling-intro">
          {{ product.name }} có thể kết hợp với nhiều phong cách khác nhau:
        </p>
        
        <div class="styling-options">
          <div class="styling-option">
            <div class="styling-icon">
              <i class="bi bi-briefcase"></i>
            </div>
            <div class="styling-info">
              <h4>Phong cách công sở</h4>
              <p>Kết hợp với quần âu, áo sơ mi để tạo vẻ chuyên nghiệp</p>
            </div>
          </div>
          
          <div class="styling-option">
            <div class="styling-icon">
              <i class="bi bi-lightning"></i>
            </div>
            <div class="styling-info">
              <h4>Phong cách thể thao</h4>
              <p>Phối với quần short, giày thể thao cho vẻ năng động</p>
            </div>
          </div>
          
          <div class="styling-option">
            <div class="styling-icon">
              <i class="bi bi-cup-hot"></i>
            </div>
            <div class="styling-info">
              <h4>Phong cách casual</h4>
              <p>Kết hợp với quần jean, sneaker cho vẻ thoải mái</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Size Chart -->
    <div class="size-chart-section">
      <h3 class="section-title">
        <i class="bi bi-rulers"></i>
        Bảng Size
      </h3>
      
      <div class="size-chart">
        <div class="size-chart-table">
          <table>
            <thead>
              <tr>
                <th>Size</th>
                <th>Chiều cao (cm)</th>
                <th>Cân nặng (kg)</th>
                <th>Ngực (cm)</th>
                <th>Eo (cm)</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="size in sizeChart" :key="size.size">
                <td class="size-label">{{ size.size }}</td>
                <td>{{ size.height }}</td>
                <td>{{ size.weight }}</td>
                <td>{{ size.chest }}</td>
                <td>{{ size.waist }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        
        <div class="size-guide-tip">
          <i class="bi bi-info-circle"></i>
          <span>Không chắc chắn về size? Liên hệ hotline 1900.272737 để được tư vấn miễn phí</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  product: {
    type: Object,
    required: true
  }
})

const availableColorsText = computed(() => {
  if (!props.product.colorOptions) return 'Đa dạng'
  const colorNames = props.product.colorOptions.map(color => getColorName(color))
  return colorNames.join(', ')
})

const availableSizesText = computed(() => {
  if (!props.product.sizes) return 'Đa dạng'
  return props.product.sizes.join(', ')
})

const sizeChart = computed(() => [
  { size: 'S', height: '160-170', weight: '50-60', chest: '86-90', waist: '70-74' },
  { size: 'M', height: '165-175', weight: '60-70', chest: '90-94', waist: '74-78' },
  { size: 'L', height: '170-180', weight: '70-80', chest: '94-98', waist: '78-82' },
  { size: 'XL', height: '175-185', weight: '80-90', chest: '98-102', waist: '82-86' },
  { size: '2XL', height: '180-190', weight: '90-100', chest: '102-106', waist: '86-90' },
  { size: '3XL', height: '185-195', weight: '100-110', chest: '106-110', waist: '90-94' }
])

const getColorName = (colorCode) => {
  const colorMap = {
    '#000000': 'Đen',
    '#ffffff': 'Trắng',
    '#ff0000': 'Đỏ',
    '#0000ff': 'Xanh dương',
    '#00ff00': 'Xanh lá',
    '#ffff00': 'Vàng',
    '#ff69b4': 'Hồng',
    '#808080': 'Xám',
    '#8b4513': 'Nâu'
  }
  return colorMap[colorCode] || colorCode
}
</script>

<style scoped>
.product-description {
  background: #fff;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.section-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.section-title i {
  color: #007bff;
}

.description-section {
  margin-bottom: 3rem;
}

.key-features {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
  margin-top: 1.5rem;
}

.feature-item {
  display: flex;
  gap: 1rem;
  align-items: flex-start;
}

.feature-icon {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #007bff, #0056b3);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.5rem;
  flex-shrink: 0;
}

.feature-content {
  flex: 1;
}

.feature-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 0.5rem;
}

.feature-description {
  color: #6c757d;
  line-height: 1.6;
  margin: 0;
}

.product-details {
  margin-bottom: 3rem;
}

.details-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
  margin-top: 1rem;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #007bff;
}

.detail-label {
  font-weight: 600;
  color: #495057;
}

.detail-value {
  color: #2c3e50;
  font-weight: 500;
}

.care-instructions {
  margin-bottom: 3rem;
}

.care-content {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 2rem;
}

.care-intro {
  font-size: 1.1rem;
  color: #495057;
  margin-bottom: 1.5rem;
  line-height: 1.6;
}

.care-list {
  margin-bottom: 1.5rem;
}

.care-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.75rem 0;
  border-bottom: 1px solid #e9ecef;
}

.care-item:last-child {
  border-bottom: none;
}

.care-item i {
  color: #28a745;
  font-size: 1.2rem;
}

.care-item span {
  color: #495057;
  font-weight: 500;
}

.care-tip {
  background: #fff3cd;
  border: 1px solid #ffeaa7;
  border-radius: 8px;
  padding: 1rem;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.care-tip i {
  color: #856404;
  font-size: 1.2rem;
}

.care-tip span {
  color: #856404;
  font-weight: 500;
}

.styling-tips {
  margin-bottom: 3rem;
}

.styling-content {
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  border-radius: 12px;
  padding: 2rem;
}

.styling-intro {
  font-size: 1.1rem;
  color: #495057;
  margin-bottom: 1.5rem;
  line-height: 1.6;
}

.styling-options {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
}

.styling-option {
  display: flex;
  gap: 1rem;
  align-items: flex-start;
  padding: 1.5rem;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.styling-icon {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #28a745, #20c997);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.2rem;
  flex-shrink: 0;
}

.styling-info h4 {
  font-size: 1rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 0.5rem;
}

.styling-info p {
  color: #6c757d;
  margin: 0;
  line-height: 1.5;
}

.size-chart-section {
  margin-bottom: 0;
}

.size-chart {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 2rem;
}

.size-chart-table {
  overflow-x: auto;
  margin-bottom: 1.5rem;
}

table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

th, td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #e9ecef;
}

th {
  background: #007bff;
  color: white;
  font-weight: 600;
}

.size-label {
  font-weight: 700;
  color: #007bff;
}

.size-guide-tip {
  background: #d1ecf1;
  border: 1px solid #bee5eb;
  border-radius: 8px;
  padding: 1rem;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.size-guide-tip i {
  color: #0c5460;
  font-size: 1.2rem;
}

.size-guide-tip span {
  color: #0c5460;
  font-weight: 500;
}

/* Responsive */
@media (max-width: 768px) {
  .product-description {
    padding: 1rem;
  }
  
  .section-title {
    font-size: 1.25rem;
  }
  
  .key-features {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }
  
  .details-grid {
    grid-template-columns: 1fr;
  }
  
  .styling-options {
    grid-template-columns: 1fr;
  }
  
  .care-content, .styling-content, .size-chart {
    padding: 1rem;
  }
  
  .feature-item, .styling-option {
    flex-direction: column;
    text-align: center;
  }
}
</style>
