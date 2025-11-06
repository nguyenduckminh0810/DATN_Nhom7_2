// Chạy trong Console để xóa token cũ và đăng xuất
localStorage.removeItem('auro_token')
localStorage.removeItem('auro_user')
console.log('✅ Đã xóa token cũ')
console.log('👉 Hãy đăng nhập lại để lấy token mới!')
// Reload page
setTimeout(() => (window.location.href = '/login'), 1000)
