import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useUserStore } from '../../../stores/user.js'

// Mock the API service
vi.mock('../../../services/api.js', () => ({
  default: {
    auth: {
      login: vi.fn(),
      register: vi.fn(),
      logout: vi.fn(),
      refresh: vi.fn(),
    },
    user: {
      getProfile: vi.fn(),
      updateProfile: vi.fn(),
      changePassword: vi.fn(),
      uploadAvatar: vi.fn(),
    }
  }
}))

describe('User Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('should initialize with empty user state', () => {
    const userStore = useUserStore()
    
    expect(userStore.user).toBeNull()
    expect(userStore.isAuthenticated).toBe(false)
    expect(userStore.userRole).toBe('guest')
    expect(userStore.isAdmin).toBe(false)
  })

  it('should set user data correctly', () => {
    const userStore = useUserStore()
    const mockUser = {
      id: 1,
      name: 'Test User',
      email: 'test@example.com',
      role: 'user'
    }

    userStore.setUser(mockUser)

    expect(userStore.user).toEqual(mockUser)
    expect(userStore.isAuthenticated).toBe(true)
    expect(userStore.userRole).toBe('user')
    expect(userStore.userName).toBe('Test User')
    expect(userStore.userEmail).toBe('test@example.com')
  })

  it('should set admin user correctly', () => {
    const userStore = useUserStore()
    const mockAdmin = {
      id: 1,
      name: 'Admin User',
      email: 'admin@example.com',
      role: 'admin'
    }

    userStore.setUser(mockAdmin)

    expect(userStore.isAdmin).toBe(true)
    expect(userStore.userRole).toBe('admin')
  })

  it('should clear user data when setUser is called with null', () => {
    const userStore = useUserStore()
    const mockUser = {
      id: 1,
      name: 'Test User',
      email: 'test@example.com',
      role: 'user'
    }

    userStore.setUser(mockUser)
    expect(userStore.isAuthenticated).toBe(true)

    userStore.setUser(null)
    expect(userStore.user).toBeNull()
    expect(userStore.isAuthenticated).toBe(false)
  })

  it('should set token correctly', () => {
    const userStore = useUserStore()
    const mockToken = 'mock-jwt-token'

    userStore.setToken(mockToken)
    expect(localStorage.setItem).toHaveBeenCalledWith('auro_token', mockToken)
  })

  it('should clear token when setToken is called with null', () => {
    const userStore = useUserStore()

    userStore.setToken(null)
    expect(localStorage.removeItem).toHaveBeenCalledWith('auro_token')
  })

  it('should clear error when clearError is called', () => {
    const userStore = useUserStore()
    userStore.error = 'Some error'

    userStore.clearError()
    expect(userStore.error).toBeNull()
  })
})
