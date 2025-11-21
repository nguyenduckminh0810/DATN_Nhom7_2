import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import cartService from '@/services/cartService'
import variantService from '@/services/variantService'
import { useCartStore } from '@/stores/cart'

const SNAPSHOT_STORAGE_KEY = 'auro_reorder_variant_labels'
const SNAPSHOT_STORAGE_TTL = 1000 * 60 * 30

const ensurePositiveInteger = (value, fallback = 1) => {
  const number = Number(value)
  if (Number.isNaN(number) || number <= 0) {
    return fallback
  }
  return Math.floor(number)
}

const normalizeText = (value) => {
  if (value == null) {
    return ''
  }
  return value.toString().trim()
}

const normalizeKeyPart = (value) => {
  return normalizeText(value).toLowerCase()
}

const buildProductKey = (productId, color, size) => {
  if (!productId) {
    return null
  }
  return `${productId}|${normalizeKeyPart(color)}|${normalizeKeyPart(size)}`
}

const readStoredSnapshots = () => {
  if (typeof window === 'undefined' || !window.localStorage) {
    return null
  }

  try {
    const raw = window.localStorage.getItem(SNAPSHOT_STORAGE_KEY)
    if (!raw) {
      return null
    }

    const payload = JSON.parse(raw)
    if (
      !payload ||
      typeof payload !== 'object' ||
      (payload.createdAt && Date.now() - payload.createdAt > SNAPSHOT_STORAGE_TTL)
    ) {
      window.localStorage.removeItem(SNAPSHOT_STORAGE_KEY)
      return null
    }

    return {
      createdAt: payload.createdAt || Date.now(),
      variants: payload.variants || {},
      products: payload.products || {},
    }
  } catch (error) {
    console.error('❌ [REORDER] Không thể đọc snapshot lưu trữ:', error)
    window.localStorage.removeItem(SNAPSHOT_STORAGE_KEY)
    return null
  }
}

const writeStoredSnapshots = (data) => {
  if (typeof window === 'undefined' || !window.localStorage) {
    return
  }

  try {
    window.localStorage.setItem(SNAPSHOT_STORAGE_KEY, JSON.stringify(data))
  } catch (error) {
    console.error('❌ [REORDER] Không thể ghi snapshot lưu trữ:', error)
  }
}

const mergeSnapshotsWithStorage = (snapshots) => {
  if (!Array.isArray(snapshots) || snapshots.length === 0) {
    return
  }

  const stored = readStoredSnapshots() || {
    createdAt: Date.now(),
    variants: {},
    products: {},
  }

  const now = Date.now()

  snapshots.forEach((snapshot) => {
    if (!snapshot) {
      return
    }

    const payload = {
      displayName: snapshot.displayName,
      color: snapshot.color,
      size: snapshot.size,
      productId: snapshot.productId || null,
      savedAt: now,
    }

    if (snapshot.variantId) {
      stored.variants[snapshot.variantId] = payload
    }

    const productKey = buildProductKey(snapshot.productId, snapshot.color, snapshot.size)
    if (productKey) {
      stored.products[productKey] = payload
    }
  })

  stored.createdAt = now
  writeStoredSnapshots(stored)
}

const createOrderItemSnapshot = (item) => {
  if (!item) {
    return null
  }

  const variantId = item.bienTheId || item.variantId || null
  const productId = item.sanPhamId || item.productId || null
  if (!variantId && !productId) {
    return null
  }

  const baseName = normalizeText(
    item.tenSanPham ||
      item.name ||
      item.productName ||
      item.title ||
      (item.sanPham && item.sanPham.tenSanPham) ||
      'Sản phẩm',
  )
  const color = normalizeText(item.mauSacTen || item.selectedColor || item.color)
  const size = normalizeText(item.kichCoTen || item.selectedSize || item.size)
  const variantLabel = [color, size].filter(Boolean).join(' - ')
  const displayName = variantLabel ? `${baseName} | ${variantLabel}` : baseName

  return {
    variantId,
    productId,
    color,
    size,
    displayName,
  }
}

const applySnapshotsToCart = (cartItems, extraSnapshots = []) => {
  if (!Array.isArray(cartItems) || cartItems.length === 0) {
    return
  }

  const snapshotMap = new Map()

  const addSnapshotToMap = (snapshot) => {
    if (!snapshot) {
      return
    }

    if (snapshot.variantId) {
      snapshotMap.set(`variant:${snapshot.variantId}`, snapshot)
    }

    const productKey = buildProductKey(snapshot.productId, snapshot.color, snapshot.size)
    if (productKey) {
      snapshotMap.set(`product:${productKey}`, snapshot)
    }
  }

  if (Array.isArray(extraSnapshots)) {
    extraSnapshots.forEach(addSnapshotToMap)
  }

  const stored = readStoredSnapshots()
  if (stored) {
    Object.entries(stored.variants || {}).forEach(([variantId, data]) => {
      addSnapshotToMap({
        variantId,
        productId: data.productId,
        color: data.color,
        size: data.size,
        displayName: data.displayName,
      })
    })

    Object.entries(stored.products || {}).forEach(([productKey, data]) => {
      const [productId] = productKey.split('|')
      addSnapshotToMap({
        productId: data.productId || productId,
        color: data.color,
        size: data.size,
        displayName: data.displayName,
      })
    })
  }

  cartItems.forEach((cartItem) => {
    if (!cartItem) {
      return
    }

    const variantId = cartItem.variantId || cartItem.bienTheId || null
    const productKey = buildProductKey(cartItem.productId, cartItem.color, cartItem.size)

    let matched = null
    if (variantId && snapshotMap.has(`variant:${variantId}`)) {
      matched = snapshotMap.get(`variant:${variantId}`)
    } else if (productKey && snapshotMap.has(`product:${productKey}`)) {
      matched = snapshotMap.get(`product:${productKey}`)
    }

    if (matched?.displayName) {
      cartItem.name = matched.displayName
    }
    if (matched?.color) {
      cartItem.color = matched.color
    }
    if (matched?.size) {
      cartItem.size = matched.size
    }
  })
}

export function useReorder() {
  const router = useRouter()
  const cartStore = useCartStore()
  const reorderingState = reactive({})

  const setReorderingState = (orderId, value) => {
    if (orderId == null) {
      return
    }
    reorderingState[orderId] = value
  }

  const isOrderReordering = (orderId) => {
    if (orderId == null) {
      return false
    }
    return !!reorderingState[orderId]
  }

  const reorderOrder = async (order) => {
    if (!order) {
      alert('Không tìm thấy thông tin đơn hàng.')
      return
    }

    const orderId = order.id
    if (orderId == null) {
      alert('Đơn hàng không hợp lệ.')
      return
    }

    if (isOrderReordering(orderId)) {
      return
    }

    const itemsToReorder = Array.isArray(order.items)
      ? order.items.filter((item) => item?.bienTheId)
      : []

    if (itemsToReorder.length === 0) {
      alert('Đơn hàng không có sản phẩm hợp lệ để mua lại.')
      return
    }

    setReorderingState(orderId, true)

    const failedItems = []
    const outOfStockItems = []
    const insufficientStockItems = []
    const snapshots = []
    let successCount = 0

    try {
      // Kiểm tra tồn kho trước khi thêm vào giỏ hàng
      for (const item of itemsToReorder) {
        const quantity = ensurePositiveInteger(item.quantity || item.soLuong, 1)
        const variantId = item.bienTheId

        try {
          // Lấy thông tin biến thể để kiểm tra tồn kho
          const variantResponse = await variantService.getById(variantId)
          const variant = variantResponse?.data || variantResponse

          if (!variant) {
            console.warn('⚠️ [REORDER] Không tìm thấy thông tin biến thể:', variantId)
            failedItems.push({ ...item, reason: 'Không tìm thấy sản phẩm' })
            continue
          }

          const stock = variant.stock || 0
          const available = variant.available !== false && stock > 0

          // Kiểm tra tồn kho
          if (!available || stock <= 0) {
            outOfStockItems.push({ ...item, stock: 0 })
            continue
          }

          if (quantity > stock) {
            insufficientStockItems.push({ ...item, stock, requestedQuantity: quantity })
            // Thêm với số lượng tối đa có thể
            try {
              await cartService.addToCart({
                bienTheId: variantId,
                soLuong: stock,
              })
              successCount += 1
              const snapshot = createOrderItemSnapshot(item)
              if (snapshot) {
                snapshots.push(snapshot)
              }
            } catch (error) {
              console.error('❌ [REORDER] Lỗi khi thêm sản phẩm với số lượng giảm:', {
                item,
                error,
              })
              failedItems.push({ ...item, reason: 'Lỗi khi thêm vào giỏ hàng' })
            }
            continue
          }

          // Sản phẩm có đủ hàng, thêm vào giỏ hàng
          try {
            await cartService.addToCart({
              bienTheId: variantId,
              soLuong: quantity,
            })
            successCount += 1
            const snapshot = createOrderItemSnapshot(item)
            if (snapshot) {
              snapshots.push(snapshot)
            }
          } catch (error) {
            console.error('❌ [REORDER] Lỗi khi thêm sản phẩm vào giỏ hàng:', {
              item,
              error,
            })
            const errorMessage = error.response?.data?.message || error.message || ''
            if (errorMessage.includes('tồn kho') || errorMessage.includes('hết hàng') || errorMessage.includes('stock')) {
              outOfStockItems.push({ ...item, stock: 0 })
            } else {
              failedItems.push({ ...item, reason: errorMessage || 'Lỗi không xác định' })
            }
          }
        } catch (error) {
          console.error('❌ [REORDER] Lỗi khi kiểm tra tồn kho:', {
            item,
            error,
          })
          // Nếu không lấy được thông tin biến thể, vẫn thử thêm vào giỏ hàng
          // Backend sẽ kiểm tra lại
          try {
            await cartService.addToCart({
              bienTheId: variantId,
              soLuong: quantity,
            })
            successCount += 1
            const snapshot = createOrderItemSnapshot(item)
            if (snapshot) {
              snapshots.push(snapshot)
            }
          } catch (addError) {
            console.error('❌ [REORDER] Lỗi khi thêm sản phẩm vào giỏ hàng:', {
              item,
              error: addError,
            })
            failedItems.push({ ...item, reason: 'Lỗi khi thêm vào giỏ hàng' })
          }
        }
      }

      if (successCount > 0) {
        mergeSnapshotsWithStorage(snapshots)

        try {
          await cartStore.loadCart()
          applySnapshotsToCart(cartStore.items, snapshots)
        } catch (loadError) {
          console.error('❌ [REORDER] Lỗi khi tải lại giỏ hàng:', loadError)
        }
      }

      // Tạo thông báo chi tiết
      const messages = []

      if (successCount > 0) {
        messages.push(`Đã thêm ${successCount} sản phẩm vào giỏ hàng.`)
      }

      if (outOfStockItems.length > 0) {
        const outOfStockNames = outOfStockItems.map((item) => item.name || 'Sản phẩm').join(', ')
        messages.push(`Sản phẩm đã hết hàng: ${outOfStockNames}`)
      }

      if (insufficientStockItems.length > 0) {
        const insufficientDetails = insufficientStockItems
          .map((item) => {
            const name = item.name || 'Sản phẩm'
            return `${name} (chỉ còn ${item.stock} sản phẩm, đã thêm ${item.stock} thay vì ${item.requestedQuantity})`
          })
          .join('; ')
        messages.push(`Số lượng không đủ: ${insufficientDetails}`)
      }

      if (failedItems.length > 0) {
        const failedNames = failedItems.map((item) => item.name || 'Sản phẩm').join(', ')
        messages.push(`Không thể thêm: ${failedNames}`)
      }

      if (successCount === 0) {
        if (outOfStockItems.length > 0 || insufficientStockItems.length > 0) {
          alert(
            'Không thể thêm sản phẩm vào giỏ hàng do vấn đề về tồn kho:\n\n' +
              messages.filter((m) => !m.includes('Đã thêm')).join('\n'),
          )
        } else {
          alert('Không thể thêm sản phẩm nào vào giỏ hàng. Vui lòng thử lại sau.')
        }
        return
      }

      // Hiển thị thông báo tổng hợp
      if (messages.length > 0) {
        alert(messages.join('\n\n'))
      }

      try {
        await router.push({ path: '/cart', query: { reorder: orderId } })
      } catch (navigationError) {
        console.error('❌ [REORDER] Lỗi khi điều hướng tới trang giỏ hàng:', navigationError)
      }
    } catch (error) {
      console.error('❌ [REORDER] Lỗi chung khi mua lại:', error)
      alert('Lỗi khi mua lại: ' + (error.response?.data?.message || error.message))
    } finally {
      setReorderingState(orderId, false)
    }
  }

  return {
    reorderOrder,
    isOrderReordering,
  }
}

export default useReorder


