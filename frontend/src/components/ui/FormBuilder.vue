<template>
  <form @submit.prevent="handleSubmit" class="form-builder" :class="formClass">
    <div class="form-header" v-if="title || description">
      <h3 v-if="title" class="form-title">{{ title }}</h3>
      <p v-if="description" class="form-description">{{ description }}</p>
    </div>

    <div class="form-fields">
      <FormField
        v-for="field in formFields"
        :key="field.name"
        v-model="values[field.name]"
        :type="field.type"
        :label="field.label"
        :placeholder="field.placeholder"
        :help-text="field.helpText"
        :required="field.required"
        :min-length="field.minLength"
        :max-length="field.maxLength"
        :min="field.min"
        :max="field.max"
        :step="field.step"
        :disabled="field.disabled || isSubmitting"
        :readonly="field.readonly"
        :loading="field.loading"
        :errors="errors[field.name] || []"
        :touched="touched[field.name] || false"
        :size="field.size || size"
        :variant="field.variant || variant"
        :options="field.options || []"
        :rows="field.rows || 3"
        :icon="field.icon"
        :autocomplete="field.autocomplete"
        :show-counter="field.showCounter"
        @blur="handleFieldBlur(field.name)"
        @focus="handleFieldFocus(field.name)"
        @validate="(value) => validateField(field.name, value)"
      />
    </div>

    <div class="form-actions" v-if="showActions">
      <slot name="actions" :isSubmitting="isSubmitting" :isValid="isValid">
        <button
          type="button"
          class="btn btn-secondary"
          :disabled="isSubmitting"
          @click="handleReset"
        >
          {{ resetText }}
        </button>
        <button
          type="submit"
          class="btn btn-primary"
          :disabled="!isValid || isSubmitting"
        >
          <div v-if="isSubmitting" class="btn-loading">
            <div class="spinner"></div>
          </div>
          {{ isSubmitting ? submittingText : submitText }}
        </button>
      </slot>
    </div>

    <!-- Form Status -->
    <div v-if="statusMessage" class="form-status" :class="statusType">
      <i :class="statusIcon"></i>
      {{ statusMessage }}
    </div>
  </form>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import FormField from './FormField.vue'
import { useFormValidation } from '../../composables/useFormValidation'
import { SUCCESS_MESSAGES } from '../../utils/errorMessages.js'

const props = defineProps({
  // Form configuration
  schema: {
    type: Object,
    required: true
  },
  initialValues: {
    type: Object,
    default: () => ({})
  },
  
  // Form appearance
  title: {
    type: String,
    default: ''
  },
  description: {
    type: String,
    default: ''
  },
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
  
  // Form behavior
  validateOnBlur: {
    type: Boolean,
    default: true
  },
  validateOnChange: {
    type: Boolean,
    default: false
  },
  showActions: {
    type: Boolean,
    default: true
  },
  
  // Button texts
  submitText: {
    type: String,
    default: 'Gửi'
  },
  submittingText: {
    type: String,
    default: 'Đang xử lý...'
  },
  resetText: {
    type: String,
    default: 'Đặt lại'
  },
  
  // Status
  statusMessage: {
    type: String,
    default: ''
  },
  statusType: {
    type: String,
    default: 'info',
    validator: (value) => ['info', 'success', 'warning', 'error'].includes(value)
  }
})

const emit = defineEmits(['submit', 'reset', 'field-change', 'field-blur', 'field-focus'])

// Use form validation composable
const {
  values,
  errors,
  touched,
  isSubmitting,
  isValid,
  isDirty,
  hasErrors,
  setValue,
  setTouched,
  setError,
  clearErrors,
  reset,
  handleSubmit: validateAndSubmit,
  validateField,
  validateAll
} = useFormValidation(props.schema, props.initialValues)

// Computed properties
const formFields = computed(() => {
  return Object.entries(props.schema).map(([name, config]) => ({
    name,
    ...config
  }))
})

const formClass = computed(() => [
  'form-builder',
  `form-builder--${props.size}`,
  `form-builder--${props.variant}`,
  {
    'is-submitting': isSubmitting.value,
    'has-errors': hasErrors.value,
    'is-valid': isValid.value
  }
])

const statusIcon = computed(() => {
  switch (props.statusType) {
    case 'success':
      return 'ph-check-circle'
    case 'warning':
      return 'ph-warning-circle'
    case 'error':
      return 'ph-x-circle'
    default:
      return 'ph-info'
  }
})

// Methods
const handleSubmit = async () => {
  const result = await validateAndSubmit(async (formData) => {
    emit('submit', formData)
  })
  
  if (result) {
    // Show success message if no custom status is provided
    if (!props.statusMessage) {
      // This would be handled by the parent component
    }
  }
}

const handleReset = () => {
  reset()
  emit('reset')
}

const handleFieldBlur = (fieldName) => {
  if (props.validateOnBlur) {
    setTouched(fieldName, true)
  }
  emit('field-blur', { fieldName, value: values.value[fieldName] })
}

const handleFieldFocus = (fieldName) => {
  emit('field-focus', { fieldName, value: values.value[fieldName] })
}

const handleFieldChange = (fieldName, value) => {
  setValue(fieldName, value)
  emit('field-change', { fieldName, value })
}

// Watch for validation changes
watch(values, (newValues) => {
  if (props.validateOnChange) {
    // Validate all fields on any change
    validateAll()
  }
}, { deep: true })

// Expose methods for parent components
defineExpose({
  values,
  errors,
  touched,
  isValid,
  isDirty,
  hasErrors,
  setValue,
  setTouched,
  setError,
  clearErrors,
  reset,
  validateField,
  validateAll
})
</script>

<style scoped>
.form-builder {
  max-width: 100%;
}

.form-header {
  margin-bottom: 2rem;
}

.form-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #111827;
  margin-bottom: 0.5rem;
}

.form-description {
  color: #6b7280;
  font-size: 0.875rem;
  line-height: 1.25rem;
  margin: 0;
}

.form-fields {
  margin-bottom: 2rem;
}

.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  align-items: center;
  padding-top: 1.5rem;
  border-top: 1px solid #e5e7eb;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  border-radius: 0.5rem;
  font-weight: 500;
  font-size: 0.875rem;
  line-height: 1.25rem;
  transition: all 0.2s ease;
  cursor: pointer;
  border: none;
  outline: none;
  text-decoration: none;
  min-height: 2.75rem;
}

.btn:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.btn-primary {
  background-color: #3b82f6;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: #2563eb;
}

.btn-primary:focus {
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.btn-secondary {
  background-color: #ffffff;
  color: #374151;
  border: 1px solid #d1d5db;
}

.btn-secondary:hover:not(:disabled) {
  background-color: #f9fafb;
}

.btn-secondary:focus {
  box-shadow: 0 0 0 3px rgba(209, 213, 219, 0.1);
}

.btn-loading {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.spinner {
  width: 1rem;
  height: 1rem;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.form-status {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  border-radius: 0.5rem;
  font-size: 0.875rem;
  line-height: 1.25rem;
  margin-top: 1rem;
}

.form-status.info {
  background-color: #eff6ff;
  color: #1e40af;
  border: 1px solid #bfdbfe;
}

.form-status.success {
  background-color: #f0fdf4;
  color: #166534;
  border: 1px solid #bbf7d0;
}

.form-status.warning {
  background-color: #fffbeb;
  color: #92400e;
  border: 1px solid #fed7aa;
}

.form-status.error {
  background-color: #fef2f2;
  color: #dc2626;
  border: 1px solid #fecaca;
}

/* Form variants */
.form-builder--small .form-title {
  font-size: 1.25rem;
}

.form-builder--large .form-title {
  font-size: 1.875rem;
}

.form-builder--small .btn {
  padding: 0.5rem 1rem;
  font-size: 0.75rem;
}

.form-builder--large .btn {
  padding: 1rem 2rem;
  font-size: 1rem;
}

/* Form states */
.form-builder.is-submitting {
  pointer-events: none;
  opacity: 0.7;
}

.form-builder.has-errors .form-actions {
  border-top-color: #fecaca;
}

.form-builder.is-valid .form-actions {
  border-top-color: #bbf7d0;
}

/* Responsive */
@media (max-width: 640px) {
  .form-actions {
    flex-direction: column-reverse;
    gap: 0.75rem;
  }
  
  .btn {
    width: 100%;
  }
}
</style>
