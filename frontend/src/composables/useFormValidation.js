import { ref, computed } from 'vue'

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
      fieldErrors.push(`${fieldSchema.label || fieldName} là bắt buộc`)
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
      fieldErrors.push(`${fieldSchema.label || fieldName} phải có ít nhất ${fieldSchema.minLength} ký tự`)
    }

    // Max length validation
    if (fieldSchema.maxLength && value.toString().length > fieldSchema.maxLength) {
      fieldErrors.push(`${fieldSchema.label || fieldName} không được vượt quá ${fieldSchema.maxLength} ký tự`)
    }

    // Email validation
    if (fieldSchema.type === 'email') {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      if (!emailRegex.test(value)) {
        fieldErrors.push('Email không hợp lệ')
      }
    }

    // Phone validation
    if (fieldSchema.type === 'phone') {
      const phoneRegex = /^[0-9]{10,11}$/
      if (!phoneRegex.test(value.replace(/\s/g, ''))) {
        fieldErrors.push('Số điện thoại không hợp lệ')
      }
    }

    // Number validation
    if (fieldSchema.type === 'number') {
      if (isNaN(value) || value === '') {
        fieldErrors.push(`${fieldSchema.label || fieldName} phải là số`)
      } else {
        const numValue = parseFloat(value)
        if (fieldSchema.min !== undefined && numValue < fieldSchema.min) {
          fieldErrors.push(`${fieldSchema.label || fieldName} phải lớn hơn hoặc bằng ${fieldSchema.min}`)
        }
        if (fieldSchema.max !== undefined && numValue > fieldSchema.max) {
          fieldErrors.push(`${fieldSchema.label || fieldName} phải nhỏ hơn hoặc bằng ${fieldSchema.max}`)
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

// Common validation schemas
export const validationSchemas = {
  login: {
    email: {
      label: 'Email',
      type: 'email',
      required: true
    },
    password: {
      label: 'Mật khẩu',
      type: 'password',
      required: true,
      minLength: 6
    }
  },

  register: {
    name: {
      label: 'Họ và tên',
      required: true,
      minLength: 2,
      maxLength: 50
    },
    email: {
      label: 'Email',
      type: 'email',
      required: true
    },
    phone: {
      label: 'Số điện thoại',
      type: 'phone',
      required: true
    },
    password: {
      label: 'Mật khẩu',
      type: 'password',
      required: true,
      minLength: 6,
      maxLength: 50
    },
    confirmPassword: {
      label: 'Xác nhận mật khẩu',
      type: 'password',
      required: true,
      validate: (value, allValues) => {
        if (value !== allValues.password) {
          return 'Mật khẩu xác nhận không khớp'
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
      maxLength: 50
    },
    email: {
      label: 'Email',
      type: 'email',
      required: true
    },
    phone: {
      label: 'Số điện thoại',
      type: 'phone',
      required: true
    },
    address: {
      label: 'Địa chỉ',
      maxLength: 200
    }
  },

  contact: {
    name: {
      label: 'Họ và tên',
      required: true,
      minLength: 2,
      maxLength: 50
    },
    email: {
      label: 'Email',
      type: 'email',
      required: true
    },
    subject: {
      label: 'Chủ đề',
      required: true,
      minLength: 5,
      maxLength: 100
    },
    message: {
      label: 'Nội dung',
      required: true,
      minLength: 10,
      maxLength: 1000
    }
  },

  newsletter: {
    email: {
      label: 'Email',
      type: 'email',
      required: true
    }
  }
}
