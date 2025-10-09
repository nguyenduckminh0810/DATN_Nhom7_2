<template>
  <div class="product-shipping">
    <!-- Shipping Options -->
    <div class="shipping-options">
      <h3 class="section-title">
        <i class="ph-truck"></i>
        Tùy chọn giao hàng
      </h3>
      
      <div class="shipping-grid">
        <div class="shipping-option">
          <div class="shipping-icon">
            <i class="ph-truck"></i>
          </div>
          <div class="shipping-info">
            <h4>Giao hàng tiêu chuẩn</h4>
            <p>Miễn phí cho đơn hàng từ 200.000₫</p>
            <span class="shipping-time">3-5 ngày làm việc</span>
          </div>
          <div class="shipping-price">
            <span v-if="isFreeShipping" class="free">Miễn phí</span>
            <span v-else class="price">25.000₫</span>
          </div>
        </div>
        
        <div class="shipping-option">
          <div class="shipping-icon">
            <i class="ph-lightning"></i>
          </div>
          <div class="shipping-info">
            <h4>Giao hàng nhanh</h4>
            <p>Giao hàng trong ngày tại TP.HCM</p>
            <span class="shipping-time">Trong ngày</span>
          </div>
          <div class="shipping-price">
            <span class="price">50.000₫</span>
          </div>
        </div>
        
        <div class="shipping-option">
          <div class="shipping-icon">
            <i class="ph-clock"></i>
          </div>
          <div class="shipping-info">
            <h4>Giao hàng theo giờ</h4>
            <p>Chọn khung giờ giao hàng phù hợp</p>
            <span class="shipping-time">Theo lịch hẹn</span>
          </div>
          <div class="shipping-price">
            <span class="price">30.000₫</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Shipping Policy -->
    <div class="shipping-policy">
      <h3 class="section-title">
        <i class="ph-shield-check"></i>
        Chính sách giao hàng
      </h3>
      
      <div class="policy-grid">
        <div class="policy-item">
          <div class="policy-icon">
            <i class="ph-map-pin"></i>
          </div>
          <div class="policy-content">
            <h4>Phạm vi giao hàng</h4>
            <p>Giao hàng toàn quốc với mạng lưới đối tác vận chuyển uy tín</p>
            <ul>
              <li>TP.HCM: Giao hàng nhanh trong ngày</li>
              <li>Hà Nội: 1-2 ngày làm việc</li>
              <li>Các tỉnh thành khác: 3-5 ngày làm việc</li>
            </ul>
          </div>
        </div>
        
        <div class="policy-item">
          <div class="policy-icon">
            <i class="ph-clock-counter"></i>
          </div>
          <div class="policy-content">
            <h4>Thời gian giao hàng</h4>
            <p>Cam kết giao hàng đúng hẹn với khách hàng</p>
            <ul>
              <li>Thứ 2 - Thứ 6: 8:00 - 18:00</li>
              <li>Thứ 7: 8:00 - 12:00</li>
              <li>Chủ nhật: Không giao hàng</li>
            </ul>
          </div>
        </div>
        
        <div class="policy-item">
          <div class="policy-icon">
            <i class="ph-phone"></i>
          </div>
          <div class="policy-content">
            <h4>Theo dõi đơn hàng</h4>
            <p>Liên tục cập nhật trạng thái giao hàng</p>
            <ul>
              <li>SMS thông báo khi đơn hàng được giao</li>
              <li>Email xác nhận và mã vận đơn</li>
              <li>Hotline hỗ trợ 24/7: 1900.272737</li>
            </ul>
          </div>
        </div>
      </div>
    </div>

    <!-- Return Policy -->
    <div class="return-policy">
      <h3 class="section-title">
        <i class="ph-arrow-clockwise"></i>
        Chính sách đổi trả
      </h3>
      
      <div class="return-content">
        <div class="return-highlight">
          <div class="highlight-icon">
            <i class="ph-gift"></i>
          </div>
          <div class="highlight-text">
            <h4>60 ngày đổi trả miễn phí</h4>
            <p>Đổi trả vì bất kỳ lý do gì trong vòng 60 ngày kể từ ngày nhận hàng</p>
          </div>
        </div>
        
        <div class="return-conditions">
          <h4>Điều kiện đổi trả:</h4>
          <div class="conditions-grid">
            <div class="condition-item">
              <i class="ph-check-circle"></i>
              <span>Sản phẩm còn nguyên tag, chưa qua sử dụng</span>
            </div>
            <div class="condition-item">
              <i class="ph-check-circle"></i>
              <span>Giữ nguyên bao bì và hóa đơn mua hàng</span>
            </div>
            <div class="condition-item">
              <i class="ph-check-circle"></i>
              <span>Không có dấu hiệu hư hỏng do sử dụng</span>
            </div>
          </div>
        </div>
        
        <div class="return-process">
          <h4>Quy trình đổi trả:</h4>
          <div class="process-steps">
            <div class="step">
              <div class="step-number">1</div>
              <div class="step-content">
                <h5>Liên hệ hỗ trợ</h5>
                <p>Gọi hotline 1900.272737 hoặc chat online</p>
              </div>
            </div>
            <div class="step">
              <div class="step-number">2</div>
              <div class="step-content">
                <h5>Chuẩn bị hàng</h5>
                <p>Đóng gói sản phẩm theo hướng dẫn</p>
              </div>
            </div>
            <div class="step">
              <div class="step-number">3</div>
              <div class="step-content">
                <h5>Giao hàng</h5>
                <p>Nhân viên đến tận nơi nhận hàng trả</p>
              </div>
            </div>
            <div class="step">
              <div class="step-number">4</div>
              <div class="step-content">
                <h5>Hoàn tiền</h5>
                <p>Hoàn tiền trong 24h sau khi nhận hàng</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Contact Support -->
    <div class="contact-support">
      <h3 class="section-title">
        <i class="ph-headset"></i>
        Hỗ trợ khách hàng
      </h3>
      
      <div class="support-grid">
        <div class="support-item">
          <div class="support-icon">
            <i class="ph-phone"></i>
          </div>
          <div class="support-info">
            <h4>Hotline</h4>
            <p class="support-number">1900.272737</p>
            <p class="support-time">8:30 - 22:00 hàng ngày</p>
          </div>
        </div>
        
        <div class="support-item">
          <div class="support-icon">
            <i class="ph-chat-circle"></i>
          </div>
          <div class="support-info">
            <h4>Chat trực tuyến</h4>
            <p class="support-desc">Hỗ trợ 24/7 qua chat</p>
            <button class="chat-btn">Bắt đầu chat</button>
          </div>
        </div>
        
        <div class="support-item">
          <div class="support-icon">
            <i class="ph-envelope"></i>
          </div>
          <div class="support-info">
            <h4>Email</h4>
            <p class="support-desc">support@auro.com</p>
            <p class="support-time">Phản hồi trong 2h</p>
          </div>
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

const isFreeShipping = computed(() => {
  return props.product.price >= 200000
})
</script>

<style scoped>
.product-shipping {
  padding: 2rem;
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

.shipping-options {
  margin-bottom: 3rem;
}

.shipping-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
}

.shipping-option {
  background: white;
  border: 2px solid #e9ecef;
  border-radius: 12px;
  padding: 1.5rem;
  display: flex;
  align-items: center;
  gap: 1rem;
  transition: all 0.3s ease;
}

.shipping-option:hover {
  border-color: #007bff;
  box-shadow: 0 4px 12px rgba(0,123,255,0.15);
}

.shipping-icon {
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

.shipping-info {
  flex: 1;
}

.shipping-info h4 {
  font-size: 1.1rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 0.5rem;
}

.shipping-info p {
  color: #6c757d;
  margin-bottom: 0.25rem;
  font-size: 0.9rem;
}

.shipping-time {
  color: #28a745;
  font-weight: 600;
  font-size: 0.9rem;
}

.shipping-price {
  text-align: right;
}

.shipping-price .free {
  color: #28a745;
  font-weight: 700;
  font-size: 1.1rem;
}

.shipping-price .price {
  color: #dc3545;
  font-weight: 700;
  font-size: 1.1rem;
}

.shipping-policy {
  margin-bottom: 3rem;
}

.policy-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
}

.policy-item {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 2rem;
  display: flex;
  gap: 1.5rem;
  align-items: flex-start;
}

.policy-icon {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #28a745, #20c997);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.5rem;
  flex-shrink: 0;
}

.policy-content h4 {
  font-size: 1.2rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 0.75rem;
}

.policy-content p {
  color: #495057;
  margin-bottom: 1rem;
  line-height: 1.6;
}

.policy-content ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.policy-content li {
  color: #6c757d;
  margin-bottom: 0.5rem;
  padding-left: 1.5rem;
  position: relative;
}

.policy-content li::before {
  content: '•';
  color: #007bff;
  font-weight: bold;
  position: absolute;
  left: 0;
}

.return-policy {
  margin-bottom: 3rem;
}

.return-content {
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  border-radius: 12px;
  padding: 2rem;
}

.return-highlight {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  background: white;
  border-radius: 12px;
  padding: 2rem;
  margin-bottom: 2rem;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.highlight-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #dc3545, #c82333);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 2rem;
  flex-shrink: 0;
}

.highlight-text h4 {
  font-size: 1.5rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 0.5rem;
}

.highlight-text p {
  color: #495057;
  font-size: 1.1rem;
  margin: 0;
}

.return-conditions, .return-process {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  margin-bottom: 1.5rem;
}

.return-conditions h4, .return-process h4 {
  font-size: 1.2rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 1rem;
}

.conditions-grid {
  display: grid;
  gap: 1rem;
}

.condition-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 8px;
}

.condition-item i {
  color: #28a745;
  font-size: 1.2rem;
}

.condition-item span {
  color: #495057;
  font-weight: 500;
}

.process-steps {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
}

.step {
  display: flex;
  gap: 1rem;
  align-items: flex-start;
}

.step-number {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #007bff, #0056b3);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 700;
  font-size: 1.1rem;
  flex-shrink: 0;
}

.step-content h5 {
  font-size: 1rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 0.25rem;
}

.step-content p {
  color: #6c757d;
  margin: 0;
  font-size: 0.9rem;
}

.contact-support {
  margin-bottom: 0;
}

.support-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
}

.support-item {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: all 0.3s ease;
}

.support-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0,0,0,0.15);
}

.support-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #ffc107, #ff9800);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 2rem;
  margin: 0 auto 1rem;
}

.support-info h4 {
  font-size: 1.2rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 0.75rem;
}

.support-number {
  font-size: 1.5rem;
  font-weight: 700;
  color: #007bff;
  margin-bottom: 0.5rem;
}

.support-desc {
  color: #495057;
  margin-bottom: 0.5rem;
}

.support-time {
  color: #6c757d;
  font-size: 0.9rem;
}

.chat-btn {
  background: linear-gradient(135deg, #007bff, #0056b3);
  color: white;
  border: none;
  border-radius: 20px;
  padding: 0.75rem 1.5rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 0.5rem;
}

.chat-btn:hover {
  background: linear-gradient(135deg, #0056b3, #004085);
  transform: translateY(-2px);
}

/* Responsive */
@media (max-width: 768px) {
  .product-shipping {
    padding: 1rem;
  }
  
  .section-title {
    font-size: 1.25rem;
  }
  
  .shipping-grid, .policy-grid {
    grid-template-columns: 1fr;
  }
  
  .shipping-option {
    flex-direction: column;
    text-align: center;
  }
  
  .policy-item {
    flex-direction: column;
    text-align: center;
  }
  
  .return-highlight {
    flex-direction: column;
    text-align: center;
  }
  
  .process-steps {
    grid-template-columns: 1fr;
  }
  
  .step {
    flex-direction: column;
    text-align: center;
  }
  
  .support-grid {
    grid-template-columns: 1fr;
  }
}
</style>
