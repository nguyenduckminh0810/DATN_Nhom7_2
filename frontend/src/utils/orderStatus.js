const normalizeKey = (value) => {
  return value ? value.toString().trim().toUpperCase() : ''
}

const STATUS_DEFINITIONS = [
  {
    code: 'PENDING',
    label: 'Chờ xác nhận',
    badgeClass: 'bg-warning',
    color: '#ffc107',
    sortOrder: 10,
    forCustomer: true,
    forAdmin: true,
    forKanban: true,
    aliases: ['CHO_XAC_NHAN', 'Chờ xác nhận', 'CHỜ XÁC NHẬN', 'PENDING'],
  },
  {
    code: 'PROCESSING',
    label: 'Đang xử lý',
    badgeClass: 'bg-info',
    color: '#17a2b8',
    sortOrder: 20,
    forCustomer: true,
    forAdmin: true,
    forKanban: true,
    aliases: [
      'DANG_XU_LY',
      'Đang xử lý',
      'ĐANG XỬ LÝ',
      'CONFIRMED',
      'ĐÃ XÁC NHẬN',
      'Đã xác nhận',
      'PROCESSING',
    ],
  },
  {
    code: 'SHIPPING',
    label: 'Đang giao',
    badgeClass: 'bg-primary',
    color: '#0d6efd',
    sortOrder: 30,
    forCustomer: true,
    forAdmin: true,
    forKanban: true,
    aliases: ['DANG_GIAO', 'Đang giao', 'ĐANG GIAO', 'Chờ giao hàng', 'CHỜ GIAO HÀNG', 'SHIPPING'],
  },
  {
    code: 'DELIVERED',
    label: 'Đã giao',
    badgeClass: 'bg-success',
    color: '#20c997',
    sortOrder: 40,
    forCustomer: true,
    forAdmin: true,
    forKanban: true,
    aliases: ['DA_GIAO', 'Đã giao', 'ĐÃ GIAO', 'DELIVERED'],
  },
  {
    code: 'COMPLETED',
    label: 'Hoàn tất',
    badgeClass: 'bg-success',
    color: '#28a745',
    sortOrder: 50,
    forCustomer: true,
    forAdmin: true,
    forKanban: true,
    aliases: ['HOAN_THANH', 'Hoàn tất', 'HOÀN TẤT', 'Hoàn thành', 'HOÀN THÀNH', 'COMPLETED'],
  },
  {
    code: 'CANCELLED',
    label: 'Đã hủy',
    badgeClass: 'bg-danger',
    color: '#dc3545',
    sortOrder: 60,
    forCustomer: true,
    forAdmin: true,
    forKanban: true,
    aliases: ['DA_HUY', 'Đã hủy', 'ĐÃ HỦY', 'CANCELLED'],
  },
]

const DEFAULT_STATUS_CODE = 'PENDING'

const STATUS_BY_CODE = STATUS_DEFINITIONS.reduce((acc, definition) => {
  acc[definition.code] = definition
  return acc
}, {})

const STATUS_BY_ALIAS = STATUS_DEFINITIONS.reduce((acc, definition) => {
  const aliases = Array.isArray(definition.aliases) ? definition.aliases : []
  const uniqueAliases = new Set([definition.code, definition.label, ...aliases])

  uniqueAliases.forEach((alias) => {
    const key = normalizeKey(alias)
    if (key && !acc[key]) {
      acc[key] = definition
    }
  })

  return acc
}, {})

const getDefinition = (status) => {
  const key = normalizeKey(status)
  if (key && STATUS_BY_ALIAS[key]) {
    return STATUS_BY_ALIAS[key]
  }
  return STATUS_BY_CODE[DEFAULT_STATUS_CODE]
}

export const ORDER_STATUS_DEFINITIONS = STATUS_DEFINITIONS.map((definition) => ({ ...definition }))

export const ORDER_STATUS_CODES = Object.freeze(
  STATUS_DEFINITIONS.reduce((acc, definition) => {
    acc[definition.code] = definition.code
    return acc
  }, {}),
)

export const ORDER_STATUS_FOR_CUSTOMER = ORDER_STATUS_DEFINITIONS.filter(
  (definition) => definition.forCustomer,
)

export const ORDER_STATUS_FOR_ADMIN = ORDER_STATUS_DEFINITIONS.filter(
  (definition) => definition.forAdmin,
)

export const ORDER_STATUS_FOR_KANBAN = ORDER_STATUS_DEFINITIONS.filter(
  (definition) => definition.forKanban,
)

export const ORDER_STATUS_FLOW = ORDER_STATUS_DEFINITIONS.filter((definition) =>
  ['PENDING', 'PROCESSING', 'SHIPPING', 'DELIVERED', 'COMPLETED'].includes(definition.code),
)
  .sort((a, b) => a.sortOrder - b.sortOrder)
  .map((definition) => definition.code)

export const normalizeOrderStatus = (status) => {
  const definition = getDefinition(status)
  return { ...definition }
}

export const getOrderStatusCode = (status) => getDefinition(status).code

export const getOrderStatusLabel = (status) => getDefinition(status).label

export const getOrderStatusClass = (status) => getDefinition(status).badgeClass

export const getOrderStatusColor = (status) => getDefinition(status).color


