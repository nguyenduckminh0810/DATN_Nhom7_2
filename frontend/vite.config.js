import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  build: {
    // Code splitting optimization
    rollupOptions: {
      output: {
        manualChunks: {
          // Vendor chunks
          'vue-vendor': ['vue', 'vue-router', 'pinia'],
          'ui-vendor': ['bootstrap', '@popperjs/core', 'phosphor-icons'],
          'chart-vendor': ['chart.js'],
          'utils-vendor': ['axios', '@vueuse/core'],
          
          // Feature chunks
          'admin': [
            './src/views/admin/Dashboard.vue',
            './src/views/admin/Products.vue',
            './src/views/admin/Categories.vue',
            './src/views/admin/Orders.vue',
            './src/views/admin/Users.vue',
            './src/views/admin/AnalyticsNew.vue'
          ],
          'forms': [
            './src/components/common/FormField.vue',
            './src/components/common/FormBuilder.vue',
            './src/composables/useFormValidation.js'
          ],
          'product': [
            './src/views/ProductDetail.vue',
            './src/views/Category.vue',
            './src/components/product/ProductCard.vue'
          ]
        }
      }
    },
    // Chunk size warnings
    chunkSizeWarningLimit: 1000,
    // Minification
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true
      }
    },
    // Source maps for production debugging
    sourcemap: false,
    // Asset optimization
    assetsInlineLimit: 4096
  },
  // Development server optimization
  server: {
    hmr: {
      overlay: false
    }
  },
  // CSS optimization
  css: {
    devSourcemap: false
  },
  // Dependency optimization
  optimizeDeps: {
    include: [
      'vue',
      'vue-router',
      'pinia',
      'axios',
      '@vueuse/core',
      'bootstrap',
      'phosphor-icons'
    ],
    exclude: ['@fortawesome/fontawesome-free']
  }
})
