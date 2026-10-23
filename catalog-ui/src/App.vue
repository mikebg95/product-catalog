<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import HomePage from './components/HomePage.vue'
import ProductPage from './components/ProductPage.vue'

// Two pages, chosen by the url hash: #/ is the shop, #/products/{sku} is one product.
const hash = ref(window.location.hash)
const onHashChange = () => {
  hash.value = window.location.hash
  window.scrollTo(0, 0)
}
onMounted(() => window.addEventListener('hashchange', onHashChange))
onUnmounted(() => window.removeEventListener('hashchange', onHashChange))

const sku = computed(() => {
  const match = hash.value.match(/^#\/products\/(.+)$/)
  return match ? decodeURIComponent(match[1]) : null
})
</script>

<template>
  <ProductPage v-if="sku" :key="sku" :sku="sku" />
  <HomePage v-else />
</template>
