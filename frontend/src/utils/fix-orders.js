// Script để fix các đơn hàng có tổng tiền = null
const API_URL = 'http://localhost:8080/api/don-hang/fix-null-total';

async function fixOrders() {
  try {
    console.log('🔧 Đang fix các đơn hàng có tổng tiền = null...');
    
    const response = await fetch(API_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      }
    });
    
    const data = await response.json();
    
    if (data.success) {
      console.log('✅ Thành công!');
      console.log(`📊 Đã fix ${data.fixedCount} đơn hàng`);
      console.log(`💬 Message: ${data.message}`);
    } else {
      console.error('❌ Lỗi:', data.error);
    }
  } catch (error) {
    console.error('❌ Lỗi khi gọi API:', error);
  }
}

fixOrders();
