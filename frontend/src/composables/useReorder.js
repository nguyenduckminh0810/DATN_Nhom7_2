import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import cartService from '@/services/cartService'
import { useCartStore } from '@/stores/cart'
import orderService from '@/services/orderService'

const REORDER_STORAGE_KEY = 'auro_reorder_shipping'
const REORDER_ITEMS_KEY = 'auro_reorder_items'

const MAX_STORAGE_AGE = 1000 * 60 * 30 // 30 phút

const cleanReorderStorageIfExpired = () => {
  try {
    const raw = localStorage.getItem(REORDER_STORAGE_KEY)
    if (!raw) {
      return null
    }
    const payload = JSON.parse(raw)
    if (!payload || typeof payload !== 'object') {
      localStorage.removeItem(REORDER_STORAGE_KEY)
      return null
    }
    if (payload.createdAt && Date.now() - payload.createdAt > MAX_STORAGE_AGE) {
      localStorage.removeItem(REORDER_STORAGE_KEY)
      return null
    }
    return payload
  } catch (error) {
    console.error('❌ [REORDER] Không thể đọc dữ liệu lưu trữ mua lại:', error)
    localStorage.removeItem(REORDER_STORAGE_KEY)
    return null
  }
}

const saveReorderShipping = (orderId, shipping) => {
  if (!shipping) {
    return
  }
  try {
    const payload = {
      orderId,
      createdAt: Date.now(),
      shipping,
    }
    localStorage.setItem(REORDER_STORAGE_KEY, JSON.stringify(payload))
  } catch (error) {
    console.error('❌ [REORDER] Không thể lưu thông tin giao hàng cho mua lại:', error)
  }
}

const saveReorderItems = (orderId, items) => {
  if (!Array.isArray(items) || items.length === 0) {
    return
  }
  try {
    const itemSnapshots = items.map((item) => {
      const color = item.mauSacTen || item.selectedColor || item.color || ''
      const size = item.kichCoTen || item.selectedSize || item.size || ''
      const variantText = [color, size].filter(Boolean).join(' - ')
      const baseName = item.name || ''
      const displayName = variantText ? `${baseName} (${variantText})` : baseName
      const variantId = item.bienTheId || item.variantId || null
      const productId = item.sanPhamId || item.productId || null
      const normalizedColor = normalizeKeyPart(color)
      const normalizedSize = normalizeKeyPart(size)
      const normalizedName = normalizeKeyPart(baseName)

      const identifiers = []
      if (variantId) {
        identifiers.push(`variant:${variantId}`)
      }
      if (productId) {
        identifiers.push(`product:${productId}|${normalizedColor}|${normalizedSize}`)
      }
      const variantTextKey = [normalizedColor, normalizedSize].filter(Boolean).join('|')
      if (variantTextKey) {
        identifiers.push(`variantText:${variantTextKey}`)
      }
      if (normalizedName) {
        identifiers.push(`name:${normalizedName}`)
      }

      return {
        orderId,
        variantId,
        productId,
        name: baseName,
        displayName,
        color,
        size,
        quantity: item.quantity || item.soLuong || 1,
        identifiers,
      }
    })

    const payload = {
      orderId,
      createdAt: Date.now(),
      items: itemSnapshots,
    }

    localStorage.setItem(REORDER_ITEMS_KEY, JSON.stringify(payload))
  } catch (error) {
    console.error('❌ [REORDER] Không thể lưu thông tin sản phẩm mua lại:', error)
  }
}

const normalizeText = (text) => {
  return (text || '').trim()
}

const normalizeKeyPart = (value) => {
  if (value == null) {
    return ''
  }
  return value.toString().trim().toLowerCase()
}

const parseShippingSnapshot = (snapshot, note) => {
  if (!snapshot) {
    return {
      fullName: '',
      phone: '',
      email: '',
      addressLine: '',
      provinceName: '',
      districtName: '',
      wardName: '',
      note: note || '',
    }
  }

  const info = {
    fullName: '',
    phone: '',
    email: '',
    addressLine: '',
    provinceName: '',
    districtName: '',
    wardName: '',
    note: note || '',
  }

  const normalizedSnapshot = snapshot.replace(/<br\s*\/?>/gi, '\n')
  const lines = normalizedSnapshot
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter(Boolean)

  const parseLine = (line) => {
    const [label, ...rest] = line.split(':')
    if (!label || rest.length === 0) {
      return { label: '', value: line }
    }
    return {
      label: label.trim().toLowerCase(),
      value: rest.join(':').trim(),
    }
  }

  lines.forEach((line) => {
    const { label, value } = parseLine(line)
    const lowerLine = line.toLowerCase()

    if (!label) {
      if (!info.addressLine) {
        info.addressLine = normalizeText(value)
      }
      return
    }

    if (label.includes('họ tên') || label.includes('người nhận') || label.includes('ten nguoi nhan')) {
      info.fullName = normalizeText(value)
      return
    }

    if (label.includes('điện thoại') || label.includes('số điện thoại') || label.includes('phone')) {
      info.phone =
        normalizeText(value).replace(/[^0-9+]/g, '') ||
        normalizeText(value)
      return
    }

    if (label.includes('email')) {
      info.email = normalizeText(value)
      return
    }

    if (label.includes('địa chỉ') || label.includes('dia chi')) {
      info.addressLine = normalizeText(value)
      return
    }

    if (label.includes('phường') || label.includes('xã')) {
      info.wardName = normalizeText(value)
      return
    }

    if (label.includes('quận') || label.includes('huyện')) {
      info.districtName = normalizeText(value)
      return
    }

    if (label.includes('tỉnh') || label.includes('thành phố')) {
      info.provinceName = normalizeText(value)
      return
    }

    if (lowerLine.startsWith('ghi chú') || lowerLine.startsWith('ghi chu')) {
      info.note = normalizeText(value)
    }
  })

  if (info.addressLine) {
    const segments = info.addressLine
      .split(',')
      .map((segment) => segment.trim())
      .filter(Boolean)
    if (!info.provinceName && segments.length >= 1) {
      info.provinceName = normalizeText(segments[segments.length - 1])
    }
    if (!info.districtName && segments.length >= 2) {
      info.districtName = normalizeText(segments[segments.length - 2])
    }
    if (!info.wardName && segments.length >= 3) {
      info.wardName = normalizeText(segments[segments.length - 3])
    }
  }

  return info
}

export function useReorder() {
  const cartStore = useCartStore()
  const router = useRouter()
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
      return
    }

    const orderId = order.id
    cleanReorderStorageIfExpired()

    if (isOrderReordering(orderId)) {
      return
    }

    if (!order.items || order.items.length === 0) {
      alert('Đơn hàng này không có sản phẩm để mua lại.')
      return
    }

    const itemsToReorder = order.items.filter((item) => item?.bienTheId)

    if (itemsToReorder.length === 0) {
      alert('Không tìm thấy biến thể sản phẩm hợp lệ để thêm vào giỏ hàng.')
      return
    }

    let shippingInfo = null

    if (order.shippingSnapshot) {
      shippingInfo = parseShippingSnapshot(order.shippingSnapshot, order.shippingNote)
    } else {
      try {
        const response = await orderService.getOrderById(orderId)
        const detailData = response?.data || response
        const snapshot = detailData?.diaChiGiaoSnapshot || ''
        const note = detailData?.ghiChu || ''
        if (snapshot) {
          shippingInfo = parseShippingSnapshot(snapshot, note)
        }
      } catch (error) {
        console.error('❌ [REORDER] Không thể lấy chi tiết đơn hàng để điền địa chỉ:', error)
      }
    }

    try {
      setReorderingState(orderId, true)

      const failedItems = []
      let successCount = 0

      for (const item of itemsToReorder) {
        const quantity = item.quantity && item.quantity > 0 ? item.quantity : 1

        try {
          await cartService.addToCart({
            bienTheId: item.bienTheId,
            soLuong: quantity,
          })
          successCount += 1
        } catch (error) {
          console.error('❌ [REORDER] Lỗi khi thêm sản phẩm vào giỏ hàng:', {
            item,
            error,
          })
          failedItems.push(item)
        }
      }

      if (successCount > 0) {
        try {
          await cartStore.loadCart()
        } catch (loadError) {
          console.error('❌ [REORDER] Lỗi khi tải lại giỏ hàng:', loadError)
        }
      }

      if (failedItems.length === itemsToReorder.length) {
        alert('Không thể thêm sản phẩm nào vào giỏ hàng. Vui lòng thử lại sau.')
        return
      }

      if (successCount > 0) {
        saveReorderItems(orderId, itemsToReorder)
        if (shippingInfo) {
          saveReorderShipping(orderId, shippingInfo)
        }
      }

      if (failedItems.length > 0) {
        const failedNames = failedItems.map((item) => item.name).join(', ')
        alert(
          `Một số sản phẩm không thể thêm vào giỏ hàng: ${failedNames}. Các sản phẩm còn lại đã được thêm.`,
        )
      } else {
        alert('Đã thêm tất cả sản phẩm vào giỏ hàng. Vui lòng kiểm tra giỏ hàng của bạn.')
      }

      if (successCount > 0) {
        cleanReorderStorageIfExpired()
        try {
          await router.push({ path: '/cart', query: { reorder: orderId } })
        } catch (navigationError) {
          console.error('❌ [REORDER] Lỗi khi điều hướng tới trang giỏ hàng:', navigationError)
        }
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

