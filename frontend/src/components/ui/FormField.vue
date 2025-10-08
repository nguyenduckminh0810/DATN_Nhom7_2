<template>
  <div class="form-field" :class="{ 'has-error': hasError, 'is-valid': isValid }">
    <!-- Label -->
    <label 
      v-if="label" 
      :for="fieldId" 
      class="form-label"
      :class="{ 'required': required }"
    >
      {{ label }}
      <span v-if="required" class="required-asterisk">*</span>
    </label>

    <!-- Input Container -->
    <div class="input-container" :class="inputContainerClass">
      <!-- Input/Textarea/Select -->
      <input
        v-if="type !== 'textarea' && type !== 'select'"
        :id="fieldId"
        :type="inputType"
        :value="modelValue"
        :placeholder="placeholder"
        :disabled="disabled"
        :readonly="readonly"
        :maxlength="maxLength"
        :minlength="minLength"
        :min="min"
        :max="max"
        :step="step"
        :autocomplete="autocomplete"
        :class="inputClass"
        @input="handleInput"
        @blur="handleBlur"
        @focus="handleFocus"
        @keydown="handleKeydown"
      />

      <textarea
        v-else-if="type === 'textarea'"
        :id="fieldId"
        :value="modelValue"
        :placeholder="placeholder"
        :disabled="disabled"
        :readonly="readonly"
        :maxlength="maxLength"
        :minlength="minLength"
        :rows="rows"
        :class="inputClass"
        @input="handleInput"
        @blur="handleBlur"
        @focus="handleFocus"
      ></textarea>

      <select
        v-else-if="type === 'select'"
        :id="fieldId"
        :value="modelValue"
        :disabled="disabled"
        :class="inputClass"
        @change="handleInput"
        @blur="handleBlur"
        @focus="handleFocus"
      >
        <option v-if="placeholder" value="" disabled>
          {{ placeholder }}
        </option>
        <option
          v-for="option in options"
          :key="option.value"
          :value="option.value"
        >
          {{ option.label }}
        </option>
      </select>

      <!-- Input Icons -->
      <div v-if="showPasswordToggle || icon || loading" class="input-icon">
        <div v-if="loading" class="loading-icon">
          <div class="spinner"></div>
        </div>
        <button
          v-else-if="showPasswordToggle"
          type="button"
          class="password-toggle"
          @click="togglePassword"
          :aria-label="showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'"
        >
          <i :class="showPassword ? 'ph-eye-slash' : 'ph-eye'"></i>
        </button>
        <i v-else-if="icon" :class="icon"></i>
      </div>
    </div>

    <!-- Help Text -->
    <div v-if="helpText && !hasError" class="help-text">
      {{ helpText }}
    </div>

    <!-- Error Messages -->
    <div v-if="hasError" class="error-messages">
      <div
        v-for="(error, index) in errorMessages"
        :key="index"
        class="error-message"
      >
        <i class="ph-warning-circle"></i>
        {{ error }}
      </div>
    </div>

    <!-- Character Counter -->
    <div v-if="showCounter && maxLength" class="character-counter">
      {{ characterCount }}/{{ maxLength }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  // Basic props
  modelValue: {
    type: [String, Number],
    default: ''
  },
  type: {
    type: String,
    default: 'text',
    validator: (value) => [
      'text', 'email', 'password', 'tel', 'url', 'number', 'search',
      'textarea', 'select', 'date', 'time', 'datetime-local'
    ].includes(value)
  },
  label: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: ''
  },
  helpText: {
    type: String,
    default: ''
  },
  
  // Validation props
  required: {
    type: Boolean,
    default: false
  },
  minLength: {
    type: Number,
    default: null
  },
  maxLength: {
    type: Number,
    default: null
  },
  min: {
    type: Number,
    default: null
  },
  max: {
    type: Number,
    default: null
  },
  step: {
    type: Number,
    default: null
  },
  
  // State props
  disabled: {
    type: Boolean,
    default: false
  },
  readonly: {
    type: Boolean,
    default: false
  },
  loading: {
    type: Boolean,
    default: false
  },
  
  // Error props
  errors: {
    type: Array,
    default: () => []
  },
  touched: {
    type: Boolean,
    default: false
  },
  
  // UI props
  size: {
    type: String,
    default: 'medium',
    validator: (value) => ['small', 'medium', 'large'].includes(value)
  },
  variant: {
    type: String,
    default: 'default',
    validator: (value) => ['default', 'outlined', 'filled'].includes(value)
  },
  
  // Select props
  options: {
    type: Array,
    default: () => []
  },
  
  // Textarea props
  rows: {
    type: Number,
    default: 3
  },
  
  // Icon props
  icon: {
    type: String,
    default: ''
  },
  
  // Autocomplete
  autocomplete: {
    type: String,
    default: 'off'
  },
  
  // Counter
  showCounter: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits([
  'update:modelValue',
  'blur',
  'focus',
  'keydown',
  'validate'
])

// Internal state
const showPassword = ref(false)
const isFocused = ref(false)

// Computed properties
const fieldId = computed(() => `field-${Math.random().toString(36).substr(2, 9)}`)

const inputType = computed(() => {
  if (props.type === 'password') {
    return showPassword.value ? 'text' : 'password'
  }
  return props.type
})

const inputClass = computed(() => [
  'form-input',
  `form-input--${props.size}`,
  `form-input--${props.variant}`,
  {
    'has-error': hasError.value,
    'is-valid': isValid.value,
    'is-focused': isFocused.value,
    'is-disabled': props.disabled,
    'is-readonly': props.readonly
  }
])

const inputContainerClass = computed(() => [
  'input-container',
  `input-container--${props.variant}`,
  {
    'has-icon': props.icon || props.showPasswordToggle || props.loading,
    'has-error': hasError.value,
    'is-focused': isFocused.value
  }
])

const hasError = computed(() => {
  return props.errors && props.errors.length > 0 && props.touched
})

const isValid = computed(() => {
  return props.touched && !hasError.value && props.modelValue
})

const errorMessages = computed(() => {
  return props.errors || []
})

const characterCount = computed(() => {
  return String(props.modelValue || '').length
})

const showPasswordToggle = computed(() => {
  return props.type === 'password'
})

// Methods
const handleInput = (event) => {
  const value = event.target.value
  emit('update:modelValue', value)
  emit('validate', value)
}

const handleBlur = (event) => {
  isFocused.value = false
  emit('blur', event)
  emit('validate', props.modelValue)
}

const handleFocus = (event) => {
  isFocused.value = true
  emit('focus', event)
}

const handleKeydown = (event) => {
  emit('keydown', event)
}

const togglePassword = () => {
  showPassword.value = !showPassword.value
}

// Watch for external changes
watch(() => props.modelValue, (newValue) => {
  emit('validate', newValue)
})
</script>

<style scoped>
.form-field {
  margin-bottom: 1.5rem;
  position: relative;
}

.form-label {
  display: block;
  font-weight: 500;
  color: #374151;
  margin-bottom: 0.5rem;
  font-size: 0.875rem;
  line-height: 1.25rem;
}

.form-label.required .required-asterisk {
  color: #ef4444;
  margin-left: 0.25rem;
}

.input-container {
  position: relative;
  display: flex;
  align-items: center;
}

.input-container--default {
  border-radius: 0.5rem;
  border: 1px solid #d1d5db;
  background-color: #ffffff;
  transition: all 0.2s ease;
}

.input-container--outlined {
  border-radius: 0.5rem;
  border: 2px solid #d1d5db;
  background-color: transparent;
  transition: all 0.2s ease;
}

.input-container--filled {
  border-radius: 0.5rem;
  border: 1px solid transparent;
  background-color: #f9fafb;
  transition: all 0.2s ease;
}

.input-container.is-focused {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.input-container.has-error {
  border-color: #ef4444;
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1);
}

.input-container.has-icon {
  padding-right: 2.5rem;
}

.form-input {
  width: 100%;
  border: none;
  outline: none;
  background: transparent;
  font-size: 1rem;
  line-height: 1.5rem;
  color: #111827;
  padding: 0.75rem 1rem;
}

.form-input--small {
  padding: 0.5rem 0.75rem;
  font-size: 0.875rem;
}

.form-input--large {
  padding: 1rem 1.25rem;
  font-size: 1.125rem;
}

.form-input::placeholder {
  color: #9ca3af;
}

.form-input:disabled {
  color: #9ca3af;
  cursor: not-allowed;
}

.form-input:read-only {
  cursor: default;
}

.input-icon {
  position: absolute;
  right: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  align-items: center;
  color: #6b7280;
}

.loading-icon {
  display: flex;
  align-items: center;
}

.spinner {
  width: 1rem;
  height: 1rem;
  border: 2px solid #e5e7eb;
  border-top: 2px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.password-toggle {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.25rem;
  color: #6b7280;
  transition: color 0.2s ease;
}

.password-toggle:hover {
  color: #374151;
}

.help-text {
  margin-top: 0.25rem;
  font-size: 0.75rem;
  color: #6b7280;
  line-height: 1rem;
}

.error-messages {
  margin-top: 0.25rem;
}

.error-message {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.75rem;
  color: #ef4444;
  line-height: 1rem;
}

.error-message i {
  font-size: 0.875rem;
}

.character-counter {
  margin-top: 0.25rem;
  font-size: 0.75rem;
  color: #6b7280;
  text-align: right;
}

/* Form field states */
.form-field.has-error .form-label {
  color: #ef4444;
}

.form-field.is-valid .form-label {
  color: #059669;
}

.form-field.is-valid .input-container {
  border-color: #10b981;
}

.form-field.is-valid .input-container.is-focused {
  border-color: #10b981;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.1);
}

/* Responsive */
@media (max-width: 640px) {
  .form-input {
    font-size: 16px; /* Prevent zoom on iOS */
  }
}
</style>
