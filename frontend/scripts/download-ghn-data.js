/**
 * 🚀 SCRIPT TẢI FULL DỮ LIỆU GHN
 * 
 * Script này tải TOÀN BỘ dữ liệu địa chỉ Việt Nam từ API GHN:
 * - 63 tỉnh/thành phố
 * - ~700 quận/huyện
 * - ~11,000 phường/xã
 * 
 * CÁCH SỬ DỤNG:
 * 1. Đảm bảo đã cấu hình GHN_TOKEN trong backend
 * 2. Chạy backend trên cổng 8080
 * 3. Chạy script: node scripts/download-ghn-data.js
 * 4. Dữ liệu sẽ được lưu vào: frontend/data/ghn-full-data.json
 * 
 * @author DATN Team
 * @date 2025
 */

import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// API endpoint của backend
const API_BASE = 'http://localhost:8080/api/shipping';

// Delay giữa các request để tránh rate limit
const DELAY_MS = 100;

/**
 * Helper: Delay execution
 */
const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms));

/**
 * Helper: Fetch with retry
 */
async function fetchWithRetry(url, retries = 3) {
  for (let i = 0; i < retries; i++) {
    try {
      const response = await fetch(url);
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      return await response.json();
    } catch (error) {
      console.error(`❌ Lỗi lần ${i + 1}/${retries}:`, error.message);
      if (i === retries - 1) throw error;
      await delay(1000 * (i + 1)); // Exponential backoff
    }
  }
}

/**
 * 1. Tải danh sách tỉnh/thành phố
 */
async function loadProvinces() {
  console.log('\n📍 Bước 1: Tải danh sách tỉnh/thành phố...');
  
  const data = await fetchWithRetry(`${API_BASE}/provinces`);
  
  if (!data.success || !data.data) {
    throw new Error('Không thể tải danh sách tỉnh/thành phố');
  }
  
  console.log(`✅ Đã tải ${data.data.length} tỉnh/thành phố`);
  return data.data;
}

/**
 * 2. Tải danh sách quận/huyện cho TẤT CẢ các tỉnh
 */
async function loadAllDistricts(provinces) {
  console.log('\n🏙️ Bước 2: Tải danh sách quận/huyện...');
  
  const allDistricts = {};
  let totalDistricts = 0;
  let processedCount = 0;
  
  for (const province of provinces) {
    processedCount++;
    const percent = ((processedCount / provinces.length) * 100).toFixed(1);
    
    try {
      process.stdout.write(`\r⏳ Đang tải ${province.ProvinceName}... [${processedCount}/${provinces.length}] ${percent}%`);
      
      const data = await fetchWithRetry(`${API_BASE}/districts?provinceId=${province.ProvinceID}`);
      
      if (data.success && data.data && data.data.length > 0) {
        allDistricts[province.ProvinceID] = data.data;
        totalDistricts += data.data.length;
      } else {
        console.log(`\n⚠️  Tỉnh ${province.ProvinceName} không có dữ liệu quận/huyện`);
      }
      
      await delay(DELAY_MS);
      
    } catch (error) {
      console.error(`\n❌ Lỗi khi tải ${province.ProvinceName}:`, error.message);
    }
  }
  
  console.log(`\n✅ Đã tải ${totalDistricts} quận/huyện từ ${Object.keys(allDistricts).length} tỉnh/thành`);
  return allDistricts;
}

/**
 * 3. Tải danh sách phường/xã cho TẤT CẢ các quận/huyện
 */
async function loadAllWards(districtsByProvince) {
  console.log('\n🏘️ Bước 3: Tải danh sách phường/xã...');
  
  const allWards = {};
  let totalWards = 0;
  let processedCount = 0;
  
  // Đếm tổng số quận/huyện
  const totalDistricts = Object.values(districtsByProvince).reduce(
    (sum, districts) => sum + districts.length, 
    0
  );
  
  for (const [provinceId, districts] of Object.entries(districtsByProvince)) {
    for (const district of districts) {
      processedCount++;
      const percent = ((processedCount / totalDistricts) * 100).toFixed(1);
      
      try {
        process.stdout.write(`\r⏳ Đang tải phường/xã... [${processedCount}/${totalDistricts}] ${percent}%`);
        
        const data = await fetchWithRetry(`${API_BASE}/wards?districtId=${district.DistrictID}`);
        
        if (data.success && data.data && data.data.length > 0) {
          allWards[district.DistrictID] = data.data;
          totalWards += data.data.length;
        }
        
        await delay(DELAY_MS);
        
      } catch (error) {
        console.error(`\n❌ Lỗi khi tải phường/xã cho ${district.DistrictName}:`, error.message);
      }
    }
  }
  
  console.log(`\n✅ Đã tải ${totalWards} phường/xã từ ${Object.keys(allWards).length} quận/huyện`);
  return allWards;
}

/**
 * 4. Lưu dữ liệu vào file JSON
 */
function saveToFile(data, filename) {
  console.log(`\n💾 Đang lưu dữ liệu vào ${filename}...`);
  
  const dataDir = path.join(__dirname, '..', 'data');
  
  // Tạo thư mục data nếu chưa có
  if (!fs.existsSync(dataDir)) {
    fs.mkdirSync(dataDir, { recursive: true });
  }
  
  const filePath = path.join(dataDir, filename);
  
  fs.writeFileSync(
    filePath, 
    JSON.stringify(data, null, 2),
    'utf-8'
  );
  
  const stats = fs.statSync(filePath);
  const fileSizeMB = (stats.size / 1024 / 1024).toFixed(2);
  
  console.log(`✅ Đã lưu file: ${filePath}`);
  console.log(`📦 Kích thước: ${fileSizeMB} MB`);
}

/**
 * 5. Tạo file mock data tối ưu (chỉ lấy một số phường/xã mẫu)
 */
function createOptimizedMockData(fullData) {
  console.log('\n🔧 Đang tạo mock data tối ưu...');
  
  const optimized = {
    provinces: fullData.provinces,
    districts: {},
    wards: {}
  };
  
  // Giữ TOÀN BỘ quận/huyện
  optimized.districts = fullData.districts;
  
  // Chỉ lấy 5-10 phường/xã đầu tiên cho mỗi quận/huyện để giảm dung lượng
  for (const [districtId, wards] of Object.entries(fullData.wards)) {
    optimized.wards[districtId] = wards.slice(0, 10);
  }
  
  console.log('✅ Đã tạo mock data tối ưu');
  return optimized;
}

/**
 * 6. Tạo file TypeScript/JavaScript để import vào project
 */
function generateImportFile(data, filename) {
  console.log(`\n📝 Đang tạo file import ${filename}...`);
  
  const content = `/**
 * 🗺️ DỮ LIỆU ĐỊA CHỈ VIỆT NAM - GHN
 * 
 * File này được tạo tự động bởi script download-ghn-data.js
 * Chứa TOÀN BỘ dữ liệu địa chỉ Việt Nam từ API GHN
 * 
 * Bao gồm:
 * - ${data.provinces.length} tỉnh/thành phố
 * - ${Object.keys(data.districts).length} tỉnh có dữ liệu quận/huyện
 * - ${Object.values(data.districts).reduce((sum, d) => sum + d.length, 0)} quận/huyện
 * - ${Object.keys(data.wards).length} quận có dữ liệu phường/xã
 * - ${Object.values(data.wards).reduce((sum, w) => sum + w.length, 0)} phường/xã
 * 
 * @generated ${new Date().toISOString()}
 */

export const GHN_DATA = ${JSON.stringify(data, null, 2)};

export default GHN_DATA;
`;
  
  const dataDir = path.join(__dirname, '..', 'data');
  const filePath = path.join(dataDir, filename);
  
  fs.writeFileSync(filePath, content, 'utf-8');
  
  console.log(`✅ Đã tạo file: ${filePath}`);
}

/**
 * MAIN: Chạy script
 */
async function main() {
  console.log('='.repeat(60));
  console.log('🚀 SCRIPT TẢI FULL DỮ LIỆU ĐỊA CHỈ GHN');
  console.log('='.repeat(60));
  
  const startTime = Date.now();
  
  try {
    // Bước 1: Tải tỉnh/thành
    const provinces = await loadProvinces();
    
    // Bước 2: Tải quận/huyện
    const districts = await loadAllDistricts(provinces);
    
    // Bước 3: Tải phường/xã
    const wards = await loadAllWards(districts);
    
    // Tổng hợp dữ liệu
    const fullData = {
      provinces,
      districts,
      wards,
      metadata: {
        totalProvinces: provinces.length,
        totalDistricts: Object.values(districts).reduce((sum, d) => sum + d.length, 0),
        totalWards: Object.values(wards).reduce((sum, w) => sum + w.length, 0),
        generatedAt: new Date().toISOString(),
        apiSource: 'GHN (Giao Hàng Nhanh)'
      }
    };
    
    // Bước 4: Lưu file JSON đầy đủ
    saveToFile(fullData, 'ghn-full-data.json');
    
    // Bước 5: Tạo mock data tối ưu
    const optimizedData = createOptimizedMockData(fullData);
    saveToFile(optimizedData, 'ghn-mock-data.json');
    
    // Bước 6: Tạo file JS để import
    generateImportFile(fullData, 'ghn-full-data.js');
    generateImportFile(optimizedData, 'ghn-mock-data.js');
    
    // Thống kê
    const duration = ((Date.now() - startTime) / 1000).toFixed(1);
    
    console.log('\n' + '='.repeat(60));
    console.log('✅ HOÀN THÀNH!');
    console.log('='.repeat(60));
    console.log(`⏱️  Thời gian: ${duration}s`);
    console.log(`📍 Tỉnh/thành: ${fullData.metadata.totalProvinces}`);
    console.log(`🏙️  Quận/huyện: ${fullData.metadata.totalDistricts}`);
    console.log(`🏘️  Phường/xã: ${fullData.metadata.totalWards}`);
    console.log('\n📁 Các file đã tạo:');
    console.log('   - data/ghn-full-data.json (FULL data)');
    console.log('   - data/ghn-full-data.js (FULL data - import)');
    console.log('   - data/ghn-mock-data.json (Mock data tối ưu)');
    console.log('   - data/ghn-mock-data.js (Mock data tối ưu - import)');
    console.log('\n🎯 CÁCH SỬ DỤNG:');
    console.log('   1. Import vào composable: import { GHN_DATA } from "@/data/ghn-full-data.js"');
    console.log('   2. Hoặc sử dụng mock data: import { GHN_DATA } from "@/data/ghn-mock-data.js"');
    console.log('   3. Thay thế MOCK_DATA trong useShipping.js');
    console.log('='.repeat(60));
    
  } catch (error) {
    console.error('\n' + '='.repeat(60));
    console.error('❌ LỖI NGHIÊM TRỌNG');
    console.error('='.repeat(60));
    console.error('Chi tiết:', error.message);
    console.error('\n🔧 KHẮC PHỤC:');
    console.error('   1. Kiểm tra backend đang chạy trên port 8080');
    console.error('   2. Kiểm tra đã cấu hình GHN_TOKEN trong backend');
    console.error('   3. Kiểm tra kết nối internet');
    console.error('   4. Xem log chi tiết ở trên');
    console.error('='.repeat(60));
    process.exit(1);
  }
}

// Chạy script
main();
