/**
 * 🌐 SCRIPT TẢI DỮ LIỆU GHN TRONG BROWSER
 * 
 * Script này chạy TRỰC TIẾP trong DevTools Console của trình duyệt
 * để tải đầy đủ dữ liệu địa chỉ từ API GHN.
 * 
 * CÁCH SỬ DỤNG:
 * 1. Mở website của bạn đang chạy trên http://localhost:5173
 * 2. Mở DevTools (F12)
 * 3. Chuyển sang tab Console
 * 4. Copy toàn bộ code này và paste vào Console
 * 5. Nhấn Enter để chạy
 * 6. Đợi script hoàn thành (~2-5 phút)
 * 7. Click nút "Download Full Data" để tải file JSON
 * 
 * @author DATN Team
 * @date 2025-11-02
 */

(async function downloadGHNData() {
  console.log('='.repeat(60));
  console.log('🚀 BẮT ĐẦU TẢI DỮ LIỆU GHN');
  console.log('='.repeat(60));
  
  const API_BASE = '/api/shipping'; // Relative URL để tránh CORS
  const DELAY_MS = 100;
  
  const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms));
  
  async function fetchWithRetry(url, retries = 3) {
    for (let i = 0; i < retries; i++) {
      try {
        const response = await fetch(url);
        if (!response.ok) {
          throw new Error(`HTTP ${response.status}`);
        }
        const json = await response.json();
        return json;
      } catch (error) {
        console.error(`❌ Lỗi lần ${i + 1}/${retries}:`, error.message);
        if (i === retries - 1) throw error;
        await delay(1000 * (i + 1));
      }
    }
  }
  
  // Bước 1: Tải tỉnh/thành
  console.log('\n📍 Bước 1: Tải danh sách tỉnh/thành phố...');
  const provinceData = await fetchWithRetry(`${API_BASE}/provinces`);
  const provinces = provinceData.data || provinceData;
  console.log(`✅ Đã tải ${provinces.length} tỉnh/thành phố`);
  
  // Bước 2: Tải quận/huyện
  console.log('\n🏙️ Bước 2: Tải danh sách quận/huyện...');
  const allDistricts = {};
  let totalDistricts = 0;
  
  for (let i = 0; i < provinces.length; i++) {
    const province = provinces[i];
    const percent = (((i + 1) / provinces.length) * 100).toFixed(1);
    console.log(`⏳ ${province.ProvinceName}... [${i + 1}/${provinces.length}] ${percent}%`);
    
    try {
      const districtData = await fetchWithRetry(`${API_BASE}/districts?provinceId=${province.ProvinceID}`);
      const districts = districtData.data || districtData;
      
      if (districts && districts.length > 0) {
        allDistricts[province.ProvinceID] = districts;
        totalDistricts += districts.length;
      }
      
      await delay(DELAY_MS);
    } catch (error) {
      console.error(`❌ Lỗi khi tải ${province.ProvinceName}`);
    }
  }
  
  console.log(`✅ Đã tải ${totalDistricts} quận/huyện từ ${Object.keys(allDistricts).length} tỉnh/thành`);
  
  // Bước 3: Tải phường/xã
  console.log('\n🏘️ Bước 3: Tải danh sách phường/xã...');
  const allWards = {};
  let totalWards = 0;
  let processedCount = 0;
  
  const districtList = Object.values(allDistricts).flat();
  const totalDistrictCount = districtList.length;
  
  for (let i = 0; i < districtList.length; i++) {
    const district = districtList[i];
    processedCount++;
    
    if (processedCount % 10 === 0) {
      const percent = ((processedCount / totalDistrictCount) * 100).toFixed(1);
      console.log(`⏳ Đang tải phường/xã... [${processedCount}/${totalDistrictCount}] ${percent}%`);
    }
    
    try {
      const wardData = await fetchWithRetry(`${API_BASE}/wards?districtId=${district.DistrictID}`);
      const wards = wardData.data || wardData;
      
      if (wards && wards.length > 0) {
        allWards[district.DistrictID] = wards;
        totalWards += wards.length;
      }
      
      await delay(DELAY_MS);
    } catch (error) {
      // Silent fail
    }
  }
  
  console.log(`✅ Đã tải ${totalWards} phường/xã từ ${Object.keys(allWards).length} quận/huyện`);
  
  // Tổng hợp dữ liệu
  const fullData = {
    provinces,
    districts: allDistricts,
    wards: allWards,
    metadata: {
      totalProvinces: provinces.length,
      totalDistricts: totalDistricts,
      totalWards: totalWards,
      generatedAt: new Date().toISOString(),
      apiSource: 'GHN (Giao Hàng Nhanh)'
    }
  };
  
  // Tạo mock data tối ưu (chỉ lấy 10 phường/xã đầu tiên mỗi quận)
  const mockData = {
    provinces: fullData.provinces,
    districts: fullData.districts,
    wards: {}
  };
  
  for (const [districtId, wards] of Object.entries(fullData.wards)) {
    mockData.wards[districtId] = wards.slice(0, 10);
  }
  
  // Hiển thị kết quả
  console.log('\n' + '='.repeat(60));
  console.log('✅ HOÀN THÀNH!');
  console.log('='.repeat(60));
  console.log(`📍 Tỉnh/thành: ${fullData.metadata.totalProvinces}`);
  console.log(`🏙️  Quận/huyện: ${fullData.metadata.totalDistricts}`);
  console.log(`🏘️  Phường/xã: ${fullData.metadata.totalWards}`);
  console.log('='.repeat(60));
  
  // Tạo hàm download file
  window.downloadGHNFullData = () => {
    const blob = new Blob([JSON.stringify(fullData, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'ghn-full-data.json';
    a.click();
    URL.revokeObjectURL(url);
    console.log('✅ Đã tải file ghn-full-data.json');
  };
  
  window.downloadGHNMockData = () => {
    const blob = new Blob([JSON.stringify(mockData, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'ghn-mock-data.json';
    a.click();
    URL.revokeObjectURL(url);
    console.log('✅ Đã tải file ghn-mock-data.json');
  };
  
  window.downloadGHNFullDataJS = () => {
    const jsContent = `/**
 * 🗺️ DỮ LIỆU ĐỊA CHỈ VIỆT NAM - GHN
 * 
 * File này được tạo tự động bởi browser console script
 * Chứa TOÀN BỘ dữ liệu địa chỉ Việt Nam từ API GHN
 * 
 * @generated ${new Date().toISOString()}
 */

export const GHN_DATA = ${JSON.stringify(fullData, null, 2)};

export default GHN_DATA;
`;
    const blob = new Blob([jsContent], { type: 'text/javascript' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'ghn-full-data.js';
    a.click();
    URL.revokeObjectURL(url);
    console.log('✅ Đã tải file ghn-full-data.js');
  };
  
  window.downloadGHNMockDataJS = () => {
    const jsContent = `/**
 * 🗺️ DỮ LIỆU ĐỊA CHỈ VIỆT NAM - GHN (MOCK - TỐI ƯU)
 * 
 * File này được tạo tự động bởi browser console script
 * Chứa dữ liệu địa chỉ tối ưu cho mock mode
 * 
 * @generated ${new Date().toISOString()}
 */

export const GHN_DATA = ${JSON.stringify(mockData, null, 2)};

export default GHN_DATA;
`;
    const blob = new Blob([jsContent], { type: 'text/javascript' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'ghn-mock-data.js';
    a.click();
    URL.revokeObjectURL(url);
    console.log('✅ Đã tải file ghn-mock-data.js');
  };
  
  // Lưu vào window để có thể truy cập
  window.GHN_FULL_DATA = fullData;
  window.GHN_MOCK_DATA = mockData;
  
  console.log('\n📥 TẢI DỮ LIỆU:');
  console.log('   - downloadGHNFullData()     → Tải FULL data (JSON)');
  console.log('   - downloadGHNMockData()     → Tải Mock data (JSON)');
  console.log('   - downloadGHNFullDataJS()   → Tải FULL data (JS module)');
  console.log('   - downloadGHNMockDataJS()   → Tải Mock data (JS module)');
  console.log('\n🔍 XEM DỮ LIỆU:');
  console.log('   - window.GHN_FULL_DATA');
  console.log('   - window.GHN_MOCK_DATA');
  console.log('\n💡 KHUYẾN NGHỊ: Chạy downloadGHNMockDataJS() để tải file tối ưu');
  console.log('='.repeat(60));
  
  return {
    fullData,
    mockData,
    download: {
      fullJSON: () => window.downloadGHNFullData(),
      mockJSON: () => window.downloadGHNMockData(),
      fullJS: () => window.downloadGHNFullDataJS(),
      mockJS: () => window.downloadGHNMockDataJS()
    }
  };
})();
