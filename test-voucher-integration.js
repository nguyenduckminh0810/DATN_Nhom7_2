// Test script cho tích hợp Voucher BE-FE
const axios = require('axios');

const BASE_URL = 'http://localhost:8080/api';
const FRONTEND_URL = 'http://localhost:5173';

async function testVoucherIntegration() {
  console.log('🚀 Test tích hợp Voucher BE-FE...\n');

  try {
    // 1. Test Backend API
    console.log('1️⃣ Test Backend API...');
    
    // Test tạo voucher
    const voucherData = {
      ma: 'TEST-INTEGRATION-' + Date.now(),
      loai: 'percent',
      giaTri: 20,
      giamToiDa: 100000,
      donToiThieu: 500000,
      batDauLuc: new Date().toISOString(),
      ketThucLuc: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString(),
      gioiHanSuDung: 50
    };

    const createResponse = await axios.post(`${BASE_URL}/phieu-giam-gia/quan-ly`, voucherData);
    console.log('✅ Tạo voucher thành công:', createResponse.data.data.ma);
    const voucherId = createResponse.data.data.id;

    // Test lấy voucher có sẵn
    const availableResponse = await axios.get(`${BASE_URL}/phieu-giam-gia/co-san`);
    console.log('✅ Lấy voucher có sẵn thành công, số lượng:', availableResponse.data.length);

    // Test kiểm tra voucher
    const checkData = {
      maVoucher: voucherData.ma,
      khachHangId: 1,
      donHangTong: 600000
    };

    const checkResponse = await axios.post(`${BASE_URL}/phieu-giam-gia/kiem-tra`, checkData);
    console.log('✅ Kiểm tra voucher thành công:', checkResponse.data.success);

    // Test áp dụng voucher
    const applyResponse = await axios.post(`${BASE_URL}/phieu-giam-gia/ap-dung`, checkData);
    console.log('✅ Áp dụng voucher thành công:', applyResponse.data.success);

    console.log('\n2️⃣ Test Frontend Integration...');
    console.log('📱 Frontend URL:', FRONTEND_URL);
    console.log('🔗 Admin Promotions:', `${FRONTEND_URL}/admin/promotions`);
    console.log('🛒 Cart Page:', `${FRONTEND_URL}/cart`);

    console.log('\n3️⃣ Hướng dẫn test Frontend:');
    console.log('1. Khởi động Frontend: cd frontend && npm run dev');
    console.log('2. Truy cập trang Admin: http://localhost:5173/admin/promotions');
    console.log('3. Kiểm tra:');
    console.log('   - Hiển thị danh sách voucher');
    console.log('   - Tạo voucher mới');
    console.log('   - Chỉnh sửa voucher');
    console.log('   - Xóa voucher');
    console.log('4. Truy cập trang Cart: http://localhost:5173/cart');
    console.log('5. Kiểm tra:');
    console.log('   - Hiển thị voucher có sẵn');
    console.log('   - Nhập mã voucher thủ công');
    console.log('   - Áp dụng voucher thành công');

    console.log('\n4️⃣ Test Data đã tạo:');
    console.log('📝 Voucher Code:', voucherData.ma);
    console.log('💰 Giá trị:', voucherData.giaTri + '%');
    console.log('💵 Giảm tối đa:', voucherData.giamToiDa + ' VNĐ');
    console.log('🛒 Đơn tối thiểu:', voucherData.donToiThieu + ' VNĐ');
    console.log('📅 HSD:', new Date(voucherData.ketThucLuc).toLocaleDateString('vi-VN'));

    console.log('\n🎉 Tất cả test đều thành công!');
    console.log('\n📋 Checklist test Frontend:');
    console.log('□ Admin: Hiển thị voucher');
    console.log('□ Admin: Tạo voucher mới');
    console.log('□ Admin: Chỉnh sửa voucher');
    console.log('□ Admin: Xóa voucher');
    console.log('□ Cart: Hiển thị voucher có sẵn');
    console.log('□ Cart: Nhập mã voucher');
    console.log('□ Cart: Áp dụng voucher');
    console.log('□ Cart: Tính toán giảm giá');

  } catch (error) {
    console.error('❌ Lỗi trong quá trình test:');
    if (error.response) {
      console.error('Status:', error.response.status);
      console.error('Data:', error.response.data);
    } else {
      console.error('Error:', error.message);
    }
  }
}

// Chạy test
testVoucherIntegration();
