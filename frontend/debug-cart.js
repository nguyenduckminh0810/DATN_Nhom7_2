console.log('=================================================')
console.log('🔍 CART DEBUG REPORT')
console.log('=================================================')

// Check localStorage
const token = localStorage.getItem('auro_token')
const cartLocal = localStorage.getItem('auro_cart_v1')

console.log('')
console.log('📦 LOCAL STORAGE:')
console.log('  - Token:', token ? '✅ Exists' : '❌ Missing')
console.log('  - Cart:', cartLocal ? '✅ Exists' : '❌ Missing')

if (cartLocal) {
  try {
    const cart = JSON.parse(cartLocal)
    console.log('  - Cart items count:', cart.length)
    console.log('  - Cart items:', cart)
    console.log('')
    console.log('  📋 Cart Items Details:')
    cart.forEach((item, index) => {
      console.log(`    ${index + 1}. ${item.name}`)
      console.log(`       - ID: ${item.id || 'NO ID (LOCAL ONLY)'}`)
      console.log(`       - Variant ID: ${item.bienTheId || item.variantId}`)
      console.log(`       - Quantity: ${item.quantity}`)
      console.log(`       - Selected: ${item.selected !== false ? 'Yes' : 'No'}`)
    })
  } catch (e) {
    console.log('  ❌ Error parsing cart:', e.message)
  }
}

console.log('')
console.log('=================================================')
console.log('🔧 RECOMMENDED ACTIONS:')
console.log('=================================================')

if (!token) {
  console.log('❌ NO TOKEN - You need to login!')
}

if (!cartLocal) {
  console.log('❌ NO LOCAL CART - Add items to cart first!')
} else {
  const cart = JSON.parse(cartLocal)
  const hasBackendIds = cart.some(item => item.id)
  
  if (!hasBackendIds) {
    console.log('⚠️ CART ITEMS HAVE NO BACKEND IDs!')
    console.log('   → This means items are not synced with backend')
    console.log('   → Call cartStore.loadCart() to reload from backend')
    console.log('')
    console.log('   Run this command:')
    console.log('   window.location.reload()')
  } else {
    console.log('✅ Cart items have backend IDs')
    console.log('   → Cart is synced with backend')
  }
}

console.log('')
console.log('=================================================')
console.log('🧪 TEST COMMANDS:')
console.log('=================================================')
console.log('Reload cart from backend:')
console.log('  location.reload()')
console.log('')
console.log('Clear everything:')
console.log('  localStorage.clear(); location.reload()')
console.log('')
console.log('Check backend cart via API:')
console.log('  fetch("http://localhost:8080/api/khach-hang/gio-hang", {')
console.log('    headers: { "Authorization": "Bearer " + localStorage.getItem("auro_token") }')
console.log('  }).then(r => r.json()).then(d => console.log("Backend cart:", d))')
console.log('=================================================')
