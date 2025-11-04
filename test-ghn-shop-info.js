// Test script để lấy thông tin shop từ GHN API
// Chạy: node test-ghn-shop-info.js

const https = require('https');

const GHN_TOKEN = '12fde179-b978-11f0-bdfd-7a69b8ccea68';
const SHOP_ID = '197940';

// Test 1: Lấy danh sách tỉnh TP.HCM
console.log('🔍 Test 1: Tìm ProvinceID của TP.HCM...\n');

const options1 = {
  hostname: 'dev-online-gateway.ghn.vn',
  path: '/shiip/public-api/master-data/province',
  method: 'GET',
  headers: {
    'Token': GHN_TOKEN,
    'Content-Type': 'application/json'
  }
};

const req1 = https.request(options1, (res) => {
  let data = '';
  res.on('data', (chunk) => { data += chunk; });
  res.on('end', () => {
    try {
      const response = JSON.parse(data);
      const hcm = response.data.find(p => p.ProvinceName.includes('Hồ Chí Minh'));
      const hanoi = response.data.find(p => p.ProvinceName.includes('Hà Nội'));
      
      console.log('✅ TP. Hồ Chí Minh:', hcm);
      console.log('✅ Hà Nội:', hanoi);
      
      // Test 2: Lấy danh sách quận ở TP.HCM
      if (hcm) {
        console.log('\n🔍 Test 2: Lấy các quận/huyện ở TP.HCM...\n');
        
        const postData = JSON.stringify({ province_id: hcm.ProvinceID });
        const options2 = {
          hostname: 'dev-online-gateway.ghn.vn',
          path: '/shiip/public-api/master-data/district',
          method: 'POST',
          headers: {
            'Token': GHN_TOKEN,
            'Content-Type': 'application/json',
            'Content-Length': Buffer.byteLength(postData)
          }
        };
        
        const req2 = https.request(options2, (res2) => {
          let data2 = '';
          res2.on('data', (chunk) => { data2 += chunk; });
          res2.on('end', () => {
            const response2 = JSON.parse(data2);
            const districts = response2.data.slice(0, 10); // Lấy 10 quận đầu
            console.log('✅ 10 quận/huyện đầu tiên ở TP.HCM:');
            districts.forEach(d => {
              console.log(`   - ${d.DistrictName}: DistrictID = ${d.DistrictID}`);
            });
            
            // Gợi ý
            console.log('\n📌 GỢI Ý SỬA application.properties:');
            console.log('   ghn.from.district.id=' + districts[0].DistrictID);
            console.log('\n💡 Thử các DistrictID sau:');
            districts.slice(0, 5).forEach(d => {
              console.log(`   - ${d.DistrictName} (ID: ${d.DistrictID})`);
            });
          });
        });
        
        req2.write(postData);
        req2.end();
      }
    } catch (e) {
      console.error('❌ Error:', e.message);
    }
  });
});

req1.on('error', (e) => {
  console.error('❌ Request failed:', e.message);
});

req1.end();
