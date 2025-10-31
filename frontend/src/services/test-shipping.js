/**
 * Test script for GHN Shipping Service
 * Run this in browser console to test the shipping API
 */

// Import (if using in Vue component)
// import shippingService from '@/services/shippingService'

// Test 1: Get Provinces
async function testGetProvinces() {
  console.log('=== Test 1: Get Provinces ===')
  try {
    const provinces = await shippingService.getProvinces()
    console.log('✅ Success:', provinces.length, 'provinces found')
    console.log('Sample:', provinces.slice(0, 3))
    return provinces
  } catch (error) {
    console.error('❌ Error:', error.message)
  }
}

// Test 2: Get Districts (TP.HCM)
async function testGetDistricts() {
  console.log('=== Test 2: Get Districts (TP.HCM) ===')
  try {
    const districts = await shippingService.getDistricts(202) // 202 = TP.HCM
    console.log('✅ Success:', districts.length, 'districts found')
    console.log('Sample:', districts.slice(0, 3))
    return districts
  } catch (error) {
    console.error('❌ Error:', error.message)
  }
}

// Test 3: Get Wards (Quận 1)
async function testGetWards() {
  console.log('=== Test 3: Get Wards (Quận 1) ===')
  try {
    const wards = await shippingService.getWards(1542) // 1542 = Quận 1
    console.log('✅ Success:', wards.length, 'wards found')
    console.log('Sample:', wards.slice(0, 3))
    return wards
  } catch (error) {
    console.error('❌ Error:', error.message)
  }
}

// Test 4: Get Services
async function testGetServices() {
  console.log('=== Test 4: Get Services ===')
  try {
    const services = await shippingService.getServices(1542)
    console.log('✅ Success:', services.length, 'services found')
    console.log('Services:', services)
    return services
  } catch (error) {
    console.error('❌ Error:', error.message)
  }
}

// Test 5: Calculate Shipping Fee
async function testCalculateShippingFee() {
  console.log('=== Test 5: Calculate Shipping Fee ===')
  try {
    const result = await shippingService.calculateShippingFee({
      toDistrictId: 1443, // Quận 3
      toWardCode: '21012', // Phường Võ Thị Sáu
      totalWeight: 500, // 500g
      insuranceValue: 500000, // 500,000 VNĐ
      serviceId: 53320, // Express
    })
    console.log('✅ Success:')
    console.log('  - Shipping Fee:', shippingService.formatCurrency(result.shippingFee))
    console.log('  - Expected Delivery:', result.expectedDeliveryTime)
    console.log('  - Full result:', result)
    return result
  } catch (error) {
    console.error('❌ Error:', error.message)
  }
}

// Test 6: Calculate with different weight
async function testCalculateHeavyPackage() {
  console.log('=== Test 6: Calculate Heavy Package (5kg) ===')
  try {
    const result = await shippingService.calculateShippingFee({
      toDistrictId: 1542,
      toWardCode: '21211',
      totalWeight: 5000, // 5kg
      insuranceValue: 2000000, // 2,000,000 VNĐ
      serviceId: 53321, // Standard
    })
    console.log('✅ Success:')
    console.log('  - Weight:', shippingService.formatWeight(5000))
    console.log('  - Shipping Fee:', shippingService.formatCurrency(result.shippingFee))
    console.log('  - Service:', shippingService.getServiceName(53321))
    return result
  } catch (error) {
    console.error('❌ Error:', error.message)
  }
}

// Test 7: Validate Address
function testValidateAddress() {
  console.log('=== Test 7: Validate Address ===')

  // Valid address
  const validAddress = {
    provinceId: 202,
    districtId: 1542,
    wardCode: '21211',
    street: 'Số 1 Đường Lê Duẩn, Phường Bến Nghé',
  }

  const validResult = shippingService.validateAddress(validAddress)
  console.log('Valid Address:', validResult.valid ? '✅' : '❌', validResult)

  // Invalid address
  const invalidAddress = {
    provinceId: null,
    districtId: null,
    wardCode: null,
    street: '123',
  }

  const invalidResult = shippingService.validateAddress(invalidAddress)
  console.log('Invalid Address:', invalidResult.valid ? '✅' : '❌', invalidResult)
}

// Test 8: Format helpers
function testFormatHelpers() {
  console.log('=== Test 8: Format Helpers ===')

  console.log('Currency:')
  console.log('  - 25000 VNĐ =', shippingService.formatCurrency(25000))
  console.log('  - 1500000 VNĐ =', shippingService.formatCurrency(1500000))

  console.log('Weight:')
  console.log('  - 500g =', shippingService.formatWeight(500))
  console.log('  - 1500g =', shippingService.formatWeight(1500))
  console.log('  - 5000g =', shippingService.formatWeight(5000))

  console.log('Service Names:')
  console.log('  - 53320 =', shippingService.getServiceName(53320))
  console.log('  - 53321 =', shippingService.getServiceName(53321))
}

// Run all tests
async function runAllTests() {
  console.log('🚀 Starting GHN Shipping Service Tests...\n')

  await testGetProvinces()
  console.log('\n')

  await testGetDistricts()
  console.log('\n')

  await testGetWards()
  console.log('\n')

  await testGetServices()
  console.log('\n')

  await testCalculateShippingFee()
  console.log('\n')

  await testCalculateHeavyPackage()
  console.log('\n')

  testValidateAddress()
  console.log('\n')

  testFormatHelpers()
  console.log('\n')

  console.log('✅ All tests completed!')
}

// Export for use in browser console or component
if (typeof window !== 'undefined') {
  window.testGHNShipping = {
    runAll: runAllTests,
    testGetProvinces,
    testGetDistricts,
    testGetWards,
    testGetServices,
    testCalculateShippingFee,
    testCalculateHeavyPackage,
    testValidateAddress,
    testFormatHelpers,
  }

  console.log('💡 GHN Test functions loaded! Run: testGHNShipping.runAll()')
}

// For Node.js testing
if (typeof module !== 'undefined' && module.exports) {
  module.exports = {
    runAllTests,
    testGetProvinces,
    testGetDistricts,
    testGetWards,
    testGetServices,
    testCalculateShippingFee,
    testCalculateHeavyPackage,
    testValidateAddress,
    testFormatHelpers,
  }
}
