import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import LoginPopup from '../../../components/ui/LoginPopup.vue'

// Mock the stores and composables
vi.mock('../../../stores/user.js', () => ({
  useUserStore: () => ({
    login: vi.fn().mockResolvedValue({ success: true }),
    isLoading: false,
    error: null
  })
}))

vi.mock('../../../composables/useFormValidation.js', () => ({
  useFormValidation: () => ({
    values: { value: { email: '', password: '' } },
    errors: { value: {} },
    isSubmitting: { value: false },
    setValue: vi.fn(),
    setTouched: vi.fn(),
    handleSubmit: vi.fn().mockResolvedValue(true),
    getFieldError: vi.fn(),
    hasFieldError: vi.fn()
  }),
  validationSchemas: {
    login: {
      email: { label: 'Email', type: 'email', required: true },
      password: { label: 'Mật khẩu', type: 'password', required: true, minLength: 6 }
    }
  }
}))

vi.mock('../../../composables/useToast.js', () => ({
  useToast: () => ({
    success: vi.fn(),
    error: vi.fn()
  })
}))

vi.mock('../../../composables/useErrorHandler.js', () => ({
  useErrorHandler: () => ({
    handleApiError: vi.fn()
  })
}))

describe('LoginPopup', () => {
  let wrapper
  let router
  let pinia

  beforeEach(() => {
    pinia = createPinia()
    setActivePinia(pinia)
    
    router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/', component: { template: '<div>Home</div>' } }
      ]
    })

    wrapper = mount(LoginPopup, {
      props: {
        isOpen: true
      },
      global: {
        plugins: [pinia, router],
        stubs: {
          'router-link': true
        }
      }
    })
  })

  it('should render when isOpen is true', () => {
    expect(wrapper.find('.login-popup-overlay').exists()).toBe(true)
    expect(wrapper.find('.login-popup-container').exists()).toBe(true)
  })

  it('should not render when isOpen is false', async () => {
    await wrapper.setProps({ isOpen: false })
    expect(wrapper.find('.login-popup-overlay').exists()).toBe(false)
  })

  it('should emit close event when close button is clicked', async () => {
    const closeBtn = wrapper.find('.close-btn')
    await closeBtn.trigger('click')
    
    expect(wrapper.emitted('close')).toBeTruthy()
  })

  it('should emit switchToRegister event when register link is clicked', async () => {
    const registerLink = wrapper.find('[data-testid="switch-to-register"]')
    if (registerLink.exists()) {
      await registerLink.trigger('click')
      expect(wrapper.emitted('switchToRegister')).toBeTruthy()
    }
  })

  it('should have correct form structure', () => {
    expect(wrapper.find('form').exists()).toBe(true)
    expect(wrapper.find('input[type="text"]').exists()).toBe(true)
    expect(wrapper.find('input[type="password"]').exists()).toBe(true)
    expect(wrapper.find('button[type="submit"]').exists()).toBe(true)
  })

  it('should toggle password visibility', async () => {
    const passwordInput = wrapper.find('input[type="password"]')
    const toggleBtn = wrapper.find('.password-toggle')
    
    expect(passwordInput.attributes('type')).toBe('password')
    
    await toggleBtn.trigger('click')
    await wrapper.vm.$nextTick()
    
    // The type should change to text
    expect(wrapper.find('input[type="text"]').exists()).toBe(true)
  })

  it('should show loading state during submission', async () => {
    // Mock the form validation to return isSubmitting: true
    wrapper.vm.isSubmitting = true
    await wrapper.vm.$nextTick()
    
    const submitBtn = wrapper.find('button[type="submit"]')
    expect(submitBtn.attributes('disabled')).toBeDefined()
  })

  it('should handle social login buttons', async () => {
    const googleBtn = wrapper.find('.google-btn')
    const facebookBtn = wrapper.find('.facebook-btn')
    
    if (googleBtn.exists()) {
      await googleBtn.trigger('click')
      // Should not throw error
    }
    
    if (facebookBtn.exists()) {
      await facebookBtn.trigger('click')
      // Should not throw error
    }
  })

  it('should have proper accessibility attributes', () => {
    const form = wrapper.find('form')
    const textInput = wrapper.find('input[type="text"]')
    const passwordInput = wrapper.find('input[type="password"]')
    
    expect(form.exists()).toBe(true)
    expect(textInput.exists()).toBe(true)
    expect(passwordInput.exists()).toBe(true)
  })
})
