<template>
  <footer class="footer-modern py-5">
    <div class="container">
      <div class="row">
        <!-- Brand & Description -->
        <div class="col-lg-4 mb-4">
          <h5 class="fw-bold mb-3 gradient-text">
            <i class="bi bi-shop me-2"></i>AURO
          </h5>
          <p class="text-white-50">
            Thương hiệu thời trang nam cao cấp với thiết kế tinh tế và chất lượng vượt trội.
            Chúng tôi cam kết mang đến những sản phẩm tốt nhất cho phái mạnh.
          </p>
          <div class="social-links">
            <a href="#" title="Facebook">
              <i class="bi bi-facebook"></i>
            </a>
            <a href="#" title="Instagram">
              <i class="bi bi-instagram"></i>
            </a>
            <a href="#" title="Twitter">
              <i class="bi bi-twitter"></i>
            </a>
            <a href="#" title="YouTube">
              <i class="bi bi-youtube"></i>
            </a>
          </div>
        </div>
        
        <!-- Products -->
        <div class="col-lg-2 col-md-6 mb-4">
          <h6 class="fw-bold mb-3">Sản phẩm</h6>
          <ul class="list-unstyled">
            <li class="mb-2">
              <router-link to="/category/ao" class="text-white-50 text-decoration-none">
                Áo sơ mi
              </router-link>
            </li>
            <li class="mb-2">
              <router-link to="/category/ao" class="text-white-50 text-decoration-none">
                Áo thun
              </router-link>
            </li>
            <li class="mb-2">
              <router-link to="/category/quan" class="text-white-50 text-decoration-none">
                Quần âu
              </router-link>
            </li>
            <li class="mb-2">
              <router-link to="/category/quan" class="text-white-50 text-decoration-none">
                Quần jean
              </router-link>
            </li>
            <li class="mb-2">
              <router-link to="/category/phu-kien" class="text-white-50 text-decoration-none">
                Phụ kiện
              </router-link>
            </li>
          </ul>
        </div>
        
        <!-- Customer Service -->
        <div class="col-lg-2 col-md-6 mb-4">
          <h6 class="fw-bold mb-3">Hỗ trợ khách hàng</h6>
          <ul class="list-unstyled">
            <li class="mb-2">
              <a href="#" class="text-white-50 text-decoration-none">Hướng dẫn mua hàng</a>
            </li>
            <li class="mb-2">
              <a href="#" class="text-white-50 text-decoration-none">Chính sách đổi trả</a>
            </li>
            <li class="mb-2">
              <a href="#" class="text-white-50 text-decoration-none">Chính sách bảo hành</a>
            </li>
            <li class="mb-2">
              <a href="#" class="text-white-50 text-decoration-none">Vận chuyển & giao hàng</a>
            </li>
            <li class="mb-2">
              <a href="#" class="text-white-50 text-decoration-none">Câu hỏi thường gặp</a>
            </li>
          </ul>
        </div>
        
        <!-- Company -->
        <div class="col-lg-2 col-md-6 mb-4">
          <h6 class="fw-bold mb-3">Công ty</h6>
          <ul class="list-unstyled">
            <li class="mb-2">
              <a href="#" class="text-white-50 text-decoration-none">Giới thiệu</a>
            </li>
            <li class="mb-2">
              <a href="#" class="text-white-50 text-decoration-none">Tuyển dụng</a>
            </li>
            <li class="mb-2">
              <a href="#" class="text-white-50 text-decoration-none">Tin tức</a>
            </li>
            <li class="mb-2">
              <a href="#" class="text-white-50 text-decoration-none">Liên hệ</a>
            </li>
            <li class="mb-2">
              <a href="#" class="text-white-50 text-decoration-none">Điều khoản sử dụng</a>
            </li>
          </ul>
        </div>
        
        <!-- Contact Info -->
        <div class="col-lg-2 col-md-6 mb-4">
          <h6 class="fw-bold mb-3">Liên hệ</h6>
          <div class="contact-info">
            <p class="text-white-50 mb-2">
              <i class="bi bi-geo-alt me-2"></i>
              123 Đường ABC, Quận XYZ, TP.HCM
            </p>
            <p class="text-white-50 mb-2">
              <i class="bi bi-telephone me-2"></i>
              <a href="tel:02812345678" class="text-white-50 text-decoration-none">
                (028) 1234 5678
              </a>
            </p>
            <p class="text-white-50 mb-2">
              <i class="bi bi-envelope me-2"></i>
              <a href="mailto:info@auro.com" class="text-white-50 text-decoration-none">
                info@auro.com
              </a>
            </p>
            <p class="text-white-50">
              <i class="bi bi-clock me-2"></i>
              T2-T6: 8:00-18:00<br>
              <span class="ms-4">T7-CN: 9:00-17:00</span>
            </p>
          </div>
        </div>
      </div>
      
      <!-- Newsletter -->
      <div class="row mt-4">
        <div class="col-12">
          <div class="newsletter-section text-center py-4 border-top" style="border-color: rgba(255, 255, 255, 0.2) !important;">
            <h6 class="fw-bold mb-3">Đăng ký nhận tin</h6>
            <p class="text-white-50 mb-3">Nhận thông tin về sản phẩm mới và ưu đãi đặc biệt</p>
            <form @submit.prevent="handleNewsletter" class="d-flex justify-content-center gap-2">
              <div class="input-group" style="max-width: 400px;">
                <input 
                  v-model="email"
                  type="email" 
                  class="form-control" 
                  placeholder="Nhập email của bạn"
                  required
                >
                <button class="btn btn-auro-primary" type="submit">
                  Đăng ký
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
      
      <!-- Copyright -->
      <div class="row mt-4">
        <div class="col-12">
          <hr class="my-4">
          <div class="row align-items-center">
            <div class="col-md-6">
              <p class="text-white-50 mb-0">
                &copy; 2024 AURO. Tất cả quyền được bảo lưu.
              </p>
            </div>
            <div class="col-md-6 text-md-end">
              <div class="payment-methods">
                <span class="text-white-50 me-3">Thanh toán:</span>
                <i class="bi bi-credit-card text-white-50 me-2" title="Thẻ tín dụng"></i>
                <i class="bi bi-paypal text-white-50 me-2" title="PayPal"></i>
                <i class="bi bi-bank text-white-50 me-2" title="Chuyển khoản"></i>
                <i class="bi bi-cash text-white-50" title="Tiền mặt"></i>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
import { ref } from 'vue'

const email = ref('')

const handleNewsletter = () => {
  if (email.value) {
    // TODO: Implement newsletter subscription
    alert('Cảm ơn bạn đã đăng ký nhận tin!')
    email.value = ''
  }
}
</script>

<style scoped>
.social-links a {
  font-size: 1.2rem;
  transition: all 0.3s ease;
}

.contact-info a:hover {
  color: #ffc107 !important;
}

.payment-methods i {
  font-size: 1.1rem;
  transition: color 0.3s ease;
}

.payment-methods i:hover {
  color: #ffc107 !important;
}

.newsletter-section {
  background: rgba(255, 255, 255, 0.15);
  border-radius: 1rem;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

/* Mobile responsive */
@media (max-width: 767.98px) {
  .newsletter-section .input-group {
    max-width: 100% !important;
  }
  
  .payment-methods {
    margin-top: 1rem;
    text-align: center !important;
  }
}
</style>
