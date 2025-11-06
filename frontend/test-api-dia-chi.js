// Copy đoạn này vào Console trình duyệt để test API
;(async () => {
  const token = localStorage.getItem('auro_token')

  if (!token) {
    console.error('❌ Không có token! Hãy đăng nhập trước')
    return
  }

  console.log('📡 Testing API with token...')

  try {
    const response = await fetch('http://localhost:8080/api/dia-chi', {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
    })

    console.log('📊 Response status:', response.status)
    console.log('📊 Response statusText:', response.statusText)
    console.log('📊 Response headers:', Object.fromEntries(response.headers))

    if (response.status === 403) {
      console.error('❌ LỖI 403 FORBIDDEN!')
      const text = await response.text()
      console.log('Response body:', text)
    } else {
      const data = await response.json()
      console.log('✅ Response data:', data)
    }
  } catch (error) {
    console.error('❌ Network error:', error)
  }
})()
