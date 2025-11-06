// Test script to check address save
const token = localStorage.getItem('auro_token');
console.log('Token:', token ? 'exists' : 'not found');

const testData = {
  hoTen: 'Test User',
  soDienThoai: '0123456789',
  diaChi1: '123 Test Street',
  phuongXa: 'Phường Test',
  quanHuyen: 'N/A',
  tinhThanh: 'Hà Nội',
  macDinh: false
};

console.log('Test data:', testData);

fetch('http://localhost:8080/api/dia-chi', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer ' + token
  },
  body: JSON.stringify(testData)
})
.then(res => {
  console.log('Response status:', res.status);
  return res.json();
})
.then(data => {
  console.log('Response data:', data);
})
.catch(err => {
  console.error('Error:', err);
});
