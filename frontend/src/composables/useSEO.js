import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'

export const useSEO = () => {
  const route = useRoute()
  
  // Default meta data
  const defaultMeta = {
    title: 'AURO - Thời trang nam cao cấp',
    description: 'Thương hiệu thời trang nam cao cấp với thiết kế tinh tế và chất lượng vượt trội. Khám phá bộ sưu tập áo sơ mi, quần âu, phụ kiện nam đẳng cấp.',
    keywords: 'thời trang nam, áo sơ mi, quần âu, phụ kiện nam, AURO, thời trang cao cấp',
    image: '/images/auro-og-image.jpg',
    url: '',
    type: 'website',
    siteName: 'AURO'
  }

  // Current meta data
  const meta = ref({ ...defaultMeta })

  // Computed properties
  const pageTitle = computed(() => {
    return meta.value.title
  })

  const pageDescription = computed(() => {
    return meta.value.description
  })

  const pageKeywords = computed(() => {
    return meta.value.keywords
  })

  const pageImage = computed(() => {
    return meta.value.image
  })

  const pageUrl = computed(() => {
    return meta.value.url || window.location.href
  })

  // Set meta data
  const setMeta = (newMeta) => {
    meta.value = { ...defaultMeta, ...newMeta }
    updateDocumentMeta()
  }

  // Update document meta tags
  const updateDocumentMeta = () => {
    // Update title
    document.title = meta.value.title

    // Update meta description
    updateMetaTag('description', meta.value.description)
    updateMetaTag('keywords', meta.value.keywords)

    // Update Open Graph tags
    updateMetaTag('og:title', meta.value.title, 'property')
    updateMetaTag('og:description', meta.value.description, 'property')
    updateMetaTag('og:image', meta.value.image, 'property')
    updateMetaTag('og:url', pageUrl.value, 'property')
    updateMetaTag('og:type', meta.value.type, 'property')
    updateMetaTag('og:site_name', meta.value.siteName, 'property')

    // Update Twitter Card tags
    updateMetaTag('twitter:card', 'summary_large_image', 'name')
    updateMetaTag('twitter:title', meta.value.title, 'name')
    updateMetaTag('twitter:description', meta.value.description, 'name')
    updateMetaTag('twitter:image', meta.value.image, 'name')

    // Update canonical URL
    updateLinkTag('canonical', pageUrl.value)
  }

  // Helper function to update meta tags
  const updateMetaTag = (name, content, attribute = 'name') => {
    let tag = document.querySelector(`meta[${attribute}="${name}"]`)
    
    if (!tag) {
      tag = document.createElement('meta')
      tag.setAttribute(attribute, name)
      document.head.appendChild(tag)
    }
    
    tag.setAttribute('content', content)
  }

  // Helper function to update link tags
  const updateLinkTag = (rel, href) => {
    let tag = document.querySelector(`link[rel="${rel}"]`)
    
    if (!tag) {
      tag = document.createElement('link')
      tag.setAttribute('rel', rel)
      document.head.appendChild(tag)
    }
    
    tag.setAttribute('href', href)
  }

  // Set page title
  const setTitle = (title) => {
    setMeta({ title })
  }

  // Set page description
  const setDescription = (description) => {
    setMeta({ description })
  }

  // Set page keywords
  const setKeywords = (keywords) => {
    setMeta({ keywords })
  }

  // Set page image
  const setImage = (image) => {
    setMeta({ image })
  }

  // Set page URL
  const setUrl = (url) => {
    setMeta({ url })
  }

  // Generate structured data
  const generateStructuredData = (type, data) => {
    const structuredData = {
      '@context': 'https://schema.org',
      '@type': type,
      ...data
    }

    // Remove existing structured data
    const existingScript = document.querySelector('script[type="application/ld+json"]')
    if (existingScript) {
      existingScript.remove()
    }

    // Add new structured data
    const script = document.createElement('script')
    script.type = 'application/ld+json'
    script.textContent = JSON.stringify(structuredData)
    document.head.appendChild(script)
  }

  // Product structured data
  const setProductStructuredData = (product) => {
    const structuredData = {
      '@context': 'https://schema.org',
      '@type': 'Product',
      name: product.name,
      description: product.description,
      image: product.images,
      brand: {
        '@type': 'Brand',
        name: 'AURO'
      },
      offers: {
        '@type': 'Offer',
        price: product.price,
        priceCurrency: 'VND',
        availability: product.stock > 0 ? 'https://schema.org/InStock' : 'https://schema.org/OutOfStock'
      },
      aggregateRating: product.rating ? {
        '@type': 'AggregateRating',
        ratingValue: product.rating,
        reviewCount: product.reviewCount
      } : undefined
    }

    generateStructuredData('Product', structuredData)
  }

  // Organization structured data
  const setOrganizationStructuredData = () => {
    const structuredData = {
      '@context': 'https://schema.org',
      '@type': 'Organization',
      name: 'AURO',
      description: 'Thương hiệu thời trang nam cao cấp',
      url: 'https://auro.com',
      logo: 'https://auro.com/images/auro-logo.png',
      contactPoint: {
        '@type': 'ContactPoint',
        telephone: '+84-xxx-xxx-xxx',
        contactType: 'customer service'
      },
      sameAs: [
        'https://facebook.com/auro',
        'https://instagram.com/auro',
        'https://twitter.com/auro'
      ]
    }

    generateStructuredData('Organization', structuredData)
  }

  // Breadcrumb structured data
  const setBreadcrumbStructuredData = (breadcrumbs) => {
    const structuredData = {
      '@context': 'https://schema.org',
      '@type': 'BreadcrumbList',
      itemListElement: breadcrumbs.map((item, index) => ({
        '@type': 'ListItem',
        position: index + 1,
        name: item.name,
        item: item.url
      }))
    }

    generateStructuredData('BreadcrumbList', structuredData)
  }

  // FAQ structured data
  const setFAQStructuredData = (faqs) => {
    const structuredData = {
      '@context': 'https://schema.org',
      '@type': 'FAQPage',
      mainEntity: faqs.map(faq => ({
        '@type': 'Question',
        name: faq.question,
        acceptedAnswer: {
          '@type': 'Answer',
          text: faq.answer
        }
      }))
    }

    generateStructuredData('FAQPage', structuredData)
  }

  // Initialize SEO for current route
  const initializeSEO = () => {
    const routeMeta = route.meta || {}
    
    if (routeMeta.title) {
      setTitle(routeMeta.title)
    }
    
    if (routeMeta.description) {
      setDescription(routeMeta.description)
    }
    
    if (routeMeta.keywords) {
      setKeywords(routeMeta.keywords)
    }
    
    if (routeMeta.image) {
      setImage(routeMeta.image)
    }

    // Set URL
    setUrl(window.location.href)
  }

  // Reset to default meta
  const resetMeta = () => {
    meta.value = { ...defaultMeta }
    updateDocumentMeta()
  }

  return {
    // State
    meta,

    // Computed
    pageTitle,
    pageDescription,
    pageKeywords,
    pageImage,
    pageUrl,

    // Actions
    setMeta,
    setTitle,
    setDescription,
    setKeywords,
    setImage,
    setUrl,
    updateDocumentMeta,
    initializeSEO,
    resetMeta,

    // Structured data
    generateStructuredData,
    setProductStructuredData,
    setOrganizationStructuredData,
    setBreadcrumbStructuredData,
    setFAQStructuredData
  }
}
