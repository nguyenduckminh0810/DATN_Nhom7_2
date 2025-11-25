console.log('=================================================')
console.log('🔍 CHECKING TOKEN VALIDITY')
console.log('=================================================')

const token = localStorage.getItem('auro_token')

if (!token) {
  console.log('❌ No token found in localStorage')
  console.log('✅ Ready for fresh login!')
} else {
  try {
    const base64Url = token.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const payload = JSON.parse(window.atob(base64))
    
    console.log('🔓 Token Payload:', payload)
    console.log('')
    console.log('📋 Token Details:')
    console.log('  - Subject:', payload.sub)
    console.log('  - Issued At:', new Date(payload.iat * 1000).toLocaleString())
    console.log('  - Expires:', new Date(payload.exp * 1000).toLocaleString())
    console.log('')
    console.log('👮 Authorities:', payload.authorities)
    
    if (!payload.authorities) {
      console.log('❌ ERROR: No authorities found!')
      console.log('⚠️  This token is INVALID and will be cleared!')
    } else if (!Array.isArray(payload.authorities)) {
      console.log('❌ ERROR: Authorities is not an array!')
      console.log('⚠️  This token is INVALID and will be cleared!')
    } else if (payload.authorities.length === 0) {
      console.log('❌ ERROR: Authorities array is empty!')
      console.log('⚠️  This token is INVALID and will be cleared!')
    } else {
      const hasValidRole = payload.authorities.some(auth => 
        auth && auth.authority && auth.authority.startsWith('ROLE_')
      )
      
      if (hasValidRole) {
        console.log('✅ Token is VALID!')
        console.log('✅ Has proper ROLE_* authority')
      } else {
        console.log('❌ ERROR: No valid ROLE_* authority found!')
        console.log('⚠️  This token is INVALID and will be cleared!')
      }
    }
  } catch (error) {
    console.log('❌ ERROR decoding token:', error.message)
    console.log('⚠️  This token is INVALID and will be cleared!')
  }
}

console.log('')
console.log('=================================================')
console.log('🔧 MANUAL COMMANDS:')
console.log('=================================================')
console.log('Clear token:')
console.log('  localStorage.clear(); location.reload()')
console.log('')
console.log('View token:')
console.log('  localStorage.getItem("auro_token")')
console.log('')
console.log('=================================================')
