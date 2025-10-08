import { ref, computed } from 'vue'
import { ERROR_MESSAGES, SUCCESS_MESSAGES, WARNING_MESSAGES } from '../utils/errorMessages.js'

export const useFormValidation = (schema, initialValues = {}) => {
  const values = ref({ ...initialValues })
  const errors = ref({})
  const touched = ref({})
  const isSubmitting = ref(false)

  // Computed properties
  const isValid = computed(() => {
    return Object.keys(errors.value).length === 0 && 
           Object.values(values.value).some(value => value !== '' && value != null)
  })

  const isDirty = computed(() => {
    return Object.keys(touched.value).length > 0
  })

  const hasErrors = computed(() => {
    return Object.keys(errors.value).length > 0
  })

  // Validation functions
  const validateField = (fieldName, value) => {
    if (!schema[fieldName]) return

    const fieldSchema = schema[fieldName]
    const fieldErrors = []

    // Required validation
    if (fieldSchema.required && (!value || value.toString().trim() === '')) {
      fieldErrors.push(fieldSchema.requiredMessage || ERROR_MESSAGES.REQUIRED_FIELD)
    }

    // Skip other validations if field is empty and not required
    if (!value || value.toString().trim() === '') {
      if (fieldErrors.length === 0) {
        delete errors.value[fieldName]
      } else {
        errors.value[fieldName] = fieldErrors
      }
      return
    }

    // Min length validation
    if (fieldSchema.minLength && value.toString().length < fieldSchema.minLength) {
      fieldErrors.push(fieldSchema.minLengthMessage || `${fieldSchema.label || fieldName} phải có ít nhất ${fieldSchema.minLength} ký tự`)
    }

    // Max length validation
    if (fieldSchema.maxLength && value.toString().length > fieldSchema.maxLength) {
      fieldErrors.push(fieldSchema.maxLengthMessage || `${fieldSchema.label || fieldName} không được vượt quá ${fieldSchema.maxLength} ký tự`)
    }

    // Email validation
    if (fieldSchema.type === 'email') {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      if (!emailRegex.test(value)) {
        fieldErrors.push(fieldSchema.emailMessage || ERROR_MESSAGES.INVALID_EMAIL)
      }
    }

    // Phone validation
    if (fieldSchema.type === 'phone') {
      const phoneRegex = /^[0-9]{10,11}$/
      if (!phoneRegex.test(value.replace(/\s/g, ''))) {
        fieldErrors.push(fieldSchema.phoneMessage || ERROR_MESSAGES.INVALID_PHONE)
      }
    }

    // Number validation
    if (fieldSchema.type === 'number') {
      if (isNaN(value) || value === '') {
        fieldErrors.push(fieldSchema.numberMessage || `${fieldSchema.label || fieldName} phải là số`)
      } else {
        const numValue = parseFloat(value)
        if (fieldSchema.min !== undefined && numValue < fieldSchema.min) {
          fieldErrors.push(fieldSchema.minMessage || `${fieldSchema.label || fieldName} phải lớn hơn hoặc bằng ${fieldSchema.min}`)
        }
        if (fieldSchema.max !== undefined && numValue > fieldSchema.max) {
          fieldErrors.push(fieldSchema.maxMessage || `${fieldSchema.label || fieldName} phải nhỏ hơn hoặc bằng ${fieldSchema.max}`)
        }
      }
    }

    // Custom validation
    if (fieldSchema.validate && typeof fieldSchema.validate === 'function') {
      const customError = fieldSchema.validate(value, values.value)
      if (customError) {
        fieldErrors.push(customError)
      }
    }

    // Set errors
    if (fieldErrors.length > 0) {
      errors.value[fieldName] = fieldErrors
    } else {
      delete errors.value[fieldName]
    }
  }

  const validateAll = () => {
    const newErrors = {}
    
    Object.keys(schema).forEach(fieldName => {
      validateField(fieldName, values.value[fieldName])
      if (errors.value[fieldName]) {
        newErrors[fieldName] = errors.value[fieldName]
      }
    })

    return Object.keys(newErrors).length === 0
  }

  // Form actions
  const setValue = (fieldName, value) => {
    values.value[fieldName] = value
    if (touched.value[fieldName]) {
      validateField(fieldName, value)
    }
  }

  const setTouched = (fieldName, isTouched = true) => {
    touched.value[fieldName] = isTouched
    if (isTouched) {
      validateField(fieldName, values.value[fieldName])
    }
  }

  const setError = (fieldName, errorMessage) => {
    if (errorMessage) {
      errors.value[fieldName] = [errorMessage]
    } else {
      delete errors.value[fieldName]
    }
  }

  const clearErrors = () => {
    errors.value = {}
  }

  const reset = () => {
    values.value = { ...initialValues }
    errors.value = {}
    touched.value = {}
    isSubmitting.value = false
  }

  const handleSubmit = async (onSubmit) => {
    if (isSubmitting.value) return

    // Mark all fields as touched
    Object.keys(schema).forEach(fieldName => {
      touched.value[fieldName] = true
    })

    // Validate all fields
    const isValid = validateAll()
    
    if (!isValid) {
      return false
    }

    try {
      isSubmitting.value = true
      await onSubmit(values.value)
      return true
    } catch (error) {
      console.error('Form submission error:', error)
      return false
    } finally {
      isSubmitting.value = false
    }
  }

  // Field-specific helpers
  const getFieldError = (fieldName) => {
    return errors.value[fieldName]?.[0] || null
  }

  const hasFieldError = (fieldName) => {
    return !!errors.value[fieldName]
  }

  const isFieldTouched = (fieldName) => {
    return !!touched.value[fieldName]
  }

  const isFieldValid = (fieldName) => {
    return !errors.value[fieldName] && touched.value[fieldName]
  }

  return {
    // State
    values,
    errors,
    touched,
    isSubmitting,

    // Computed
    isValid,
    isDirty,
    hasErrors,

    // Actions
    setValue,
    setTouched,
    setError,
    clearErrors,
    reset,
    handleSubmit,
    validateField,
    validateAll,

    // Helpers
    getFieldError,
    hasFieldError,
    isFieldTouched,
    isFieldValid
  }
}

// Enhanced validation schemas with standardized messages
export const validationSchemas = {
  login: {
    email: {
      label: 'Email',
      type: 'email',
      required: true,
      requiredMessage: 'Vui lòng nhập email',
      emailMessage: ERROR_MESSAGES.INVALID_EMAIL
    },
    password: {
      label: 'Mật khẩu',
      type: 'password',
      required: true,
      minLength: 6,
      requiredMessage: 'Vui lòng nhập mật khẩu',
      minLengthMessage: ERROR_MESSAGES.PASSWORD_TOO_SHORT
    }
  },

  register: {
    name: {
      label: 'Họ và tên',
      required: true,
      minLength: 2,
      maxLength: 50,
      requiredMessage: 'Vui lòng nhập họ và tên',
      minLengthMessage: 'Họ và tên phải có ít nhất 2 ký tự',
      maxLengthMessage: 'Họ và tên không được vượt quá 50 ký tự'
    },
    email: {
      label: 'Email',
      type: 'email',
      required: true,
      requiredMessage: 'Vui lòng nhập email',
      emailMessage: ERROR_MESSAGES.INVALID_EMAIL
    },
    phone: {
      label: 'Số điện thoại',
      type: 'phone',
      required: true,
      requiredMessage: 'Vui lòng nhập số điện thoại',
      phoneMessage: ERROR_MESSAGES.INVALID_PHONE
    },
    password: {
      label: 'Mật khẩu',
      type: 'password',
      required: true,
      minLength: 6,
      maxLength: 50,
      requiredMessage: 'Vui lòng nhập mật khẩu',
      minLengthMessage: ERROR_MESSAGES.PASSWORD_TOO_SHORT,
      maxLengthMessage: 'Mật khẩu không được vượt quá 50 ký tự'
    },
    confirmPassword: {
      label: 'Xác nhận mật khẩu',
      type: 'password',
      required: true,
      requiredMessage: 'Vui lòng xác nhận mật khẩu',
      validate: (value, allValues) => {
        if (value !== allValues.password) {
          return ERROR_MESSAGES.PASSWORD_MISMATCH
        }
        return null
      }
    }
  },

  profile: {
    name: {
      label: 'Họ và tên',
      required: true,
      minLength: 2,
      maxLength: 50,
      requiredMessage: 'Vui lòng nhập họ và tên',
      minLengthMessage: 'Họ và tên phải có ít nhất 2 ký tự',
      maxLengthMessage: 'Họ và tên không được vượt quá 50 ký tự'
    },
    email: {
      label: 'Email',
      type: 'email',
      required: true,
      requiredMessage: 'Vui lòng nhập email',
      emailMessage: ERROR_MESSAGES.INVALID_EMAIL
    },
    phone: {
      label: 'Số điện thoại',
      type: 'phone',
      required: true,
      requiredMessage: 'Vui lòng nhập số điện thoại',
      phoneMessage: ERROR_MESSAGES.INVALID_PHONE
    },
    address: {
      label: 'Địa chỉ',
      maxLength: 200,
      maxLengthMessage: 'Địa chỉ không được vượt quá 200 ký tự'
    }
  },

  contact: {
    name: {
      label: 'Họ và tên',
      required: true,
      minLength: 2,
      maxLength: 50,
      requiredMessage: 'Vui lòng nhập họ và tên',
      minLengthMessage: 'Họ và tên phải có ít nhất 2 ký tự',
      maxLengthMessage: 'Họ và tên không được vượt quá 50 ký tự'
    },
    email: {
      label: 'Email',
      type: 'email',
      required: true,
      requiredMessage: 'Vui lòng nhập email',
      emailMessage: ERROR_MESSAGES.INVALID_EMAIL
    },
    subject: {
      label: 'Chủ đề',
      required: true,
      minLength: 5,
      maxLength: 100,
      requiredMessage: 'Vui lòng nhập chủ đề',
      minLengthMessage: 'Chủ đề phải có ít nhất 5 ký tự',
      maxLengthMessage: 'Chủ đề không được vượt quá 100 ký tự'
    },
    message: {
      label: 'Nội dung',
      required: true,
      minLength: 10,
      maxLength: 1000,
      requiredMessage: 'Vui lòng nhập nội dung',
      minLengthMessage: 'Nội dung phải có ít nhất 10 ký tự',
      maxLengthMessage: 'Nội dung không được vượt quá 1000 ký tự'
    }
  },

  newsletter: {
    email: {
      label: 'Email',
      type: 'email',
      required: true,
      requiredMessage: 'Vui lòng nhập email',
      emailMessage: ERROR_MESSAGES.INVALID_EMAIL
    }
  },

  // Additional schemas for checkout and other forms
  checkout: {
    fullName: {
      label: 'Họ và tên',
      required: true,
      minLength: 2,
      maxLength: 50,
      requiredMessage: 'Vui lòng nhập họ và tên',
      minLengthMessage: 'Họ và tên phải có ít nhất 2 ký tự',
      maxLengthMessage: 'Họ và tên không được vượt quá 50 ký tự'
    },
    email: {
      label: 'Email',
      type: 'email',
      required: true,
      requiredMessage: 'Vui lòng nhập email',
      emailMessage: ERROR_MESSAGES.INVALID_EMAIL
    },
    phone: {
      label: 'Số điện thoại',
      type: 'phone',
      required: true,
      requiredMessage: 'Vui lòng nhập số điện thoại',
      phoneMessage: ERROR_MESSAGES.INVALID_PHONE
    },
    address: {
      label: 'Địa chỉ',
      required: true,
      minLength: 10,
      maxLength: 200,
      requiredMessage: 'Vui lòng nhập địa chỉ',
      minLengthMessage: 'Địa chỉ phải có ít nhất 10 ký tự',
      maxLengthMessage: 'Địa chỉ không được vượt quá 200 ký tự'
    },
    city: {
      label: 'Thành phố',
      required: true,
      minLength: 2,
      maxLength: 50,
      requiredMessage: 'Vui lòng nhập thành phố',
      minLengthMessage: 'Tên thành phố phải có ít nhất 2 ký tự',
      maxLengthMessage: 'Tên thành phố không được vượt quá 50 ký tự'
    },
    notes: {
      label: 'Ghi chú',
      maxLength: 500,
      maxLengthMessage: 'Ghi chú không được vượt quá 500 ký tự'
    }
  },

  // Password change form
  changePassword: {
    currentPassword: {
      label: 'Mật khẩu hiện tại',
      type: 'password',
      required: true,
      requiredMessage: 'Vui lòng nhập mật khẩu hiện tại'
    },
    newPassword: {
      label: 'Mật khẩu mới',
      type: 'password',
      required: true,
      minLength: 6,
      maxLength: 50,
      requiredMessage: 'Vui lòng nhập mật khẩu mới',
      minLengthMessage: ERROR_MESSAGES.PASSWORD_TOO_SHORT,
      maxLengthMessage: 'Mật khẩu không được vượt quá 50 ký tự'
    },
    confirmPassword: {
      label: 'Xác nhận mật khẩu mới',
      type: 'password',
      required: true,
      requiredMessage: 'Vui lòng xác nhận mật khẩu mới',
      validate: (value, allValues) => {
        if (value !== allValues.newPassword) {
          return ERROR_MESSAGES.PASSWORD_MISMATCH
        }
        return null
      }
    }
  }
}
