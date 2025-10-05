import { describe, it, expect, beforeEach } from 'vitest'
import { useFormValidation, validationSchemas } from '../../../composables/useFormValidation.js'

describe('useFormValidation', () => {
  let form

  beforeEach(() => {
    form = useFormValidation(validationSchemas.login, { email: '', password: '' })
  })

  it('should initialize with empty values', () => {
    expect(form.values.value).toEqual({
      email: '',
      password: ''
    })
    expect(form.errors.value).toEqual({})
    expect(form.touched.value).toEqual({})
    expect(form.isSubmitting.value).toBe(false)
  })

  it('should validate required fields', () => {
    form.validateField('email', '')
    expect(form.hasFieldError('email')).toBe(true)
    expect(form.getFieldError('email')).toBe('Email là bắt buộc')
  })

  it('should validate email format', () => {
    form.validateField('email', 'invalid-email')
    expect(form.hasFieldError('email')).toBe(true)
    expect(form.getFieldError('email')).toBe('Email không hợp lệ')
  })

  it('should accept valid email', () => {
    form.validateField('email', 'test@example.com')
    expect(form.hasFieldError('email')).toBe(false)
  })

  it('should validate password minimum length', () => {
    form.validateField('password', '123')
    expect(form.hasFieldError('password')).toBe(true)
    expect(form.getFieldError('password')).toBe('Mật khẩu phải có ít nhất 6 ký tự')
  })

  it('should accept valid password', () => {
    form.validateField('password', 'password123')
    expect(form.hasFieldError('password')).toBe(false)
  })

  it('should set values correctly', () => {
    form.setValue('email', 'test@example.com')
    expect(form.values.value.email).toBe('test@example.com')
  })

  it('should set touched state', () => {
    form.setTouched('email', true)
    expect(form.isFieldTouched('email')).toBe(true)
  })

  it('should validate all fields', () => {
    form.setValue('email', '')
    form.setValue('password', '')
    
    const isValid = form.validateAll()
    expect(isValid).toBe(false)
    expect(form.hasErrors.value).toBe(true)
  })

  it('should validate all fields successfully', () => {
    form.setValue('email', 'test@example.com')
    form.setValue('password', 'password123')
    
    const isValid = form.validateAll()
    expect(isValid).toBe(true)
    expect(form.hasErrors.value).toBe(false)
  })

  it('should reset form', () => {
    form.setValue('email', 'test@example.com')
    form.setTouched('email', true)
    form.setError('email', 'Some error')
    
    form.reset()
    
    expect(form.values.value).toEqual({
      email: '',
      password: ''
    })
    expect(form.errors.value).toEqual({})
    expect(form.touched.value).toEqual({})
    expect(form.isSubmitting.value).toBe(false)
  })

  it('should handle form submission', async () => {
    form.setValue('email', 'test@example.com')
    form.setValue('password', 'password123')
    
    const mockSubmit = vi.fn().mockResolvedValue('success')
    
    const result = await form.handleSubmit(mockSubmit)
    
    expect(result).toBe(true)
    expect(mockSubmit).toHaveBeenCalledWith({
      email: 'test@example.com',
      password: 'password123'
    })
  })

  it('should handle form submission with validation errors', async () => {
    form.setValue('email', '')
    form.setValue('password', '')
    
    const mockSubmit = vi.fn()
    
    const result = await form.handleSubmit(mockSubmit)
    
    expect(result).toBe(false)
    expect(mockSubmit).not.toHaveBeenCalled()
    expect(form.hasErrors.value).toBe(true)
  })
})
